package github.kasuminova.mmce.common.network;

import appeng.container.slot.SlotFake;
import github.kasuminova.mmce.common.container.ContainerMEItemInputBus;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktMEInputBusInvAction implements IMessage, IMessageHandler<PktMEInputBusInvAction, IMessage> {
    private int newAmount = 0;
    private int slotID    = 0;

    public PktMEInputBusInvAction() {
    }

    public PktMEInputBusInvAction(final int newAmount, final int slotID) {
        this.newAmount = newAmount;
        this.slotID = slotID;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.newAmount = buf.readInt();
        this.slotID = buf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(newAmount);
        buf.writeInt(slotID);
    }

    @Override
    public IMessage onMessage(final PktMEInputBusInvAction message, final MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (!(player.openContainer instanceof final ContainerMEItemInputBus inputBus)) {
            return null;
        }

        Slot slot = inputBus.getSlot(message.slotID);
        if (!(slot instanceof SlotFake)) {
            return null;
        }

        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) {
            return null;
        }

        int newAmount = message.newAmount;
        if (newAmount == 0) {
            return null;
        }

        ItemStack newStack = stack.copy();
        newStack.setCount(newAmount);
        slot.putStack(newStack);

        return null;
    }
}
