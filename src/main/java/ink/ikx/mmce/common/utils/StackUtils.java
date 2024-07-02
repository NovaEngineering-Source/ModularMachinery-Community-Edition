package ink.ikx.mmce.common.utils;

import github.kasuminova.mmce.common.machine.pattern.SpecialItemBlockProxy;
import github.kasuminova.mmce.common.machine.pattern.SpecialItemBlockProxyRegistry;
import hellfirepvp.modularmachinery.common.block.BlockController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;

import java.util.List;

public class StackUtils {

    public static boolean isNotEmpty(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() != Items.MILK_BUCKET;
    }

    public static boolean isStackFilter(ItemStack stack) {
        return isNotEmpty(stack) && !(Block.getBlockFromItem(stack.getItem()) instanceof BlockController);
    }

    public static ItemStack getStackFromBlockState(IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockFluidBase) {
            return FluidUtil.getFilledBucket(new FluidStack(((IFluidBlock) block).getFluid(), 1000));
        } else if (block instanceof BlockLiquid) {
            Material m = state.getMaterial();
            if (m == Material.LAVA) {
                return new ItemStack(Items.LAVA_BUCKET);
            } else if (m == Material.WATER) {
                return new ItemStack(Items.WATER_BUCKET);
            }
        }
        return new ItemStack(Item.getItemFromBlock(block), 1, block.damageDropped(state));
    }

    public static ItemStack getStackFromBlockState(IBlockState state, BlockPos pos, World world) {
        ItemStack rawStack = ItemStack.EMPTY;
        try {
            rawStack = state.getBlock().getPickBlock(state, new RayTraceResult(Vec3d.ZERO, EnumFacing.UP, pos), world, pos, null);
        } catch (Exception ignored) {
        }
        if (rawStack.isEmpty()) {
            rawStack = getStackFromBlockState(state);
        }
        SpecialItemBlockProxy specialItemBlockProxy = SpecialItemBlockProxyRegistry.INSTANCE.getValidProxy(rawStack);
        if (specialItemBlockProxy != null) {
            return specialItemBlockProxy.getTrueStack(world.getBlockState(pos), world.getTileEntity(pos));
        }
        return rawStack;
    }

    public static ItemStack hasStacks(List<ItemStack> inputStacks, List<ItemStack> outputStacks, boolean isRemove) {
        return outputStacks.stream().filter(stack -> isNotEmpty(hasStack(stack, inputStacks, isRemove))).findFirst().orElse(ItemStack.EMPTY);
    }

    public static int getIndex(List<ItemStack> stacks, ItemStack stack) {
        for (int i = 0; i < stacks.size(); i++) {
            if (stacks.get(i).isItemEqual(stack))
                return i;
        }
        return -1;
    }

    public static ItemStack hasStack(ItemStack stack, List<ItemStack> stacks, boolean isRemove) {
        for (ItemStack stackInSlot : stacks) {
            ItemStack copy = stackInSlot.copy();
            if (stackInSlot.isEmpty()) continue;
            if (FluidUtils.areFluidHandler(stack, stackInSlot)) {
                if (FluidUtils.equalsFluidFromStack(stackInSlot, stack)) {
                    if (stackInSlot.getItem() instanceof ItemBucket || stackInSlot.getItem() instanceof UniversalBucket) {
                        if (isRemove) stackInSlot.shrink(stack.getCount());
                        return copy;
                    }
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.isItemEqual(stack)) {
                if (stackInSlot.getCount() >= stack.getCount()) {
                    if (isRemove) stackInSlot.shrink(stack.getCount());
                    return copy;
                }
            }
        }
        return ItemStack.EMPTY;
    }

}
