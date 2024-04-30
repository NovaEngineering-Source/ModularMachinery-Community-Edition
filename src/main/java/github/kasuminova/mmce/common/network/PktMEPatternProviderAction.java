package github.kasuminova.mmce.common.network;

import github.kasuminova.mmce.common.container.ContainerMEPatternProvider;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktMEPatternProviderAction implements IMessage, IMessageHandler<PktMEPatternProviderAction, IMessage> {

    private Action action = null;

    public PktMEPatternProviderAction() {
    }

    public PktMEPatternProviderAction(final Action action) {
        this.action = action;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        action = Action.values()[buf.readByte()];
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeByte(action.ordinal());
    }

    @Override
    public IMessage onMessage(final PktMEPatternProviderAction message, final MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (!(player.openContainer instanceof final ContainerMEPatternProvider patternProvider)) {
            return null;
        }
        MEPatternProvider provider = patternProvider.getOwner();
        switch (message.action) {
            case ENABLE_BLOCKING_MODE -> {
                provider.setBlockingMode(true);
                provider.markChunkDirty();
            }
            case DISABLE_BLOCKING_MODE -> {
                provider.setBlockingMode(false);
                provider.markChunkDirty();
            }
            case RETURN_ITEMS -> provider.returnItemsScheduled();
        }
        return null;
    }

    public enum Action {
        ENABLE_BLOCKING_MODE,
        DISABLE_BLOCKING_MODE,
        RETURN_ITEMS,
    }

}
