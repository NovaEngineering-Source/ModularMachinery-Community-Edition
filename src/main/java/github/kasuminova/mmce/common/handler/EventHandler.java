package github.kasuminova.mmce.common.handler;

import appeng.container.AEBaseContainer;
import github.kasuminova.mmce.common.network.PktPerformanceReport;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.container.ContainerBase;
import hellfirepvp.modularmachinery.common.item.ItemBlockController;
import hellfirepvp.modularmachinery.common.tiles.base.SelectiveUpdateTileEntity;
import hellfirepvp.modularmachinery.common.tiles.base.TileEntitySynchronized;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

@SuppressWarnings("MethodMayBeStatic")
public class EventHandler {
    private static boolean checkTERange(final EntityPlayer player, final TileEntity te) {
        BlockPos tePos = te.getPos();
        BlockPos playerPos = player.getPosition();
        return tePos.getDistance(playerPos.getX(), playerPos.getY(), playerPos.getZ()) >= 6.0D;
    }

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }

        TileEntity te = world.getTileEntity(event.getPos());
        if (te instanceof SelectiveUpdateTileEntity && te instanceof final TileEntitySynchronized teSync) {
            teSync.notifyUpdate();
        }
    }

    /**
     * <p>针对某些容易消耗大量带宽的方块实体提供选择性更新功能，以缓解网络压力。</p>
     * <p>Provide selective updates for certain square entities that tend to consume a lot of bandwidth to relieve network pressure.</p>
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.side != Side.SERVER) {
            return;
        }

        EntityPlayer player = event.player;

        Container container = player.openContainer;
        TileEntity te;
        if (container instanceof ContainerBase) {
            te = ((ContainerBase<?>) container).getOwner();
        } else {
            if (!Mods.AE2.isPresent() || !(container instanceof AEBaseContainer)) {
                return;
            }
            te = ((AEBaseContainer) container).getTileEntity();
        }

        if (!(te instanceof SelectiveUpdateTileEntity) || !(te instanceof final TileEntitySynchronized teSync)) {
            return;
        }

        if (checkTERange(player, te)) {
            player.closeScreen();
            return;
        }

        if (!(event.player instanceof EntityPlayerMP playerMP)) {
            return;
        }

        ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> {
            if (teSync.getLastUpdateTick() + 1 >= playerMP.world.getTotalWorldTime()) {
                playerMP.connection.sendPacket(teSync.getUpdatePacket());
            }

            World world = event.player.getEntityWorld();
            if (te instanceof final TileMultiblockMachineController ctrl && world.getWorldTime() % 15 == 0) {
                int usedTime = ctrl.usedTimeAvg();
                int searchUsedTimeAvg = ctrl.recipeSearchUsedTimeAvg();
                ModularMachinery.NET_CHANNEL.sendTo(new PktPerformanceReport(usedTime, searchUsedTimeAvg, ctrl.getWorkMode()), playerMP);
            }
        });
    }

    @SubscribeEvent
    public void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!TileMultiblockMachineController.enableSecuritySystem) {
            return;
        }
        if (!(event.getState().getBlock() instanceof BlockController)) {
            return;
        }
        EntityPlayer player = event.getPlayer();
        if (player.isCreative()) {
            return;
        }
        World world = event.getWorld();
        TileEntity te = world.getTileEntity(event.getPos());
        if (!(te instanceof TileMultiblockMachineController ctrl)) {
            return;
        }
        UUID ownerUUID = ctrl.getOwner();
        if (ownerUUID == null) {
            return;
        }
        UUID playerUUID = player.getGameProfile().getId();
        if (!playerUUID.equals(ownerUUID)) {
            event.setCanceled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (!TileMultiblockMachineController.enableSecuritySystem) {
            return;
        }
        EntityPlayer player = event.getPlayer();
        if (player.isCreative()) {
            return;
        }
        ItemStack itemInHand = event.getItemInHand();
        if (!(itemInHand.getItem() instanceof ItemBlockController)) {
            return;
        }
        NBTTagCompound stackTag = itemInHand.getTagCompound();
        if (stackTag == null || !stackTag.hasKey("owner")) {
            return;
        }
        String ownerUUIDStr = stackTag.getString("owner");
        try {
            UUID ownerUUID = UUID.fromString(ownerUUIDStr);
            UUID playerUUID = player.getGameProfile().getId();
            if (!playerUUID.equals(ownerUUID)) {
                event.setCanceled(true);
            }
        } catch (Exception e) {
            ModularMachinery.log.warn("Invalid owner uuid " + ownerUUIDStr, e);
        }
    }
}
