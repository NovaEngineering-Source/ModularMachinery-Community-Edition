package github.kasuminova.mmce.common.network;

import github.kasuminova.mmce.common.tile.MEItemOutputBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy.GuiType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktSwitchGuiMEOutputBus implements IMessage, IMessageHandler<PktSwitchGuiMEOutputBus, IMessage> {
    private BlockPos pos = BlockPos.ORIGIN;
    private int guiType = 0;

    public PktSwitchGuiMEOutputBus() {
    }

    public PktSwitchGuiMEOutputBus(final BlockPos pos, final int guiType) {
        this.pos = pos;
        this.guiType = guiType;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.guiType = buf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeInt(this.guiType);
    }

    @Override
    public IMessage onMessage(final PktSwitchGuiMEOutputBus message, final MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;

        player.getServerWorld().addScheduledTask(() -> {
            TileEntity te = player.world.getTileEntity(message.pos);

            if (!(te instanceof MEItemOutputBus)) {
                return;
            }

            player.closeScreen();

            if (message.guiType == 0) {
                player.openGui(
                        ModularMachinery.MODID,
                        GuiType.ME_ITEM_OUTPUT_BUS.ordinal(),
                        player.world,
                        message.pos.getX(),
                        message.pos.getY(),
                        message.pos.getZ()
                );
            } else if (message.guiType == 1) {
                player.openGui(
                        ModularMachinery.MODID,
                        GuiType.ME_ITEM_OUTPUT_BUS_STACK_SIZE.ordinal(),
                        player.world,
                        message.pos.getX(),
                        message.pos.getY(),
                        message.pos.getZ()
                );
            }
        });

        return null;
    }
}