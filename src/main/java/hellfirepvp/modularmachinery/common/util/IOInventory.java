/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import github.kasuminova.mmce.client.util.ItemStackUtils;
import github.kasuminova.mmce.common.util.concurrent.ReadWriteLockProvider;
import hellfirepvp.modularmachinery.common.tiles.base.SelectiveUpdateTileEntity;
import hellfirepvp.modularmachinery.common.tiles.base.TileEntitySynchronized;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: IOInventory
 * Created by HellFirePvP
 * Date: 28.06.2017 / 17:42
 */
public class IOInventory extends IItemHandlerImpl implements ReadWriteLockProvider {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final TileEntitySynchronized owner;
    // TODO IntConsumer.
    private       Consumer<Integer>      listener = null;

    private IOInventory(TileEntitySynchronized owner) {
        this.owner = owner;
    }

    public IOInventory(TileEntitySynchronized owner, int[] inSlots, int[] outSlots) {
        this(owner, inSlots, outSlots, EnumFacing.VALUES);
    }

    public IOInventory(TileEntitySynchronized owner, int[] inSlots, int[] outSlots, EnumFacing... accessibleFrom) {
        super(inSlots, outSlots, accessibleFrom);
        this.owner = owner;
    }

    public static IOInventory deserialize(TileEntitySynchronized owner, NBTTagCompound tag) {
        IOInventory inv = new IOInventory(owner);
        inv.readNBT(tag);
        return inv;
    }

    public IOInventory setListener(Consumer<Integer> listener) {
        this.listener = listener;
        return this;
    }

    public TileEntitySynchronized getOwner() {
        return owner;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        try {
            rwLock.writeLock().lock();
            super.setStackInSlot(slot, stack);
            notifyOwner();
            if (listener != null) {
                listener.accept(slot);
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return stack;
        }
        try {
            (simulate ? rwLock.writeLock() : rwLock.readLock()).lock();
            ItemStack inserted = insertItemInternal(slot, stack, simulate);
            if (!simulate) {
                if (listener != null) {
                    listener.accept(slot);
                }
                notifyOwner();
            }
            return inserted;
        } finally {
            (simulate ? rwLock.writeLock() : rwLock.readLock()).unlock();
        }
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        try {
            (simulate ? rwLock.writeLock() : rwLock.readLock()).lock();
            ItemStack extracted = super.extractItem(slot, amount, simulate);
            if (!simulate) {
                if (listener != null) {
                    listener.accept(slot);
                }
                notifyOwner();
            }
            return extracted;
        } finally {
            (simulate ? rwLock.writeLock() : rwLock.readLock()).unlock();
        }
    }

    private void notifyOwner() {
        if (owner instanceof SelectiveUpdateTileEntity) {
            owner.markNoUpdateSync();
        } else {
            owner.markForUpdateSync();
        }
    }

    public NBTTagCompound writeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setIntArray("inSlots", this.inSlots);
        tag.setIntArray("outSlots", this.outSlots);
        tag.setIntArray("miscSlots", this.miscSlots);

        NBTTagList inv = new NBTTagList();
        for (int slot = 0; slot < inventory.length; slot++) {
            SlotStackHolder holder = this.inventory[slot];
            NBTTagCompound holderTag = new NBTTagCompound();
            ItemStack stack = holder.itemStack.get();

            holderTag.setInteger("holderId", slot);
            if (stack.isEmpty()) {
                holderTag.setBoolean("holderEmpty", true);
            } else {
                ItemStackUtils.writeNBTOversize(stack, holderTag);
            }

            inv.appendTag(holderTag);
        }
        tag.setTag("inventoryArray", inv);

        int[] sides = new int[accessibleSides.length];
        for (int i = 0; i < accessibleSides.length; i++) {
            EnumFacing side = accessibleSides[i];
            sides[i] = side.ordinal();
        }
        tag.setIntArray("sides", sides);
        return tag;
    }

    public void readNBT(NBTTagCompound tag) {
        this.inSlots = tag.getIntArray("inSlots");
        this.outSlots = tag.getIntArray("outSlots");
        this.miscSlots = tag.getIntArray("miscSlots");

        NBTTagList list = tag.getTagList("inventoryArray", Constants.NBT.TAG_COMPOUND);

        int tagCount = list.tagCount();
        this.inventory = new SlotStackHolder[tagCount];
        for (int i = 0; i < tagCount; i++) {
            NBTTagCompound holderTag = list.getCompoundTagAt(i);
            int slot = holderTag.getInteger("holderId");
            checkInventoryLength(slot);

            ItemStack stack = ItemStack.EMPTY;
            if (!holderTag.getBoolean("holderEmpty")) {
                stack = ItemStackUtils.readNBTOversize(holderTag);
            }

            SlotStackHolder holder = new SlotStackHolder(slot);
            holder.itemStack.set(stack);
            this.inventory[slot] = holder;
        }

        int[] sides = tag.getIntArray("sides");
        this.accessibleSides = new EnumFacing[sides.length];
        for (int index = 0; index < sides.length; index++) {
            final int facingIndex = sides[index];
            this.accessibleSides[index] = EnumFacing.values()[facingIndex];
        }
    }

    public int calcRedstoneFromInventory() {
        int i = 0;
        float f = 0.0F;
        for (int j = 0; j < getSlots(); ++j) {
            ItemStack itemstack = getStackInSlot(j);
            if (!itemstack.isEmpty()) {
                f += (float) itemstack.getCount() / (float) Math.min(getSlotLimit(j), itemstack.getMaxStackSize());
                ++i;
            }
        }
        f = f / (float) getSlots();
        return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
    }

    @Nonnull
    @Override
    public ReadWriteLock getRWLock() {
        return rwLock;
    }

}
