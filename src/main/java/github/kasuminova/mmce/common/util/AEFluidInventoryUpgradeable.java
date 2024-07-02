package github.kasuminova.mmce.common.util;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AELog;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.Platform;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.Objects;

/**
 * From: <a href="https://github.com/PrototypeTrousers/Applied-Energistics-2/blob/AE2-Omnifactory/src/main/java/appeng/fluids/util/AEFluidInventory.java">...</a>
 */
public class AEFluidInventoryUpgradeable implements IAEFluidTank {
    private final IAEFluidStack[] fluids;
    private final IAEFluidInventory handler;
    private int capacity;
    private IFluidTankProperties[] props = null;
    private boolean oneFluidOneSlot = false;

    public AEFluidInventoryUpgradeable(final IAEFluidInventory handler, final int slots, final int capacity) {
        this.fluids = new IAEFluidStack[slots];
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

    public boolean isOneFluidOneSlot() {
        return oneFluidOneSlot;
    }

    public void setOneFluidOneSlot(final boolean oneFluidOneSlot) {
        this.oneFluidOneSlot = oneFluidOneSlot;
    }

    @Override
    public synchronized void setFluidInSlot(final int slot, final IAEFluidStack fluid) {
        if (slot >= 0 && slot < this.getSlots()) {
            if (Objects.equals(this.fluids[slot], fluid)) {
                if (fluid != null && fluid.getStackSize() != this.fluids[slot].getStackSize()) {
                    this.fluids[slot].setStackSize(fluid.getStackSize());
                    this.onContentChanged(slot);
                }
            } else {
                if (fluid == null) {
                    this.fluids[slot] = null;
                } else {
                    this.fluids[slot] = fluid.copy();
                    this.fluids[slot].setStackSize(fluid.getStackSize());
                }

                this.onContentChanged(slot);
            }
        }
    }

    private void onContentChanged(final int slot) {
        if (this.handler != null && Platform.isServer()) {
            this.handler.onFluidInventoryChanged(this, slot);
        }
    }

    @Override
    public IAEFluidStack getFluidInSlot(final int slot) {
        if (slot >= 0 && slot < this.getSlots()) {
            return this.fluids[slot];
        }
        return null;
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

        final IAEFluidStack fluid = this.fluids[slot];

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
        final IAEFluidStack fluid = this.fluids[slot];
        if (fluid == null || !fluid.getFluidStack().isFluidEqual(resource)) {
            return null;
        }
        return this.drain(slot, resource.amount, doDrain);
    }

    public FluidStack drain(final int slot, final int maxDrain, boolean doDrain) {
        final IAEFluidStack fluid = this.fluids[slot];
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
                this.fluids[slot] = null;
            }
            this.onContentChanged(slot);
        }
        return stack;
    }

    @Override
    public synchronized int fill(final FluidStack fluid, final boolean doFill) {
        if (fluid == null || fluid.amount <= 0) {
            return 0;
        }

        final FluidStack insert = fluid.copy();

        if (oneFluidOneSlot) {
            int found = -1;
            for (int i = 0; i < fluids.length; i++) {
                final IAEFluidStack fluidInSlot = this.fluids[i];
                if (fluidInSlot != null && fluidInSlot.getFluid() == insert.getFluid()) {
                    found = i;
                    break;
                }
            }
            if (found != -1) {
                return this.fill(found, insert, doFill);
            }
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
    }

    @Override
    public synchronized FluidStack drain(final FluidStack fluid, final boolean doDrain) {
        if (fluid == null || fluid.amount <= 0) {
            return null;
        }

        final FluidStack resource = fluid.copy();

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
    }

    @Override
    public FluidStack drain(final int maxDrain, final boolean doDrain) {
        if (maxDrain == 0) {
            return null;
        }

        FluidStack totalDrained = null;
        int toDrain = maxDrain;

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

                if (this.fluids[x] != null) {
                    this.fluids[x].writeToNBT(c);
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
                    this.fluids[x] = AEFluidStack.fromNBT(c);
                }
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

    private class FluidTankPropertiesWrapper implements IFluidTankProperties {
        private final int slot;

        private FluidTankPropertiesWrapper(final int slot) {
            this.slot = slot;
        }

        @Override
        public FluidStack getContents() {
            return fluids[this.slot] == null ? null : fluids[this.slot].getFluidStack();
        }

        @Override
        public int getCapacity() {
            IAEFluidStack fluid = fluids[this.slot];
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
