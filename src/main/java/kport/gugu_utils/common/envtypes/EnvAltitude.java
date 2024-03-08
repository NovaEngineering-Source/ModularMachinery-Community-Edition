package kport.gugu_utils.common.envtypes;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class EnvAltitude implements EnvironmentType {
    public final String KEY_TOOLTIP_ALTITUDE = "tooltips.gugu-utils.environment.altitude";
    private final int begin;
    private final int end;

    public EnvAltitude(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public boolean isMeet(World world, BlockPos pos) {
        return pos.getY() >= begin && pos.getY() <= end;
    }

    @Override
    public List<String> getTooltip() {
        return Collections.singletonList(I18n.format(KEY_TOOLTIP_ALTITUDE, begin, end));
    }

    @Override
    public String getName() {
        return "altitude";
    }
}
