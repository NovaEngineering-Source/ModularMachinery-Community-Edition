package github.kasuminova.mmce.common.block.appeng;

import github.kasuminova.mmce.common.tile.base.MEGasBus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public abstract class BlockMEGasBus extends BlockMEMachineComponent {

    @Override
    public void breakBlock(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof MEGasBus) {
            IItemHandlerModifiable inv = ((MEGasBus) te).getInventoryByName("upgrades");
            if (inv != null) {
                for (int i = 0; i < inv.getSlots(); i++) {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        spawnAsEntity(worldIn, pos, stack);
                        inv.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

}
