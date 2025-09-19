/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import github.kasuminova.mmce.common.util.concurrent.ReadWriteLockProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: HybridTank
 * Created by HellFirePvP
 * Date: 26.08.2017 / 18:57
 */
@Optional.Interface(iface = "mekanism.api.gas.IGasHandler", modid = "mekanism")
public class HybridTank extends FluidTank implements ReadWriteLockProvider {

    protected final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public HybridTank(int capacity) {
        super(capacity);
    }

    @Nullable
    @Override
    public FluidStack getFluid() {
        try {
            rwLock.readLock().lock();
            return super.getFluid();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void setFluid(@Nullable FluidStack fluid) {
        try {
            rwLock.writeLock().lock();
            super.setFluid(fluid);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public int getFluidAmount() {
        try {
            rwLock.readLock().lock();
            return super.getFluidAmount();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        try {
            (doFill ? rwLock.writeLock() : rwLock.readLock()).lock();
            return super.fill(resource, doFill);
        } finally {
            (doFill ? rwLock.writeLock() : rwLock.readLock()).unlock();
        }
    }

    @Override
    public int fillInternal(FluidStack resource, boolean doFill) {
        try {
            (doFill ? rwLock.writeLock() : rwLock.readLock()).lock();
            return super.fillInternal(resource, doFill);
        } finally {
            (doFill ? rwLock.writeLock() : rwLock.readLock()).unlock();
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        try {
            (doDrain ? rwLock.writeLock() : rwLock.readLock()).lock();
            return super.drain(resource, doDrain);
        } finally {
            (doDrain ? rwLock.writeLock() : rwLock.readLock()).unlock();
        }
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        try {
            (doDrain ? rwLock.writeLock() : rwLock.readLock()).lock();
            return super.drain(maxDrain, doDrain);
        } finally {
            (doDrain ? rwLock.writeLock() : rwLock.readLock()).unlock();
        }
    }

    @Nullable
    @Override
    public FluidStack drainInternal(FluidStack resource, boolean doDrain) {
        try {
            (doDrain ? rwLock.writeLock() : rwLock.readLock()).lock();
            return super.drainInternal(resource, doDrain);
        } finally {
            (doDrain ? rwLock.writeLock() : rwLock.readLock()).unlock();
        }
    }

    @Nullable
    @Override
    public FluidStack drainInternal(int maxDrain, boolean doDrain) {
        try {
            (doDrain ? rwLock.writeLock() : rwLock.readLock()).lock();
            return super.drainInternal(maxDrain, doDrain);
        } finally {
            (doDrain ? rwLock.writeLock() : rwLock.readLock()).unlock();
        }
    }

    @Nonnull
    @Override
    public ReadWriteLock getRWLock() {
        return rwLock;
    }

}
