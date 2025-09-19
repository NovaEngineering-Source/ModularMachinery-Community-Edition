package kport.modularmagic.common.network;

import io.netty.buffer.ByteBuf;
import kport.modularmagic.common.tile.TileStarlightInput;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class StarlightMessage implements IMessage {

    public int      starlightAmount;
    public BlockPos pos;

    public StarlightMessage() {
    }

    public StarlightMessage(int starlightAmount, BlockPos pos) {
        this.starlightAmount = starlightAmount;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        starlightAmount = buf.readInt();
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(starlightAmount);
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    public static class StarlightMessageHandler implements IMessageHandler<StarlightMessage, IMessage> {

        @Override
        public IMessage onMessage(StarlightMessage message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                TileEntity te = ctx.getServerHandler().player.world.getTileEntity(message.pos);
                if (te instanceof final TileStarlightInput starlightInput) {
                    return new StarlightMessage(starlightInput.getStarlightStored(), starlightInput.getPos());
                }
            } else if (ctx.side == Side.CLIENT) {
                TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
                if (te instanceof final TileStarlightInput starlightInput) {
                    Minecraft.getMinecraft().addScheduledTask(() -> starlightInput.setStarlight(message.starlightAmount));
                }
                return null;
            }
            return null;
        }
    }
}
