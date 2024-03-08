package kport.gugu_utils.common.requirements.types;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.IOType;
import kport.gugu_utils.common.envtypes.*;
import kport.gugu_utils.common.requirements.RequirementEnvironment;
import kport.gugu_utils.common.requirements.basic.ComponentRequirementAdapter;
import static kport.gugu_utils.common.requirements.basic.RequirementUtils.tryGet;
import static kport.gugu_utils.common.requirements.basic.RequirementUtils.tryGetArr;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;

import java.util.List;
import java.util.Locale;

public class RequirementTypeEnvironment extends RequirementTypeAdapter<EnvironmentType> implements RequirementTypeAdapter.PerTick<EnvironmentType> {
    @Override
    public ComponentRequirementAdapter.PerTick<EnvironmentType> gererateRequirementPerTick(IOType type, JsonObject obj) {
        String envType = tryGet(obj, "subType", true).getAsString();
        switch (envType) {
            case "weather":
                String weather = tryGet(obj, "weather", true).getAsString();
                return new RequirementEnvironment(
                        new EnvWeather(EnvWeather.Weathers.valueOf(weather.toUpperCase(Locale.ROOT))),
                        type);
            case "biome":
                JsonPrimitive boimeType = tryGet(obj, "biomeType", false);
                if (boimeType != null) {
                    return new RequirementEnvironment(
                            new EnvBoime(BiomeManager.BiomeType.getType(boimeType.getAsString())),
                            type);
                } else {
                    List<Biome> biomes = Lists.newArrayList();
                    for (JsonElement element : tryGetArr(obj, "biomes", true)) {
                        if (element.isJsonPrimitive()) {
                            ResourceLocation location = new ResourceLocation(element.getAsString());
                            if (Biome.REGISTRY.containsKey(location)) {
                                biomes.add(Biome.REGISTRY.getObject(location));
                            }
                        }
                    }
                    return new RequirementEnvironment(
                            new EnvBoime(biomes.toArray(new Biome[0])),
                            type);
                }
            case "dimension":
                List<Integer> dimensions = Lists.newArrayList();
                for (JsonElement element : tryGetArr(obj, "dimensions", true)) {
                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                        dimensions.add(element.getAsInt());
                    }
                }
                return new RequirementEnvironment(
                        new EnvDimension(dimensions.stream().mapToInt(Integer::valueOf).toArray()),
                        type);
            case "moonphase":
                List<Integer> moonphases = Lists.newArrayList();
                for (JsonElement element : tryGetArr(obj, "moonphases", true)) {
                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                        int moonphase = element.getAsInt();
                        if (moonphase >= 0 && moonphase < 8)
                            moonphases.add(element.getAsInt());
                        else
                            ModularMachinery.log.warn("MoonPhase must be in 0 - 7");
                    }
                }
                return new RequirementEnvironment(
                        new EnvMoonPhase(moonphases.stream().mapToInt(Integer::valueOf).toArray()),
                        type);
            case "time":
                int beginTime = tryGet(obj, "begin", true).getAsInt();
                int endTime = tryGet(obj, "end", true).getAsInt();
                return new RequirementEnvironment(
                        new EnvTime(beginTime, endTime),
                        type);
            case "altitude":
                int beginAltitude = tryGet(obj, "begin", true).getAsInt();
                int endAltitude = tryGet(obj, "end", true).getAsInt();
                return new RequirementEnvironment(
                        new EnvAltitude(beginAltitude, endAltitude),
                        type);
        }
        throw new JsonParseException("Type：" + envType + " is not supported");
    }

    @Override
    public ComponentRequirementAdapter<EnvironmentType> gererateRequirement(IOType ioType, JsonObject jsonObject) {
        throw new UnsupportedOperationException("Pertick opeartion not support this!");
    }
}

