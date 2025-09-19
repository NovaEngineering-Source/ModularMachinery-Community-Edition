package ink.ikx.mmce.common.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.Arrays;
import java.util.Objects;

public class FluidUtils {

    public static boolean isFluidHandler(ItemStack stack) {
        return StackUtils.isNotEmpty(stack) && Objects.nonNull(FluidUtil.getFluidHandler(stack));
    }

    public static boolean areFluidHandler(ItemStack... stacks) {
        return Arrays.stream(stacks).allMatch(FluidUtils::isFluidHandler);
    }

    public static FluidStack getFluidFromHandler(ItemStack stack) {
        if (isFluidHandler(stack)) {
            IFluidTankProperties[] tank = FluidUtil.getFluidHandler(stack).getTankProperties();
            return tank.length == 0 ? null : tank[0].getContents();
        }
        return null;
    }

    public static boolean equalsFluidFromStack(ItemStack stackA, ItemStack stackB) {
        FluidStack fluidA = getFluidFromHandler(stackA);
        FluidStack fluidB = getFluidFromHandler(stackB);
        if (MiscUtils.areNull(fluidA, fluidB)) {
            return false;
        }
        return fluidA.containsFluid(fluidB);
    }

    public static FluidStack getFluidStackFromBlockState(final IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockFluidBase) {
            return new FluidStack(((IFluidBlock) block).getFluid(), 1000);
        } else if (block instanceof BlockLiquid) {
            Material material = state.getMaterial();
            if (material == Material.WATER) {
                return new FluidStack(FluidRegistry.WATER, 1000);
            } else if (material == Material.LAVA) {
                return new FluidStack(FluidRegistry.LAVA, 1000);
            }
        }

        return null;
    }
}
