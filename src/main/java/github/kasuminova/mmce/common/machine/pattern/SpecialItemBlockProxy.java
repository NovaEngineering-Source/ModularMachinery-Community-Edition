package github.kasuminova.mmce.common.machine.pattern;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface SpecialItemBlockProxy {

    boolean isValid(final ItemStack stack);

    TileEntity transformState(final ItemStack stack, final World world);

    ItemStack getTrueStack(final IBlockState state, final TileEntity te);
}
