package kport.gugu_utils.common.envtypes;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

import java.util.List;

public class EnvDimension implements EnvironmentType {
    private final int[] dimensions;

    public EnvDimension(int[] dimension) {
        this.dimensions = dimension;
    }

    @Override
    public boolean isMeet(World world, BlockPos pos) {
        int currentDim = world.provider.getDimension();
        for (int dim : dimensions) {
            if (dim == currentDim) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getTooltip() {
        List<String> result = Lists.newArrayList(I18n.format("tooltips.gugu-utils.environment.dimension"));
        for (int dim : dimensions) {
            result.add(DimensionType.getById(dim).getName());
        }
        return result;
    }

    @Override
    public String getName() {
        return "dimensions";
    }
}
