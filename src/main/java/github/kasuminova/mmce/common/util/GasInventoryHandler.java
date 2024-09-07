package github.kasuminova.mmce.common.util;

import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.me.inventory.impl.GasInvHandler;
import mekanism.api.gas.GasStack;
import net.minecraft.util.EnumFacing;

public class GasInventoryHandler extends GasInvHandler implements IExtendedGasHandler {

    public GasInventoryHandler(final IGasInventory inv) {
        super(inv);
    }

    @Override
    public synchronized int receiveGas(final EnumFacing side, final GasStack stack, final boolean doTransfer) {
        return super.receiveGas(side, stack, doTransfer);
    }

    @Override
    public synchronized GasStack drawGas(final GasStack toDraw, final boolean doTransfer) {
        return drawGas(null, toDraw, doTransfer);
    }

}
