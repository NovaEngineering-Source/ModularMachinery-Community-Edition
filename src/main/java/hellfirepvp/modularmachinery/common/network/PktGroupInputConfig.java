package hellfirepvp.modularmachinery.common.network;

import hellfirepvp.modularmachinery.common.container.ContainerGroupInputConfig;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktGroupInputConfig implements IMessage, IMessageHandler<PktGroupInputConfig, IMessage> {

    private byte i;
    private int id;
    private boolean isGroupInput;

    public PktGroupInputConfig() {

    }

    public PktGroupInputConfig(int id) {
        this.id = id;
        i = 0;
    }

    public PktGroupInputConfig(boolean isGroupInput) {
        this.isGroupInput = isGroupInput;
        i = 1;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        i = buf.readByte();
        if (i == 0) {
            id = buf.readInt();
        } else if (i == 1) {
            isGroupInput = buf.readBoolean();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(i);
        if (i == 0) {
            buf.writeInt(id);
        } else if (i == 1) {
            buf.writeBoolean(isGroupInput);
        }
    }

    @Override
    public IMessage onMessage(PktGroupInputConfig message, MessageContext ctx) {
        if (ctx.getServerHandler().player.openContainer instanceof ContainerGroupInputConfig c) {
            switch (message.i) {
                case 0 -> c.getMachine().setGroupId(message.id);
                case 1 -> c.getMachine().setGroupInput(message.isGroupInput);
            }
        }
        return null;
    }
}