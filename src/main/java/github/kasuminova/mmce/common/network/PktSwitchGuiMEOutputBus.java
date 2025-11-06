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

/**
 * Packet for switching between the main ME Item Output Bus GUI and the Stack Size configuration GUI.
 * Sent from client to server when the player clicks the wrench button or return button.
 *
 * GUI Type: 0 = Main Output Bus GUI, 1 = Stack Size Configuration GUI
 */
public class PktSwitchGuiMEOutputBus implements IMessage, IMessageHandler<PktSwitchGuiMEOutputBus, IMessage> {

    private BlockPos pos = BlockPos.ORIGIN;
    private int guiType = 0; // 0 = main GUI, 1 = stack size GUI

    /**
     * Default constructor required by Forge's packet system
     */
    public PktSwitchGuiMEOutputBus() {
    }

    /**
     * Constructor for creating a packet to send
     * @param pos The position of the Output Bus tile entity
     * @param guiType The GUI type to open (0 = main, 1 = stack size)
     */
    public PktSwitchGuiMEOutputBus(final BlockPos pos, final int guiType) {
        this.pos = pos;
        this.guiType = guiType;
    }

    /**
     * Reads packet data from the byte buffer (client -> server)
     */
    @Override
    public void fromBytes(final ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.guiType = buf.readInt();
    }

    /**
     * Writes packet data to the byte buffer (client -> server)
     */
    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeInt(this.guiType);
    }

    /**
     * Handles the packet on the server side
     */
    @Override
    public IMessage onMessage(final PktSwitchGuiMEOutputBus message, final MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;

        System.out.println("PktSwitchGuiMEOutputBus received! GUI Type: " + message.guiType + ", Pos: " + message.pos);

        // Schedule the action on the server thread
        player.getServerWorld().addScheduledTask(() -> {
            TileEntity te = player.world.getTileEntity(message.pos);

            System.out.println("Tile entity: " + (te != null ? te.getClass().getSimpleName() : "null"));

            // Verify it's an ME Item Output Bus
            if (!(te instanceof MEItemOutputBus)) {
                return;
            }

            // Close current GUI
            player.closeScreen();

            // Open the requested GUI
            if (message.guiType == 0) {
                // Open main Output Bus GUI
                player.openGui(
                        ModularMachinery.MODID,
                        GuiType.ME_ITEM_OUTPUT_BUS.ordinal(),
                        player.world,
                        message.pos.getX(),
                        message.pos.getY(),
                        message.pos.getZ()
                );
            } else if (message.guiType == 1) {

                System.out.println("Opening stack size GUI with ID: " + GuiType.ME_ITEM_OUTPUT_BUS_STACK_SIZE.ordinal());


                // Open stack size configuration GUI
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