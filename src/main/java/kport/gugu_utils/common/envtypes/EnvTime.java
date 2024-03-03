package kport.gugu_utils.common.envtypes;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class EnvTime implements EnvironmentType {
    private final int begin;
    private final int end;

    public EnvTime(int begin, int end) {
        this.begin = begin;
        this.end = end;

    }

    public static String translateTime(int time) {
        int hour = (time / 1000 + 6) % 24;
        int minute = (int) (((double) (time % 1000)) * 60 / 1000);
        return String.format("%d:%02d", hour, minute);
    }

    @Override
    public boolean isMeet(World world, BlockPos pos) {
        int time = (int) (world.getWorldTime() % 24000);
        if (begin <= end) {
            return time >= begin && time <= end;
        } else {
            return time >= begin || time <= end;
        }
    }

    @Override
    public List<String> getTooltip() {
        return Collections.singletonList(I18n.format("tooltips.gugu-utils.environment.time", translateTime(begin), translateTime(end)));
    }

    @Override
    public String getName() {
        return "time";
    }
}
