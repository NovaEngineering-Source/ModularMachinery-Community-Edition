package github.kasuminova.mmce.common.block.appeng;

import appeng.api.implementations.items.IMemoryCard;
import github.kasuminova.mmce.common.tile.MEGasInputBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMEGasInputBus extends BlockMEGasBus {

    @Override
    public boolean onBlockActivated(@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, @Nonnull final EntityPlayer playerIn, @Nonnull final EnumHand hand, @Nonnull final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof MEGasInputBus gasInputBus) {
                ItemStack heldItem = playerIn.getHeldItem(hand);
                if (heldItem.getItem() instanceof IMemoryCard memoryCard) {
                    boolean handled = handleSettingsTransfer(gasInputBus, memoryCard, playerIn, heldItem);
                    if (handled) {
                        return true;
                    }
                }

                playerIn.openGui(ModularMachinery.MODID, CommonProxy.GuiType.ME_GAS_INPUT_BUS.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return new MEGasInputBus();
    }

}
