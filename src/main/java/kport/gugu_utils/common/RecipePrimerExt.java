package kport.gugu_utils.common;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.world.IBiome;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.RecipePrimer;
import hellfirepvp.modularmachinery.common.machine.IOType;
import kport.gugu_utils.common.envtypes.*;
import kport.gugu_utils.common.requirements.*;
import net.minecraft.world.biome.Biome;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Arrays;
import java.util.Locale;
@ZenRegister
@ZenExpansion("mods.modularmachinery.RecipePrimer")
public class RecipePrimerExt {

    //----------------------------------------------------------------------------------------------
    // mana
    //----------------------------------------------------------------------------------------------
    @ZenMethod
    public static RecipePrimer addManaInput(RecipePrimer primer, int mana) {
        primer.appendComponent(new RequirementMana(mana, IOType.INPUT));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addManaOutput(RecipePrimer primer, int mana) {
        primer.appendComponent(new RequirementMana(mana, IOType.OUTPUT));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addManaPerTickInput(RecipePrimer primer, int mana) {
        primer.appendComponent(new RequirementManaPerTick(mana, primer.getTotalProcessingTickTime(), IOType.INPUT));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addManaPerTickOutput(RecipePrimer primer, int mana) {
        primer.appendComponent(new RequirementManaPerTick(mana, primer.getTotalProcessingTickTime(), IOType.OUTPUT));
        return primer;
    }

    //----------------------------------------------------------------------------------------------
    // ember
    //----------------------------------------------------------------------------------------------
    @ZenMethod
    public static RecipePrimer addEmberInput(RecipePrimer primer, int ember) {
        primer.appendComponent(new RequirementEmber(ember, IOType.INPUT));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addEmberOutput(RecipePrimer primer, int ember) {
        primer.appendComponent(new RequirementEmber(ember, IOType.OUTPUT));
        return primer;
    }

    //----------------------------------------------------------------------------------------------
    // starlight
    //----------------------------------------------------------------------------------------------
    @ZenMethod
    public static RecipePrimer addStarlightInput(RecipePrimer primer, int starlight, String constellationName) {
        IConstellation constellation = ConstellationRegistry.getConstellationByName(constellationName);
        if (constellation == null) {
            ModularMachinery.log.warn("Couldn't find constellation " + constellationName);
        }
        primer.appendComponent(new RequirementStarlight(starlight, constellation, IOType.INPUT));
        return primer;
    }

//    @ZenMethod
//    public static RecipePrimer addStarlightOutput(RecipePrimer primer, int starlight, String constellationName) {
//        IConstellation constellation = ConstellationRegistry.getConstellationByName(constellationName);
//        if (constellation == null) {
//            GuGuUtils.logger.warn("Couldn't find constellation " + constellationName);
//        }
//        primer.appendComponent(new RequirementStarlight(starlight, constellation, IOType.OUTPUT));
//        return primer;
//    }

    @ZenMethod
    public static RecipePrimer addStarlightInput(RecipePrimer primer, int starlight) {
        primer.appendComponent(new RequirementStarlight(starlight, null, IOType.INPUT));
        return primer;
    }


    //----------------------------------------------------------------------------------------------
    // environment
    //----------------------------------------------------------------------------------------------
    private static void setEnvironment(RecipePrimer primer, EnvironmentType type) {

        primer.appendComponent(new RequirementEnvironment(type, IOType.INPUT));
    }

    @ZenMethod
    public static RecipePrimer setBiome(RecipePrimer primer, IBiome[] biomes) {
        setEnvironment(primer, new EnvBoime(Arrays.stream(biomes).map(CraftTweakerMC::getBiome).toArray(Biome[]::new)));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer setAltitude(RecipePrimer primer, int min, int max) {
        setEnvironment(primer, new EnvAltitude(min, max));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer setTime(RecipePrimer primer, int min, int max) {
        setEnvironment(primer, new EnvTime(min, max));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer setDimension(RecipePrimer primer, int[] dimensions) {
        setEnvironment(primer, new EnvDimension(dimensions));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer setMoonPhase(RecipePrimer primer, int[] moonphases) {
        for (int moonphase : moonphases)
            if (moonphase < 0 || moonphase > 7) {
                CraftTweakerAPI.logError("Mooo Phase can not be:" + moonphase);
            }
        setEnvironment(primer, new EnvMoonPhase(moonphases));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer setWeather(RecipePrimer primer, String weather) {
        EnvWeather.Weathers weatherF = null;
        try {
            weatherF = EnvWeather.Weathers.valueOf(weather.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            CraftTweakerAPI.logError("Weather can not be:" + weather);
            return primer;
        }
        setEnvironment(primer, new EnvWeather(weatherF));
        return primer;
    }

    //----------------------------------------------------------------------------------------------
    // air
    //----------------------------------------------------------------------------------------------
    @ZenMethod
    public static RecipePrimer addCompressedAirInput(RecipePrimer primer, float pressure, int air) {
        primer.appendComponent(new RequirementCompressedAir(pressure, air, IOType.INPUT));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addCompressedAirOutput(RecipePrimer primer, float pressure, int air) {
        primer.appendComponent(new RequirementCompressedAir(pressure, air, IOType.OUTPUT));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addCompressedAirPerTickInput(RecipePrimer primer, float pressure, int air) {
        primer.appendComponent(new RequirementCompressedAirPerTick(pressure, air, primer.getTotalProcessingTickTime(), IOType.INPUT));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addCompressedAirPerTickOutput(RecipePrimer primer, float pressure, int air) {
        primer.appendComponent(new RequirementCompressedAirPerTick(pressure, air, primer.getTotalProcessingTickTime(), IOType.OUTPUT));
        return primer;
    }


    //----------------------------------------------------------------------------------------------
    // hot_air
    //----------------------------------------------------------------------------------------------
    @ZenMethod
    public static RecipePrimer addHotAirInput(RecipePrimer primer, int consume, int minTemperature, int maxTemperature) {
        primer.appendComponent(new RequirementHotAir(minTemperature, maxTemperature, consume, IOType.INPUT));
        return primer;
    }

    @ZenMethod
    public static RecipePrimer addHotAirOutput(RecipePrimer primer, int maxTemperature) {
        primer.appendComponent(new RequirementHotAir(0, maxTemperature, 0, IOType.OUTPUT));
        return primer;
    }

    //----------------------------------------------------------------------------------------------
    // mek_laser
    //----------------------------------------------------------------------------------------------
    /*
    @ZenMethod
    public static RecipePrimer addMekLaserInput
     */

    //----------------------------------------------------------------------------------------------
    // mek_heat
    //----------------------------------------------------------------------------------------------
    /*
    @ZenMethod
    public static RecipePrimer addMekheatInput(RecipePrimer primer,)
    primer.appendComponent(
    return primer;

    @ZenMethod
    public static RecipePrimer addMekheatOutput(RecipePrimer primer,)
    primer.appendComponent(
    return primer;
     */

}