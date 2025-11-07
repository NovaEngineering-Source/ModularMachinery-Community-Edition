package github.kasuminova.mmce.common.network;

import github.kasuminova.mmce.common.tile.MEItemOutputBus;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet sent from client to server when the player changes the stack size value in the GUI.
 * Updates the tile entity's stack size configuration directly.
 */
public class PktMEOutputBusStackSizeChange implements IMessage, IMessageHandler<PktMEOutputBusStackSizeChange, IMessage> {

    private BlockPos pos = BlockPos.ORIGIN;
    private int stackSize = Integer.MAX_VALUE;

    /**
     * Default constructor required by Forge's packet system
     */
    public PktMEOutputBusStackSizeChange() {
    }

    /**
     * Constructor for creating a packet to send
     * @param pos The position of the Output Bus tile entity
     * @param stackSize The new stack size value
     */
    public PktMEOutputBusStackSizeChange(final BlockPos pos, final int stackSize) {
        this.pos = pos;
        this.stackSize = stackSize;
    }

    /**
     * Reads packet data from the byte buffer (client -> server)
     */
    @Override
    public void fromBytes(final ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.stackSize = buf.readInt();
    }

    /**
     * Writes packet data to the byte buffer (client -> server)
     */
    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeInt(this.stackSize);
    }

    /**
     * Handles the packet on the server side.
     * Updates the tile entity's stack size configuration directly.
     */
    @Override
    public IMessage onMessage(final PktMEOutputBusStackSizeChange message, final MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;

        // Schedule the action on the server thread
        player.getServerWorld().addScheduledTask(() -> {
            // Get the tile entity at the specified position
            TileEntity te = player.world.getTileEntity(message.pos);

            // Verify it's an ME Item Output Bus
            if (!(te instanceof MEItemOutputBus)) {
                return;
            }

            MEItemOutputBus outputBus = (MEItemOutputBus) te;

            // Validate the stack size (minimum 1)
            int validatedStackSize = Math.max(1, message.stackSize);

            // Update the tile entity's stack size configuration
            outputBus.setConfiguredStackSize(validatedStackSize);

            // Mark the tile entity as dirty to ensure it saves
            outputBus.markDirty();
        });

        return null;
    }
}