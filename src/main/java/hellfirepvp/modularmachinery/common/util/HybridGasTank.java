/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import github.kasuminova.mmce.common.util.IExtendedGasHandler;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: HybridGasTank
 * Created by HellFirePvP
 * Date: 26.08.2017 / 19:03
 */
public class HybridGasTank extends HybridTank implements IExtendedGasHandler {
    protected final GasTank gasTank;

    public HybridGasTank(int capacity) {
        super(capacity);
        gasTank = new GasTank(capacity);
    }

    @Override
    public synchronized void setFluid(@Nullable FluidStack fluid) {
        super.setFluid(fluid);
        if (getFluid() != null) {
            setGas(null);
        }
    }

    @Override
    public synchronized int fillInternal(FluidStack resource, boolean doFill) {
        if (getGas() != null && getGas().amount > 0) {
            return 0;
        }
        return super.fillInternal(resource, doFill);
    }

    @Nullable
    @Override
    public synchronized FluidStack drainInternal(int maxDrain, boolean doDrain) {
        if (getGas() != null && getGas().amount > 0) {
            return null;
        }
        return super.drainInternal(maxDrain, doDrain);
    }

    @Nullable
    @Override
    public synchronized FluidStack drainInternal(FluidStack resource, boolean doDrain) {
        if (getGas() != null && getGas().amount > 0) {
            return null;
        }
        return super.drainInternal(resource, doDrain);
    }

    @Nullable
    public synchronized GasStack getGas() {
        return this.gasTank.getGas();
    }

    public synchronized void setGas(@Nullable GasStack stack) {
        if (stack != null) {
            this.gasTank.setGas(stack.copy());
            setFluid(null);
        } else {
            this.gasTank.setGas(null);
        }
    }

    @Override
    public synchronized int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (stack == null || stack.amount <= 0) {
            return 0;
        }

        if (fluid != null && fluid.amount > 0) {
            return 0; //We don't collide with the internal fluids
        }

        int receive = gasTank.receive(stack, doTransfer);
        if (receive != 0 && doTransfer) {
            onContentsChanged();
        }
        return receive;
    }

    @Override
    public synchronized GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        if (getGas() == null || amount <= 0) {
            return null;
        }
        if (getFluid() != null && getFluid().amount > 0) {
            return null; //We don't collide with the internal fluids
        }
        if (!this.canDrawGas(side, null)) {
            return null;
        }

        GasStack drawn = this.gasTank.draw(amount, doTransfer);
        if (drawn != null && !doTransfer) {
            onContentsChanged();
        }
        return drawn;
    }

    @Override
    public GasStack drawGas(final GasStack toDraw, final boolean doTransfer) {
        return canDrawGas(null, toDraw.getGas()) ? drawGas(null, toDraw.amount, doTransfer) : null;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return canFill() && gasTank.canReceive(type);
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return canDrain() && gasTank.canDraw(type);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTank[]{gasTank};
    }

    public void readGasFromNBT(NBTTagCompound nbt) {
        NBTTagCompound subGas = nbt.getCompoundTag("gasTag");
        if (subGas.isEmpty()) {
            setGas(null);
            return;
        }

        // Old version tag support.
        if (!subGas.hasKey("Empty")) {
            setGas(GasStack.readFromNBT(subGas));
        } else {
            setGas(null);
        }
    }

    public void writeGasToNBT(NBTTagCompound nbt) {
        NBTTagCompound subGas = new NBTTagCompound();
        if (getGas() != null) {
            getGas().write(subGas);
        }
        nbt.setTag("gasTag", subGas);
    }

}
