package hellfirepvp.modularmachinery.common.item;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ItemBlockController extends ItemBlockMachineComponent {
    private final BlockController ctrlBlock;

    public ItemBlockController(final BlockController ctrlBlock) {
        super(ctrlBlock);
        this.ctrlBlock = ctrlBlock;
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        return ctrlBlock.getLocalizedName();
    }

    @Override
    public boolean placeBlockAt(@Nonnull final ItemStack stack,
                                @Nonnull final EntityPlayer player,
                                @Nonnull final World world,
                                @Nonnull final BlockPos pos,
                                @Nonnull final EnumFacing side,
                                final float hitX,
                                final float hitY,
                                final float hitZ,
                                @Nonnull final IBlockState newState) {
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileMultiblockMachineController ctrl) {
                NBTTagCompound stackTag = stack.getTagCompound();
                if (stackTag != null && stackTag.hasKey("owner")) {
                    String ownerUUIDStr = stackTag.getString("owner");
                    try {
                        ctrl.setOwner(UUID.fromString(ownerUUIDStr));
                    } catch (Exception e) {
                        ModularMachinery.log.warn("Invalid owner uuid " + ownerUUIDStr, e);
                    }
                } else {
                    ctrl.setOwner(player.getGameProfile().getId());
                }
            }
            return true;
        }
        return false;
    }
}
