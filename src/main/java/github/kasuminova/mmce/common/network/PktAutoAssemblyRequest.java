package github.kasuminova.mmce.common.network;

import ink.ikx.mmce.core.AssemblyEventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktAutoAssemblyRequest implements IMessage, IMessageHandler<PktAutoAssemblyRequest, IMessage> {

    protected BlockPos pos;
    protected short    dynamicPatternSize;

    public PktAutoAssemblyRequest() {
    }

    public PktAutoAssemblyRequest(final BlockPos pos, final short dynamicPatternSize) {
        this.pos = pos;
        this.dynamicPatternSize = dynamicPatternSize;
    }

    private static boolean isOutOfRange(final EntityPlayer player, final BlockPos target) {
        BlockPos playerPos = player.getPosition();
        return target.getDistance(playerPos.getX(), playerPos.getY(), playerPos.getZ()) >= 8.0D;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        dynamicPatternSize = buf.readShort();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeShort(dynamicPatternSize);
    }

    @Override
    public IMessage onMessage(final PktAutoAssemblyRequest message, final MessageContext ctx) {
        BlockPos target = message.pos;
        short patternSize = message.dynamicPatternSize;
        EntityPlayerMP player = ctx.getServerHandler().player;

        if (isOutOfRange(player, target)) {
            return null;
        }

        AssemblyEventHandler.INSTANCE.processAutoAssembly(player, player.getHeldItem(EnumHand.MAIN_HAND), target, patternSize);
        return null;
    }

}
