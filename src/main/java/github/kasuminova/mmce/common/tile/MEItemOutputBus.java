package github.kasuminova.mmce.common.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import github.kasuminova.mmce.common.tile.base.MEItemBus;
import github.kasuminova.mmce.common.tile.SettingsTransfer;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.locks.ReadWriteLock;

public class MEItemOutputBus extends MEItemBus implements SettingsTransfer {

    // Stack size configuration field
    private int configuredStackSize = Integer.MAX_VALUE;

    @Override
    public IOInventory buildInventory() {
        int size = 36;

        int[] slotIDs = new int[size];
        for (int slotID = 0; slotID < slotIDs.length; slotID++) {
            slotIDs[slotID] = slotID;
        }
        IOInventory inv = new IOInventory(this, new int[]{}, slotIDs);
        inv.setStackLimit(this.configuredStackSize, slotIDs);
        inv.setListener(slot -> {
            synchronized (this) {
                changedSlots[slot] = true;
            }
        });
        return inv;
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(ItemsMM.meItemOutputBus);
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

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull final IGridNode node) {
        return new TickingRequest(5, 60, !hasItem(), true);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull final IGridNode node, final int ticksSinceLastCall) {
        if (!proxy.isActive()) {
            return TickRateModulation.IDLE;
        }

        int[] needUpdateSlots = getNeedUpdateSlots();
        if (needUpdateSlots.length == 0) {
            return TickRateModulation.SLOWER;
        }

        inTick = true;
        boolean successAtLeastOnce = false;

        ReadWriteLock rwLock = inventory.getRWLock();

        try {
            rwLock.writeLock().lock();

            IMEMonitor<IAEItemStack> inv = proxy.getStorage().getInventory(channel);
            for (final int slot : needUpdateSlots) {
                changedSlots[slot] = false;
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }

                ItemStack extracted = inventory.extractItem(slot, stack.getCount(), false);

                IAEItemStack aeStack = channel.createStack(extracted);
                if (aeStack == null) {
                    continue;
                }

                IAEItemStack left = Platform.poweredInsert(proxy.getEnergy(), inv, aeStack, source);

                if (left != null) {
                    inventory.setStackInSlot(slot, left.createItemStack());

                    if (aeStack.getStackSize() != left.getStackSize()) {
                        successAtLeastOnce = true;
                    }
                } else {
                    successAtLeastOnce = true;
                }
            }

            inTick = false;
            rwLock.writeLock().unlock();
            return successAtLeastOnce ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
        } catch (GridAccessException e) {
            inTick = false;
            changedSlots = new boolean[changedSlots.length];
            rwLock.writeLock().unlock();
            return TickRateModulation.IDLE;
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

    // ==================== Stack Size Configuration Methods ====================

    /**
     * Gets the currently configured stack size limit for this Output Bus
     * @return the configured stack size (minimum 1, maximum Integer.MAX_VALUE)
     */
    public int getConfiguredStackSize() {
        return this.configuredStackSize;
    }

    /**
     * Sets the stack size limit for all slots in this Output Bus
     * @param size the new stack size limit (will be clamped to minimum 1)
     */
    public void setConfiguredStackSize(int size) {
        // Clamp to valid range (minimum 1)
        this.configuredStackSize = Math.max(1, size);
        this.applyStackSizeToInventory();
        this.markForUpdate();
    }

    /**
     * Applies the configured stack size to the inventory
     */
    private void applyStackSizeToInventory() {
        if (this.inventory != null) {
            int[] slotIDs = new int[this.inventory.getSlots()];
            for (int slotID = 0; slotID < slotIDs.length; slotID++) {
                slotIDs[slotID] = slotID;
            }
            this.inventory.setStackLimit(this.configuredStackSize, slotIDs);
        }
    }

    // ==================== NBT Serialization ====================

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);

        // Read configured stack size from NBT
        if (compound.hasKey("configuredStackSize")) {
            this.configuredStackSize = compound.getInteger("configuredStackSize");
        }

        // Apply stack size after reading
        this.applyStackSizeToInventory();
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        // Write configured stack size to NBT
        compound.setInteger("configuredStackSize", this.configuredStackSize);
    }
    // ==================== SettingsTransfer Interface ====================    // ADD FROM HERE

    @Override
    public NBTTagCompound downloadSettings() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("configuredStackSize", this.configuredStackSize);
        return tag;
    }

    @Override
    public void uploadSettings(NBTTagCompound settings) {
        if (settings.hasKey("configuredStackSize")) {
            setConfiguredStackSize(settings.getInteger("configuredStackSize"));

            // Alert the ME network that settings changed
            try {
                proxy.getTick().alertDevice(proxy.getNode());
            } catch (GridAccessException e) {
                // NO-OP
            }
        }
    }                                                                           // ADD TO HERE
}