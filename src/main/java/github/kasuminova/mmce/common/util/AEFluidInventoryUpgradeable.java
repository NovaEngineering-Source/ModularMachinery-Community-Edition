package github.kasuminova.mmce.common.util;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AELog;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.Platform;
import github.kasuminova.mmce.common.util.concurrent.ReadWriteLockProvider;
import hellfirepvp.modularmachinery.common.util.HybridFluidUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * From: <a href="https://github.com/PrototypeTrousers/Applied-Energistics-2/blob/AE2-Omnifactory/src/main/java/appeng/fluids/util/AEFluidInventory.java">...</a>
 */
@SuppressWarnings("unchecked")
public class AEFluidInventoryUpgradeable implements IAEFluidTank, ReadWriteLockProvider, IOneToOneFluidHandler {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final AtomicReference<IAEFluidStack>[] fluids;
    private final IAEFluidInventory                handler;
    private       int                              capacity;
    private       IFluidTankProperties[]           props           = null;
    private       boolean                          oneFluidOneSlot = false;

    public AEFluidInventoryUpgradeable(final IAEFluidInventory handler, final int slots, final int capacity) {
        this.fluids = new AtomicReference[slots];
        for (int i = 0; i < slots; i++) {
            this.fluids[i] = new AtomicReference<>();
        }
        this.handler = handler;
        this.capacity = capacity;
    }

    public AEFluidInventoryUpgradeable(final IAEFluidInventory handler, final int slots) {
        this(handler, slots, Integer.MAX_VALUE);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        for (int slot = 0; slot < getSlots(); slot++) {
            this.onContentChanged(slot);
        }
    }

    @Override
    public boolean isOneFluidOneSlot() {
        return oneFluidOneSlot;
    }

    public void setOneFluidOneSlot(final boolean oneFluidOneSlot) {
        this.oneFluidOneSlot = oneFluidOneSlot;
    }

