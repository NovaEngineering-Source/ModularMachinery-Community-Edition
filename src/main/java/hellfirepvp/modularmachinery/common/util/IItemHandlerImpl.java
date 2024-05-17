package hellfirepvp.modularmachinery.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class IItemHandlerImpl implements IItemHandlerModifiable {
    public static final int DEFAULT_SLOT_LIMIT = 64;

    protected int[] slotLimits = {64}; // Value not present means default, aka 64.
    protected SlotStackHolder[] inventory = {new SlotStackHolder(0)};

    public boolean allowAnySlots = false;
    public EnumFacing[] accessibleSides = {};
    protected int[] inSlots = new int[0], outSlots = new int[0], miscSlots = new int[0];

    protected IItemHandlerImpl() {
    }

    public IItemHandlerImpl(int[] inSlots, int[] outSlots) {
        this(inSlots, outSlots, EnumFacing.VALUES);
    }

    public IItemHandlerImpl(int[] inSlots, int[] outSlots, EnumFacing[] accessibleFrom) {
        this.inSlots = inSlots;
        this.outSlots = outSlots;

        int max = Math.max(getArrayMax(inSlots), getArrayMax(outSlots)) ;
        this.inventory = new SlotStackHolder[max + 1];
        this.slotLimits = new int[max + 1];

        Arrays.fill(this.slotLimits, DEFAULT_SLOT_LIMIT);
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = new SlotStackHolder(i);
        }

        this.accessibleSides = accessibleFrom;
        System.arraycopy(accessibleFrom, 0,  this.accessibleSides, 0, accessibleFrom.length);
    }

    public IItemHandlerImpl(IItemHandlerModifiable handler) {
        int slots = handler.getSlots();
        int[] inSlots = new int[slots];
        for (int i = 0; i < slots; i++) {
            inSlots[i] = i;
        }

        int[] outSlots = new int[slots];
        for (int i = 0; i < slots; i++) {
            outSlots[i] = i;
        }

        this.inSlots = inSlots;
        this.outSlots = outSlots;

        this.accessibleSides = EnumFacing.VALUES;
        this.inventory = new SlotStackHolder[slots];
        for (int i = 0; i < slots; i++) {
            SlotStackHolder holder = new SlotStackHolder(i);
            ItemStack stackInSlot = handler.getStackInSlot(i);
            if (stackInSlot.isEmpty()) {
                holder.itemStack = ItemStack.EMPTY;
            } else {
                holder.itemStack = stackInSlot.copy();
            }
            this.inventory[i] = holder;
        }
    }

    public IItemHandlerImpl copy() {
        IItemHandlerImpl copy = new IItemHandlerImpl(inSlots, outSlots, accessibleSides);
        for (int i = 0; i < inventory.length; i++) {
            copy.inventory[i] = inventory[i].copy();
        }
        System.arraycopy(slotLimits, 0, copy.slotLimits, 0, slotLimits.length);
        return copy;
    }

    public IItemHandlerImpl fastCopy() {
        IItemHandlerImpl copy = new IItemHandlerImpl();
        copy.inSlots = inSlots;
        copy.outSlots = outSlots;
        copy.miscSlots = miscSlots;

        copy.inventory = new SlotStackHolder[inventory.length];
        for (int i = 0; i < inventory.length; i++) {
            copy.inventory[i] = inventory[i].fastCopy();
        }

        copy.slotLimits = slotLimits;
        return copy;
    }

    protected static boolean arrayContains(int[] array, int i) {
        return Arrays.binarySearch(array, i) >= 0;
    }

    protected static boolean canMergeItemStacks(@Nonnull ItemStack stack, @Nonnull ItemStack other) {
        if (stack.isEmpty() || other.isEmpty() || !stack.isStackable() || !other.isStackable()) {
            return false;
        }
        return stack.isItemEqual(other) && ItemStack.areItemStackTagsEqual(stack, other);
    }

    public IItemHandlerImpl setMiscSlots(int... miscSlots) {
        this.miscSlots = miscSlots;

        int max = getArrayMax(miscSlots);
        checkSlotLimitsLength(max);
        checkInventoryLength(max);

        for (int slot : miscSlots) {
            this.inventory[slot] = new SlotStackHolder(slot);
        }
        return this;
    }

    public IItemHandlerImpl setStackLimit(int limit, int... slots) {
        int max = getArrayMax(slots);
        checkSlotLimitsLength(max);
        checkInventoryLength(max);

        for (int slot : slots) {
            this.slotLimits[slot] = limit;
        }
        return this;
    }

    public IItemHandlerModifiable asGUIAccess() {
        return new GuiAccess(this);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot <= -1 || slot >= inventory.length) {
            return;
        }
        this.inventory[slot].itemStack = stack;
    }

    @Override
    public int getSlots() {
        return inventory.length;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < 0 || slot >= slotLimits.length) {
            return DEFAULT_SLOT_LIMIT;
        }
        return slotLimits[slot];
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot > inventory.length) {
            return ItemStack.EMPTY;
        }
        SlotStackHolder holder = inventory[slot];
        return holder != null ? holder.itemStack : ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return stack;
        return insertItemInternal(slot, stack, simulate);
    }

    protected ItemStack insertItemInternal(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!allowAnySlots) {
            if (!arrayContains(inSlots, slot)) {
                return stack;
            }
        }

        IItemHandlerImpl.SlotStackHolder holder = this.inventory[slot];
        if (holder == null) {
            return stack; // Shouldn't happen anymore here tho
        }
        ItemStack toInsert = ItemUtils.copyStackWithSize(stack, stack.getCount());
        if (!holder.itemStack.isEmpty()) {
            ItemStack existing = ItemUtils.copyStackWithSize(holder.itemStack, holder.itemStack.getCount());
            int max = Math.min(existing.getMaxStackSize(), getSlotLimit(slot));
            if (existing.getCount() >= max || !canMergeItemStacks(existing, toInsert)) {
                return stack;
            }
            int movable = Math.min(max - existing.getCount(), stack.getCount());
            if (!simulate) {
                holder.itemStack.grow(movable);
            }
            if (movable >= stack.getCount()) {
                return ItemStack.EMPTY;
            } else {
                ItemStack copy = stack.copy();
                copy.shrink(movable);
                return copy;
            }
        } else {
            int max = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
            if (max >= stack.getCount()) {
                if (!simulate) {
                    holder.itemStack = stack.copy();
                }
                return ItemStack.EMPTY;
            } else {
                ItemStack copy = stack.copy();
                copy.setCount(max);
                if (!simulate) {
                    holder.itemStack = copy;
                }
                copy = stack.copy();
                copy.shrink(max);
                return copy;
            }
        }
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return extractItemInternal(slot, amount, simulate);
    }

    protected ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
        if (!allowAnySlots) {
            if (!arrayContains(outSlots, slot)) {
                return ItemStack.EMPTY;
            }
        }
        IItemHandlerImpl.SlotStackHolder holder = this.inventory[slot];
        if (holder == null) {
            return ItemStack.EMPTY; // Shouldn't happen anymore here tho
        }
        if (holder.itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack extract = ItemUtils.copyStackWithSize(holder.itemStack, Math.min(amount, holder.itemStack.getCount()));
        if (extract.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!simulate) {
            holder.itemStack = ItemUtils.copyStackWithSize(holder.itemStack, holder.itemStack.getCount() - extract.getCount());
        }
        return extract;
    }

    public void clear() {
        for (final SlotStackHolder holder : inventory) {
            holder.itemStack = ItemStack.EMPTY;
        }
    }

    public boolean hasCapability(EnumFacing facing) {
        return facing == null || Arrays.binarySearch(accessibleSides, facing) >= 0;
    }

    public IItemHandlerModifiable getCapability(EnumFacing facing) {
        if (hasCapability(facing)) {
            return this;
        }
        return null;
    }

    protected void checkSlotLimitsLength(final int max) {
        int required = max + 1;
        int invLength = slotLimits.length;
        if (required > invLength) {
            int[] tmp = new int[required];
            Arrays.fill(tmp, invLength, max, DEFAULT_SLOT_LIMIT);
            System.arraycopy(slotLimits, 0, tmp, 0, invLength);
            this.slotLimits = tmp;
        }
    }

    protected void checkInventoryLength(final int max) {
        int required = max + 1;
        int invLength = inventory.length;
        if (required > invLength) {
            SlotStackHolder[] tmp = new SlotStackHolder[required];
            for (int i = invLength; i < max; i++) {
                tmp[i] = new SlotStackHolder(i);
            }
            System.arraycopy(inventory, 0, tmp, 0, invLength);
            this.inventory = tmp;
        }
    }

    protected static int getArrayMax(final int[] slots) {
        int max = 0;
        for (final int slot : slots) {
            if (slot > max) {
                max = slot;
            }
        }
        return max;
    }

    public static class SlotStackHolder {

        public final int slotId;

        @Nonnull
        public volatile ItemStack itemStack = ItemStack.EMPTY;

        public SlotStackHolder(int slotId) {
            this.slotId = slotId;
        }

        public SlotStackHolder copy() {
            SlotStackHolder copied = new SlotStackHolder(slotId);
            if (!itemStack.isEmpty()) {
                copied.itemStack = itemStack.copy();
            }
            return copied;
        }

        public SlotStackHolder fastCopy() {
            SlotStackHolder copied = new SlotStackHolder(slotId);
            if (!itemStack.isEmpty()) {
                copied.itemStack = itemStack;
            }
            return copied;
        }
    }

    public static class GuiAccess implements IItemHandlerModifiable {

        private final IItemHandlerImpl inventory;

        public GuiAccess(IItemHandlerImpl inventory) {
            this.inventory = inventory;
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            inventory.setStackInSlot(slot, stack);
        }

        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            boolean allowPrev = inventory.allowAnySlots;
            inventory.allowAnySlots = true;

            ItemStack insert = inventory.insertItem(slot, stack, simulate);

            inventory.allowAnySlots = allowPrev;
            return insert;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            boolean allowPrev = inventory.allowAnySlots;
            inventory.allowAnySlots = true;

            ItemStack extract = inventory.extractItem(slot, Math.min(amount, 64), simulate);

            inventory.allowAnySlots = allowPrev;
            return extract;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }
    }
}
