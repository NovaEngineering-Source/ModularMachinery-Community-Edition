package github.kasuminova.mmce.common.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import github.kasuminova.mmce.common.tile.base.MEItemBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;

public class MEItemInputBus extends MEItemBus implements SettingsTransfer {

    private static final String CONFIG_TAG_KEY = "configInventory";
    private static final Map<ItemStack, IAEItemStack> AE_STACK_CACHE = new WeakHashMap<>();
    private IOInventory configInventory = buildConfigInventory();

    private int lastProcessedSlot = -1;

    @Override
    public IOInventory buildInventory() {
        int size = 16;
        int[] slotIDs = new int[size];
        for (int i = 0; i < size; i++) slotIDs[i] = i;

        IOInventory inv = new IOInventory(this, slotIDs, new int[]{});
        inv.setStackLimit(Integer.MAX_VALUE, slotIDs);
        inv.setListener(slot -> {
            synchronized (this) {
                changedSlots[slot] = true;
            }
        });
        return inv;
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(ItemsMM.meItemInputBus);
    }

    public IOInventory buildConfigInventory() {
        int size = 16;
        int[] slotIDs = new int[size];
        for (int i = 0; i < size; i++) slotIDs[i] = i;

        IOInventory inv = new IOInventory(this, new int[]{}, new int[]{});
        inv.setStackLimit(Integer.MAX_VALUE, slotIDs);
        inv.setMiscSlots(slotIDs);
        // ✅ CRITICAL: Reset failure counter when config changes (zero overhead)
        inv.setListener(slot -> {
            synchronized (this) {
                changedSlots[slot] = true;
                if (failureCounter != null && slot < failureCounter.length) {
                    failureCounter[slot] = 0;
                }
            }
        });
        return inv;
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);
        if (compound.hasKey(CONFIG_TAG_KEY)) {
            readConfigInventoryNBT(compound.getCompoundTag(CONFIG_TAG_KEY));
        }
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setTag(CONFIG_TAG_KEY, configInventory.writeNBT());
    }

    public IOInventory getConfigInventory() {
        return configInventory;
    }

    @Nullable
    @Override
    public MachineComponent.ItemBus provideComponent() {
        return new MachineComponent.ItemBus(IOType.INPUT) {
            @Override
            public IOInventory getContainerProvider() {
                return inventory;
            }
        };
    }

    // ✅ Optimal tick interval: responsive but efficient
    @Override
    public TickingRequest getTickingRequest(@Nonnull final IGridNode node) {
        return new TickingRequest(20, 200, false, true);
    }

    // ✅ Thread-safe: process only one changed slot per tick
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

                if (failureCounter[slot] > 20) {
                    continue;
                }

                ItemStack cfgStack = configInventory.getStackInSlot(slot);
                ItemStack invStack = inventory.getStackInSlot(slot);

                if (cfgStack.isEmpty()) {
                    if (!invStack.isEmpty()) {
                        ItemStack leftover = insertStackToAE(aeInv, invStack);
                        inventory.setStackInSlot(slot, leftover);
                        if (leftover.isEmpty()) {
                            successAtLeastOnce = true;
                            failureCounter[slot] = 0;
                        } else {
                            failureCounter[slot]++;
                        }
                    }
                    continue;
                }

                if (!ItemUtils.matchStacks(cfgStack, invStack)) {
                    if (invStack.isEmpty() || insertStackToAE(aeInv, invStack).isEmpty()) {
                        ItemStack pulled = extractStackFromAE(aeInv, cfgStack);
                        inventory.setStackInSlot(slot, pulled);
                        if (!pulled.isEmpty()) {
                            successAtLeastOnce = true;
                            failureCounter[slot] = 0;
                        } else {
                            failureCounter[slot]++;
                        }
                    }
                    continue;
                }

                if (cfgStack.getCount() == invStack.getCount()) {
                    continue;
                }

                if (cfgStack.getCount() > invStack.getCount()) {
                    int missing = cfgStack.getCount() - invStack.getCount();
                    ItemStack pulled = extractStackFromAE(aeInv, ItemUtils.copyStackWithSize(invStack, missing));
                    if (!pulled.isEmpty()) {
                        int newCount = invStack.getCount() + pulled.getCount();
                        inventory.setStackInSlot(slot, ItemUtils.copyStackWithSize(invStack, newCount));
                        successAtLeastOnce = true;
                        failureCounter[slot] = 0;
                    } else {
                        failureCounter[slot]++;
                    }
                } else {
                    int excess = invStack.getCount() - cfgStack.getCount();
                    ItemStack leftover = insertStackToAE(aeInv, ItemUtils.copyStackWithSize(invStack, excess));
                    if (leftover.isEmpty()) {
                        inventory.setStackInSlot(slot, ItemUtils.copyStackWithSize(invStack, cfgStack.getCount()));
                    } else {
                        inventory.setStackInSlot(slot, ItemUtils.copyStackWithSize(invStack, cfgStack.getCount() + leftover.getCount()));
                    }
                    successAtLeastOnce = true;
                    failureCounter[slot] = 0;
                }
            }

            return successAtLeastOnce ? TickRateModulation.FASTER : TickRateModulation.IDLE;

        } catch (GridAccessException e) {
            // ✅ Null-safe reset
            synchronized (this) {
                if (changedSlots != null) {
                    changedSlots = new boolean[changedSlots.length];
                }
            }
            return TickRateModulation.IDLE;
        } finally {
            inTick = false;
            rwLock.writeLock().unlock();
        }
    }

    private ItemStack extractStackFromAE(final IMEMonitor<IAEItemStack> inv, final ItemStack stack) throws GridAccessException {
        IAEItemStack aeStack = createStack(stack);
        if (aeStack == null) return ItemStack.EMPTY;
        IAEItemStack extracted = Platform.poweredExtraction(proxy.getEnergy(), inv, aeStack, source);
        return extracted == null ? ItemStack.EMPTY : extracted.createItemStack();
    }

    private ItemStack insertStackToAE(final IMEMonitor<IAEItemStack> inv, final ItemStack stack) throws GridAccessException {
        IAEItemStack aeStack = createStack(stack);
        if (aeStack == null) return stack;
        IAEItemStack left = Platform.poweredInsert(proxy.getEnergy(), inv, aeStack, source);
        return left == null ? ItemStack.EMPTY : left.createItemStack();
    }

    private IAEItemStack createStack(final ItemStack stack) {
        return AE_STACK_CACHE.computeIfAbsent(stack, v -> channel.createStack(stack));
    }

    @Override
    public void markNoUpdate() {
        if (hasChangedSlots()) {
            try {
                proxy.getTick().alertDevice(proxy.getNode());
            } catch (GridAccessException ignored) {}
        }
        super.markNoUpdate();
    }

    public boolean configInvHasItem() {
        for (int i = 0; i < configInventory.getSlots(); i++) {
            if (!configInventory.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void readConfigInventoryNBT(final NBTTagCompound compound) {
        configInventory = IOInventory.deserialize(this, compound);
        // ✅ Apply same listener for NBT loading
        configInventory.setListener(slot -> {
            synchronized (this) {
                changedSlots[slot] = true;
                if (failureCounter != null && slot < failureCounter.length) {
                    failureCounter[slot] = 0;
                }
            }
        });
        int[] slotIDs = new int[configInventory.getSlots()];
        for (int i = 0; i < slotIDs.length; i++) slotIDs[i] = i;
        configInventory.setStackLimit(Integer.MAX_VALUE, slotIDs);
    }

    @Override
    public NBTTagCompound downloadSettings() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(CONFIG_TAG_KEY, configInventory.writeNBT());
        return tag;
    }

    @Override
    public void uploadSettings(NBTTagCompound settings) {
        readConfigInventoryNBT(settings.getCompoundTag(CONFIG_TAG_KEY));
        try {
            proxy.getTick().alertDevice(proxy.getNode());
        } catch (GridAccessException e) {
            ModularMachinery.log.warn("Error while uploading settings", e);
        }
    }
}