    @Override
    public void setFluidInSlot(final int slot, final IAEFluidStack fluid) {
        try {
            rwLock.writeLock().lock();
            if (slot >= 0 && slot < this.getSlots()) {
                if (Objects.equals(getFluid(slot), fluid)) {
                    if (fluid != null && fluid.getStackSize() != getFluid(slot).getStackSize()) {
                        getFluid(slot).setStackSize(fluid.getStackSize());
                        this.onContentChanged(slot);
                    }
                } else {
                    if (fluid == null) {
                        setFluid(slot, null);
                    } else {
                        IAEFluidStack newFluid = fluid.copy();
                        newFluid.setStackSize(fluid.getStackSize());
                        setFluid(slot, newFluid);
                    }

                    this.onContentChanged(slot);
                }
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private IAEFluidStack getFluid(final int slot) {
        return this.fluids[slot].get();
    }

    private void setFluid(final int slot, final IAEFluidStack fluid) {
        this.fluids[slot].set(fluid);
    }

    private void onContentChanged(final int slot) {
        if (this.handler != null && Platform.isServer()) {
            this.handler.onFluidInventoryChanged(this, slot);
        }
    }

    @Override
    public IAEFluidStack getFluidInSlot(final int slot) {
        try {
            rwLock.readLock().lock();
            if (slot >= 0 && slot < this.getSlots()) {
                return getFluid(slot);
            }
            return null;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int getSlots() {
        return this.fluids.length;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        if (this.props == null) {
            this.props = new IFluidTankProperties[this.getSlots()];
            for (int i = 0; i < this.getSlots(); ++i) {
                this.props[i] = new FluidTankPropertiesWrapper(i);
            }
        }
        return this.props;
    }

    public int fill(final int slot, final FluidStack resource, final boolean doFill) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }

        final IAEFluidStack fluid = getFluid(slot);

        if (fluid != null && !fluid.getFluidStack().isFluidEqual(resource)) {
            return 0;
        }

        int amountToStore = this.capacity;

        if (fluid != null) {
            amountToStore -= (int) fluid.getStackSize();
        }

        amountToStore = Math.min(amountToStore, resource.amount);

        if (doFill) {
            if (fluid == null) {
                this.setFluidInSlot(slot, AEFluidStack.fromFluidStack(resource).setStackSize(amountToStore));
            } else {
                fluid.setStackSize(fluid.getStackSize() + amountToStore);
                this.onContentChanged(slot);
            }
        }

        return amountToStore;
    }

    public FluidStack drain(final int slot, final FluidStack resource, final boolean doDrain) {
        final IAEFluidStack fluid = getFluid(slot);
        if (fluid == null || !fluid.getFluidStack().isFluidEqual(resource)) {
            return null;
        }
        return this.drain(slot, resource.amount, doDrain);
    }

    public FluidStack drain(final int slot, final int maxDrain, boolean doDrain) {
        final IAEFluidStack fluid = getFluid(slot);
        if (fluid == null || maxDrain <= 0) {
            return null;
        }

        int drained = maxDrain;
        if (fluid.getStackSize() < drained) {
            drained = (int) fluid.getStackSize();
        }

        FluidStack stack = new FluidStack(fluid.getFluid(), drained);
        if (doDrain) {
            fluid.setStackSize(fluid.getStackSize() - drained);
            if (fluid.getStackSize() <= 0) {
                setFluid(slot, null);
            }
            this.onContentChanged(slot);
        }
        return stack;
    }

    @Override
    public int fill(final FluidStack fluid, final boolean doFill) {
        if (fluid == null || fluid.amount <= 0) {
            return 0;
        }

        final FluidStack insert = fluid.copy();
        try {
            (doFill ? rwLock.writeLock() : rwLock.readLock()).lock();

            int found = HybridFluidUtils.findSlotWithFluid(this, getTankProperties(), insert);
            if (found >= 0) {
                return this.fill(found, insert, doFill);
            }

            int totalFillAmount = 0;
            for (int slot = 0; slot < this.getSlots(); ++slot) {
                int fillAmount = this.fill(slot, insert, doFill);
                totalFillAmount += fillAmount;
                insert.amount -= fillAmount;
                if (insert.amount <= 0) {
                    break;
                }
            }
            return totalFillAmount;
        } finally {
            (doFill ? rwLock.writeLock() : rwLock.readLock()).unlock();
        }
    }

    @Override
    public FluidStack drain(final FluidStack fluid, final boolean doDrain) {
        if (fluid == null || fluid.amount <= 0) {
            return null;
        }

        final FluidStack resource = fluid.copy();
        try {
            (doDrain ? rwLock.writeLock() : rwLock.readLock()).lock();

            FluidStack totalDrained = null;
            for (int slot = 0; slot < this.getSlots(); ++slot) {
                FluidStack drain = this.drain(slot, resource, doDrain);
                if (drain != null) {
                    if (totalDrained == null) {
                        totalDrained = drain;
                    } else {
                        totalDrained.amount += drain.amount;
                    }

                    resource.amount -= drain.amount;
                    if (resource.amount <= 0) {
                        break;
                    }
                }
            }
            return totalDrained;
        } finally {
            (doDrain ? rwLock.writeLock() : rwLock.readLock()).unlock();
        }
    }

    @Override
    public FluidStack drain(final int maxDrain, final boolean doDrain) {
        if (maxDrain == 0) {
            return null;
        }

        FluidStack totalDrained = null;
        int toDrain = maxDrain;
        try {
            (doDrain ? rwLock.writeLock() : rwLock.readLock()).lock();

            for (int slot = 0; slot < this.getSlots(); ++slot) {
                if (totalDrained == null) {
                    totalDrained = this.drain(slot, toDrain, doDrain);
                    if (totalDrained != null) {
                        toDrain -= totalDrained.amount;
                    }
                } else {
                    FluidStack copy = totalDrained.copy();
                    copy.amount = toDrain;
                    FluidStack drain = this.drain(slot, copy, doDrain);
                    if (drain != null) {
                        totalDrained.amount += drain.amount;
                        toDrain -= drain.amount;
                    }
                }

                if (toDrain <= 0) {
                    break;
                }
            }
            return totalDrained;
        } finally {
            (doDrain ? rwLock.writeLock() : rwLock.readLock()).unlock();
        }
    }

    public void writeToNBT(final NBTTagCompound data, final String name) {
        final NBTTagCompound c = new NBTTagCompound();
        this.writeToNBT(c);
        data.setTag(name, c);
    }

    private void writeToNBT(final NBTTagCompound target) {
        for (int x = 0; x < this.fluids.length; x++) {
            try {
                final NBTTagCompound c = new NBTTagCompound();

                if (getFluid(x) != null) {
                    getFluid(x).writeToNBT(c);
                }

                target.setTag("#" + x, c);
            } catch (final Exception ignored) {
            }
        }
    }

    public void readFromNBT(final NBTTagCompound data, final String name) {
        final NBTTagCompound c = data.getCompoundTag(name);
        if (!c.isEmpty()) {
            this.readFromNBT(c);
        }
    }

    private void readFromNBT(final NBTTagCompound target) {
        for (int x = 0; x < this.fluids.length; x++) {
            try {
                final NBTTagCompound c = target.getCompoundTag("#" + x);

                if (!c.isEmpty()) {
                    setFluid(x, AEFluidStack.fromNBT(c));
                }
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

    @Nonnull
    @Override
    public ReadWriteLock getRWLock() {
        return rwLock;
    }

    private class FluidTankPropertiesWrapper implements IFluidTankProperties {
        private final int slot;

        private FluidTankPropertiesWrapper(final int slot) {
            this.slot = slot;
        }

        @Override
        public FluidStack getContents() {
            IAEFluidStack fluid = getFluid(this.slot);
            return fluid == null ? null : fluid.getFluidStack();
        }

        @Override
        public int getCapacity() {
            IAEFluidStack fluid = getFluid(this.slot);
            if (fluid == null) {
                return capacity;
            } else {
                return Math.max(capacity, (int) fluid.getStackSize());
            }
        }

        @Override
        public boolean canFill() {
            return true;
        }

        @Override
        public boolean canDrain() {
            return true;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
            return true;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
            return fluidStack != null;
        }
    }
}
