package github.kasuminova.mmce.common.util;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;

public interface IExtendedGasHandler extends IGasHandler {

    GasStack drawGas(GasStack toDraw, boolean doTransfer);

}
