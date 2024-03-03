package kport.gugu_utils.common.envtypes;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class EnvWeather implements EnvironmentType {
    public Weathers getWeather() {
        return weather;
    }

    private final Weathers weather;

    public EnvWeather(Weathers weather) {
        this.weather = weather;
    }

    @Override
    public boolean isMeet(World world, BlockPos pos) {
        WorldInfo info = world.getWorldInfo();
        switch (weather) {
            case SUNNY:
                return !info.isRaining() && !info.isThundering();
            case RAINING:
                return info.isRaining();
            case SNOWING:
                return (info.isRaining() || info.isThundering()) && world.canSnowAt(pos, false);
            case THUNDERING:
                return info.isThundering();
        }
        return false;
    }

    @Override
    public List<String> getTooltip() {
        return Collections.singletonList(I18n.format("tooltips.gugu-utils.environment.weather." + weather.name().toLowerCase(Locale.ROOT)));
    }

    @Override
    public String getName() {
        return "weather";
    }

    @ZenClass
    public enum Weathers {
        SUNNY,
        RAINING,
        SNOWING,
        THUNDERING
    }
}
