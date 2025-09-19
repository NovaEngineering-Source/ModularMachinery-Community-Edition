package github.kasuminova.mmce.common.block.appeng;

import github.kasuminova.mmce.common.tile.MEItemOutputBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMEItemOutputBus extends BlockMEItemBus {

    @Override
    public boolean onBlockActivated(
        @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state,
        @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand,
        @Nonnull EnumFacing facing,
        float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof MEItemOutputBus) {
                playerIn.openGui(ModularMachinery.MODID, CommonProxy.GuiType.ME_ITEM_OUTPUT_BUS.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return new MEItemOutputBus();
    }

    @Override
    public void dropBlockAsItemWithChance(@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, final float chance, final int fortune) {
    }

    @Override
    public void getDrops(@Nonnull final NonNullList<ItemStack> drops, @Nonnull final IBlockAccess world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, final int fortune) {
    }

    @Override
    public void breakBlock(final World worldIn,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        ItemStack dropped = new ItemStack(ItemsMM.meItemOutputBus);

        if (te == null) {
            spawnAsEntity(worldIn, pos, dropped);
            worldIn.removeTileEntity(pos);
            return;
        }
        if (!(te instanceof final MEItemOutputBus bus)) {
            spawnAsEntity(worldIn, pos, dropped);
            worldIn.removeTileEntity(pos);
            return;
        }
        if (!bus.hasItem()) {
            spawnAsEntity(worldIn, pos, dropped);
            worldIn.removeTileEntity(pos);
            return;
        }

        IOInventory inventory = bus.getInternalInventory();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("inventory", inventory.writeNBT());
        dropped.setTagCompound(tag);

        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }

        spawnAsEntity(worldIn, pos, dropped);

        worldIn.removeTileEntity(pos);
    }

    @Override
    public void onBlockPlacedBy(@Nonnull final World worldIn,
                                @Nonnull final BlockPos pos,
                                @Nonnull final IBlockState state,
                                @Nonnull final EntityLivingBase placer,
                                @Nonnull final ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        TileEntity te = worldIn.getTileEntity(pos);
        NBTTagCompound tag = stack.getTagCompound();
        if (te instanceof MEItemOutputBus && tag != null && tag.hasKey("inventory")) {
            ((MEItemOutputBus) te).readInventoryNBT(tag.getCompoundTag("inventory"));
        }
    }
}
