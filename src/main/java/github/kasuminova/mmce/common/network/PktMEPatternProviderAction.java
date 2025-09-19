package github.kasuminova.mmce.common.network;

import github.kasuminova.mmce.common.container.ContainerMEPatternProvider;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import hellfirepvp.modularmachinery.ModularMachinery;
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
        ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> {
            MEPatternProvider provider = patternProvider.getOwner();
            switch (message.action) {
                case ENABLE_BLOCKING_MODE -> provider.setWorkMode(MEPatternProvider.WorkModeSetting.BLOCKING_MODE);
                case ENABLE_DEFAULT_MODE -> provider.setWorkMode(MEPatternProvider.WorkModeSetting.DEFAULT);
                case ENABLE_CRAFTING_LOCK_MODE -> provider.setWorkMode(MEPatternProvider.WorkModeSetting.CRAFTING_LOCK_MODE);
                case ENABLE_ENHANCED_BLOCKING_MODE -> provider.setWorkMode(MEPatternProvider.WorkModeSetting.ENHANCED_BLOCKING_MODE);
                case RETURN_ITEMS -> provider.returnItemsScheduled();
            }
        });
        return null;
    }

    public enum Action {
        ENABLE_BLOCKING_MODE,
        ENABLE_DEFAULT_MODE,
        ENABLE_CRAFTING_LOCK_MODE,
        ENABLE_ENHANCED_BLOCKING_MODE,
        RETURN_ITEMS,
    }

}
