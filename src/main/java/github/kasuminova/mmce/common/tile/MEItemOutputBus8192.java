package github.kasuminova.mmce.common.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import github.kasuminova.mmce.common.tile.base.MEItemBus;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.locks.ReadWriteLock;

public class MEItemOutputBus8192 extends MEItemBus {

    private int lastProcessedSlot = -1;

    @Override
    public IOInventory buildInventory() {
        int size = 36;
        int[] slotIDs = new int[size];
        for (int i = 0; i < size; i++) {
            slotIDs[i] = i;
        }
        IOInventory inv = new IOInventory(this, new int[]{}, slotIDs);
        inv.setStackLimit(8192, slotIDs);
        inv.setListener(slot -> {
            synchronized (this) {
                changedSlots[slot] = true;
            }
        });
        return inv;
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(ItemsMM.meItemOutputBus8192);
    }

    @Nullable
    @Override
    public MachineComponent.ItemBus provideComponent() {
        return new MachineComponent.ItemBus(IOType.OUTPUT) {
            @Override
            public IOInventory getContainerProvider() {
                return inventory;
            }
        };
    }

    @Override
    public TickingRequest getTickingRequest(@Nonnull final IGridNode node) {
        return new TickingRequest(20, 200, !hasItem(), true);
    }

    @Override
    protected int[] getNeedUpdateSlots() {
        boolean[] localChangedSlots;
        synchronized (this) {
            if (changedSlots == null) return new int[0];
            localChangedSlots = changedSlots.clone();
        }

        int count = 0;
        for (boolean changed : localChangedSlots) if (changed) count++;
        if (count == 0) return new int[0];

        for (int i = 0; i < localChangedSlots.length; i++) {
            int checkSlot = (lastProcessedSlot + 1 + i) % localChangedSlots.length;
            if (localChangedSlots[checkSlot]) {
                lastProcessedSlot = checkSlot;
                return new int[]{checkSlot};
            }
        }
        return new int[0];
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull final IGridNode node, final int ticksSinceLastCall) {
        if (!proxy.isActive()) {
            return TickRateModulation.IDLE;
        }

        int[] needUpdateSlots = getNeedUpdateSlots();
        if (needUpdateSlots.length == 0) {
            return TickRateModulation.IDLE;
        }

        boolean successAtLeastOnce = false;
        ReadWriteLock rwLock = inventory.getRWLock();

        try {
            rwLock.writeLock().lock();
            inTick = true;
            IMEMonitor<IAEItemStack> aeInv = proxy.getStorage().getInventory(channel);

            for (final int slot : needUpdateSlots) {
                synchronized (this) {
                    if (changedSlots != null && slot < changedSlots.length) {
                        changedSlots[slot] = false;
                    }
                }

                if (failureCounter[slot] > 0) {
                    failureCounter[slot]--;
                }

                if (failureCounter[slot] > 20) {
                    continue;
                }

                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }

                ItemStack extracted = inventory.extractItem(slot, stack.getCount(), false);
                IAEItemStack aeStack = channel.createStack(extracted);
                if (aeStack == null) {
                    failureCounter[slot]++;
                    continue;
                }

                IAEItemStack left = Platform.poweredInsert(proxy.getEnergy(), aeInv, aeStack, source);
                if (left != null) {
                    inventory.setStackInSlot(slot, left.createItemStack());
                    if (aeStack.getStackSize() != left.getStackSize()) {
                        successAtLeastOnce = true;
                        failureCounter[slot] = 0;
                    } else {
                        failureCounter[slot]++;
                    }
                } else {
                    successAtLeastOnce = true;
                    failureCounter[slot] = 0;
                }
            }

            return successAtLeastOnce ? TickRateModulation.FASTER : TickRateModulation.IDLE;

        } catch (GridAccessException e) {
            synchronized (this) {
                if (changedSlots != null) {
                    changedSlots = new boolean[changedSlots.length];
                }
            }
            for (int i = 0; i < failureCounter.length; i++) {
                failureCounter[i] = 0;
            }
            return TickRateModulation.IDLE;
        } finally {
            inTick = false;
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void markNoUpdate() {
        if (hasChangedSlots()) {
            try {
                proxy.getTick().alertDevice(proxy.getNode());
            } catch (GridAccessException e) {
                // NO-OP
            }
        }
        super.markNoUpdate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (inventory != null) {
            int[] allSlots = new int[inventory.getSlots()];
            for (int i = 0; i < allSlots.length; i++) {
                allSlots[i] = i;
            }
            inventory.setStackLimit(8192, allSlots);
        }
    }
}