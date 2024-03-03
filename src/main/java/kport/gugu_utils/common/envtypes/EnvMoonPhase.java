package kport.gugu_utils.common.envtypes;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class EnvMoonPhase implements EnvironmentType {
    private final int[] phases;

    public EnvMoonPhase(int[] phases) {
        this.phases = phases;
    }

    public int[] getPhases() {
        return phases;
    }

    @Override
    public boolean isMeet(World world, BlockPos pos) {
        int currentPhase = world.provider.getMoonPhase(world.getWorldTime());
        if (!world.isDaytime())
            for (int phase : phases) {
                if (currentPhase == phase) {
                    return true;
                }
            }
        return false;
    }

    @Override
    public List<String> getTooltip() {
        List<String> result = Lists.newArrayList(I18n.format("tooltips.gugu-utils.environment.moonphase.desc"));
        for (int phase : phases) {
            result.add(I18n.format("tooltips.gugu-utils.environment.moonphase." + phase));
        }
        return result;
    }

    @Override
    public String getName() {
        return "moonphase";
    }
}
