package kport.gugu_utils.network;

import hellfirepvp.modularmachinery.ModularMachinery;
import io.netty.buffer.ByteBuf;
import kport.gugu_utils.common.tile.TileSparkManaOutputHatch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCollectorPostion implements IMessage {
    private int x;
    private int y;
    private int z;


    private int bx;
    private int by;
    private int bz;

    public PacketCollectorPostion() {

    }

    public PacketCollectorPostion(BlockPos pos, BlockPos data) {
        if (data != null) {
            this.x = data.getX();
            this.y = data.getY();
            this.z = data.getZ();
        } else {
            this.x = 0;
            this.y = -1;
            this.z = 0;
        }
        this.bx = pos.getX();
        this.by = pos.getY();
        this.bz = pos.getZ();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();

        bx = buf.readInt();
        by = buf.readInt();
        bz = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);

        buf.writeInt(bx);
        buf.writeInt(by);
        buf.writeInt(bz);
    }

    public BlockPos getData() {
        if (y < 0) {
            return null;
        }
        return new BlockPos(x, y, z);
    }

    public BlockPos getPos() {
        return new BlockPos(bx, by, bz);
    }

    public static class Handler implements IMessageHandler<PacketCollectorPostion, IMessage> {

        @Override
        public IMessage onMessage(PacketCollectorPostion message, MessageContext ctx) {
            ModularMachinery.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketCollectorPostion message, MessageContext ctx) {
            EntityPlayer player = ModularMachinery.proxy.getClientPlayer();
            TileEntity te = player.world.getTileEntity(message.getPos());
            if (te instanceof TileSparkManaOutputHatch) {
                ((TileSparkManaOutputHatch) te).sync(message.getData());
            }
        }
    }
}
