package kport.gugu_utils.network;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.modularmachinery.ModularMachinery;
import io.netty.buffer.ByteBuf;
import kport.gugu_utils.common.starlight.ContainerStarlightInputHatch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketStarlight implements IMessage {
    private IConstellation constellation;
    private int starlightAmount;
    private int starlightRequirement;

    public PacketStarlight() {

    }

    public PacketStarlight(int starlightAmount, int starlightRequirement, IConstellation constellation) {
        this.starlightAmount = starlightAmount;
        this.starlightRequirement = starlightRequirement;
        this.constellation = constellation;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        starlightAmount = buf.readInt();
        starlightRequirement = buf.readInt();
        try {
            constellation = ConstellationRegistry.getConstellationById(buf.readInt());
        } catch (IndexOutOfBoundsException e) {
            constellation = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(starlightAmount);
        buf.writeInt(starlightRequirement);
        buf.writeInt(ConstellationRegistry.getConstellationId(constellation));
    }

    public static class Handler implements IMessageHandler<PacketStarlight, IMessage> {

        @Override
        public IMessage onMessage(PacketStarlight message, MessageContext ctx) {
            ModularMachinery.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketStarlight message, MessageContext ctx) {
            EntityPlayer player = ModularMachinery.proxy.getClientPlayer();
            if (player.openContainer instanceof ContainerStarlightInputHatch) {
                ((ContainerStarlightInputHatch) player.openContainer).sync(message.starlightAmount,
                        message.starlightRequirement, message.constellation);
            }
        }
    }
}
