package kport.gugu_utils.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetContainerSlot implements IMessage {
    public PacketSetContainerSlot() {
    }

    public PacketSetContainerSlot(int containerSlot, ItemStack stack) {
        this.containerSlot = containerSlot;
        this.stack = stack;
    }

    private int containerSlot;
    private ItemStack stack;

    @Override
    public void fromBytes(ByteBuf buf) {
        containerSlot = buf.readInt();
        stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(containerSlot);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    public static class Handler implements IMessageHandler<PacketSetContainerSlot, IMessage> {

        @Override
        public IMessage onMessage(PacketSetContainerSlot message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (message.stack.isEmpty() || message.stack.getCount() > message.stack.getMaxStackSize()) {
                    return;
                }
                if (player.openContainer != null) {
                    if (message.containerSlot >= 0 && message.containerSlot < player.openContainer.inventorySlots.size()) {
                        Slot slot = player.openContainer.getSlot(message.containerSlot);
                        slot.putStack(message.stack);
                        slot.onSlotChanged();
                    }
                }
            });

            return null;
        }
    }
}
