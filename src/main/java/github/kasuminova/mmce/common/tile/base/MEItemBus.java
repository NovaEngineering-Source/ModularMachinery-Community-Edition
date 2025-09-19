package github.kasuminova.mmce.common.tile.base;

import appeng.api.AEApi;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.storage.channels.IItemStorageChannel;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.IntStream;

public abstract class MEItemBus extends MEMachineComponent implements IGridTickable {

    protected final IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);

    // TODO: May cause some machine fatal error, but why?
//    protected final BitSet changedSlots = new BitSet();

    protected IOInventory inventory         = buildInventory();
    protected boolean[]   changedSlots      = new boolean[inventory.getSlots()];
    protected int[]       failureCounter    = new int[inventory.getSlots()];
    protected long        lastFullCheckTick = 0;
    protected boolean     inTick            = false;

    public abstract IOInventory buildInventory();

    protected synchronized int[] getNeedUpdateSlots() {
        long current = world.getTotalWorldTime();
        if (lastFullCheckTick + 100 < current) {
            lastFullCheckTick = current;
            return IntStream.range(0, inventory.getSlots()).toArray();
        }
        IntList needUpdateSlots = new IntArrayList(changedSlots.length + 1);
        int bound = changedSlots.length;
        for (int i = 0; i < bound; i++) {
            if (changedSlots[i] && failureCounter[i] <= 0) {
                needUpdateSlots.add(i);
            }
        }
        return needUpdateSlots.toIntArray();
    }

    public IOInventory getInternalInventory() {
        return inventory;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        Capability<IItemHandler> cap = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
        if (capability == cap) {
            return cap.cast(inventory);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);

        if (compound.hasKey("inventory")) {
            readInventoryNBT(compound.getCompoundTag("inventory"));
        }
    }

    public void readInventoryNBT(final NBTTagCompound tag) {
        this.inventory = IOInventory.deserialize(this, tag);
        this.inventory.setListener(slot -> {
            synchronized (this) {
                changedSlots[slot] = true;
            }
        });

        int[] slotIDs = new int[inventory.getSlots()];
        for (int slotID = 0; slotID < slotIDs.length; slotID++) {
            slotIDs[slotID] = slotID;
        }
        inventory.setStackLimit(Integer.MAX_VALUE, slotIDs);
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setTag("inventory", inventory.writeNBT());
    }

    public boolean hasItem() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasChangedSlots() {
        for (final boolean changed : changedSlots) {
            if (changed) {
                return true;
            }
        }
        return false;
    }
}
