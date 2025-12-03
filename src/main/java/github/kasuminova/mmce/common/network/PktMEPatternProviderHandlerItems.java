package github.kasuminova.mmce.common.network;

import github.kasuminova.mmce.client.gui.GuiMEPatternProvider;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PktMEPatternProviderHandlerItems implements IMessage, IMessageHandler<PktMEPatternProviderHandlerItems, IMessage> {

    private NBTTagCompound nbt;

    public PktMEPatternProviderHandlerItems() {
    }

    public PktMEPatternProviderHandlerItems(final MEPatternProvider patternProvider) {
        nbt = patternProvider.writeProviderHandlerNBT(new NBTTagCompound());
    }

    @SideOnly(Side.CLIENT)
    protected static void processPacket(final PktMEPatternProviderHandlerItems message) {
        GuiScreen cur = Minecraft.getMinecraft().currentScreen;
        if (!(cur instanceof GuiMEPatternProvider patternProvider)) {
            return;
        }
        Minecraft.getMinecraft().addScheduledTask(() -> patternProvider.setStackList(message.nbt));
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        ByteBufUtils.writeTag(buf, nbt);
    }

    @Override
    public IMessage onMessage(final PktMEPatternProviderHandlerItems message, final MessageContext ctx) {
        if (ctx.side.isClient()) {
            processPacket(message);
        }
        return null;
    }

}
