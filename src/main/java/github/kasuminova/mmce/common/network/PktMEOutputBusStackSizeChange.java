package github.kasuminova.mmce.common.network;

import github.kasuminova.mmce.common.tile.MEItemOutputBus;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktMEOutputBusStackSizeChange implements IMessage, IMessageHandler<PktMEOutputBusStackSizeChange, IMessage> {
    private BlockPos pos = BlockPos.ORIGIN;
    private int stackSize = Integer.MAX_VALUE;

    public PktMEOutputBusStackSizeChange() {
    }

    public PktMEOutputBusStackSizeChange(final BlockPos pos, final int stackSize) {
        this.pos = pos;
        this.stackSize = stackSize;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.stackSize = buf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeInt(this.stackSize);
    }

    @Override
    public IMessage onMessage(final PktMEOutputBusStackSizeChange message, final MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;

        player.getServerWorld().addScheduledTask(() -> {
            TileEntity te = player.world.getTileEntity(message.pos);

            if (!(te instanceof MEItemOutputBus outputBus)) {
                return;
            }

            int validatedStackSize = Math.max(1, message.stackSize);

            outputBus.setConfiguredStackSize(validatedStackSize);

            outputBus.markDirty();
        });

        return null;
    }
}