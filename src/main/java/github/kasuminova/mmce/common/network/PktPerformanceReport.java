package github.kasuminova.mmce.common.network;

import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PktPerformanceReport implements IMessage, IMessageHandler<PktPerformanceReport, IMessage> {
    private int  usedTime       = 0;
    private int  searchUsedTime = 0;
    private byte workMode       = 0;

    public PktPerformanceReport() {
    }

    public PktPerformanceReport(int usedTime, int searchUsedTime, TileMultiblockMachineController.WorkMode workMode) {
        this.usedTime = usedTime;
        this.searchUsedTime = searchUsedTime;
        this.workMode = (byte) workMode.ordinal();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        usedTime = buf.readInt();
        searchUsedTime = buf.readInt();
        workMode = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(usedTime);
        buf.writeInt(searchUsedTime);
        buf.writeByte(workMode);
    }

    @Override
    public IMessage onMessage(PktPerformanceReport message, MessageContext ctx) {
        TileMultiblockMachineController.usedTimeCache = message.usedTime;
        TileMultiblockMachineController.searchUsedTimeCache = message.searchUsedTime;
        TileMultiblockMachineController.workModeCache = TileMultiblockMachineController.WorkMode.values()[message.workMode];
        return null;
    }
}
