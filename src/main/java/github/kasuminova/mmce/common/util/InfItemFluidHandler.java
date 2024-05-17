package github.kasuminova.mmce.common.util;

import hellfirepvp.modularmachinery.common.util.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class InfItemFluidHandler implements IItemHandlerModifiable, IFluidHandler {

    protected final List<ItemStack> itemStackList = new ObjectArrayList<>();
    protected final List<FluidStack> fluidStackList = new ObjectArrayList<>();

    protected volatile IItemHandlerModifiable subItemHandler = null;
    protected volatile IFluidHandler subFluidHandler = null;

    protected volatile IntConsumer onItemChanged = null;
    protected volatile IntConsumer onFluidChanged = null;

    public InfItemFluidHandler() {
    }

    public InfItemFluidHandler(final IItemHandlerModifiable subItemHandler) {
        this.subItemHandler = subItemHandler;
    }

    public InfItemFluidHandler(final IFluidHandler subFluidHandler) {
        this.subFluidHandler = subFluidHandler;
    }

    public InfItemFluidHandler(final IItemHandlerModifiable subItemHandler, final IFluidHandler subFluidHandler) {
        this.subItemHandler = subItemHandler;
        this.subFluidHandler = subFluidHandler;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        if (subFluidHandler != null) {
            return Stream.concat(
                    fluidStackList.stream().map(fluidStack -> new FluidTankProperties(fluidStack, Integer.MAX_VALUE)),
                    Arrays.stream(subFluidHandler.getTankProperties())
            ).toArray(IFluidTankProperties[]::new);
        }
        return fluidStackList.stream()
                .map(fluidStack -> new FluidTankProperties(fluidStack, Integer.MAX_VALUE))
                .toArray(IFluidTankProperties[]::new);
    }

    @Override
    public synchronized int fill(final FluidStack resource, final boolean doFill) {
        if (resource == null) {
            return 0;
        }

        int toFill = resource.amount;

        for (int i = 0; i < fluidStackList.size(); i++) {
            final FluidStack stackInSlot = fluidStackList.get(i);
            if (stackInSlot != null && stackInSlot.isFluidEqual(resource)) {
                int maxCanFill = Math.min(toFill, Integer.MAX_VALUE - stackInSlot.amount);
                if (doFill) {
                    stackInSlot.amount += maxCanFill;
                    if (onFluidChanged != null) {
                        onFluidChanged.accept(i);
                    }
                }
                toFill -= maxCanFill;

                if (toFill <= 0) {
                    break;
                }
            }
        }

        if (toFill > 0 && doFill) {
            fluidStackList.add(new FluidStack(resource, toFill));
            if (onFluidChanged != null) {
                onFluidChanged.accept(fluidStackList.size() - 1);
            }
        }

        return resource.amount;
    }

    @Nullable
    @Override
    public synchronized FluidStack drain(final FluidStack resource, final boolean doDrain) {
        if (resource == null) {
            return null;
        }

        if (subFluidHandler != null) {
            FluidStack drained = subFluidHandler.drain(resource, doDrain);
            if (drained != null) {
                return drained;
            }
        }

        for (int i = 0; i < fluidStackList.size(); i++) {
            final FluidStack stackInSlot = fluidStackList.get(i);
            if (stackInSlot != null && stackInSlot.isFluidEqual(resource)) {
                int maxCanDrain = Math.min(stackInSlot.amount, resource.amount);

                if (doDrain) {
                    stackInSlot.amount -= maxCanDrain;
                    if (stackInSlot.amount <= 0) {
                        fluidStackList.set(i, null);
                        if (onFluidChanged != null) {
                            onFluidChanged.accept(i);
                        }
                    }
                }

                return new FluidStack(resource, maxCanDrain);
            }
        }

        return null;
    }

    @Nullable
    @Override
    public synchronized FluidStack drain(final int maxDrain, final boolean doDrain) {
        if (subFluidHandler != null) {
            FluidStack drained = subFluidHandler.drain(maxDrain, doDrain);
            if (drained != null) {
                return drained;
            }
        }

        for (int i = 0; i < fluidStackList.size(); i++) {
            final FluidStack stackInSlot = fluidStackList.get(i);
            if (stackInSlot != null) {
                int maxCanDrain = Math.min(stackInSlot.amount, maxDrain);

                if (doDrain) {
                    stackInSlot.amount -= maxCanDrain;
                    if (stackInSlot.amount <= 0) {
                        fluidStackList.set(i, null);
                        if (onFluidChanged != null) {
                            onFluidChanged.accept(i);
                        }
                    }
                }

                return new FluidStack(stackInSlot, maxCanDrain);
            }
        }
        return null;
    }

    @Override
    public synchronized void setStackInSlot(final int slot, @Nonnull final ItemStack stack) {
        int trueSlot = slot;
        if (subItemHandler != null) {
            if (slot < subItemHandler.getSlots()) {
                subItemHandler.setStackInSlot(slot, stack);
                return;
            }
            trueSlot -= (subItemHandler.getSlots());
        }

        if (trueSlot >= itemStackList.size()) {
            itemStackList.add(stack);
        } else {
            itemStackList.set(trueSlot, stack);
        }
        if (onItemChanged != null) {
            onItemChanged.accept(trueSlot);
        }
    }

    @Override
    public int getSlots() {
        return itemStackList.size() + (subItemHandler != null ? subItemHandler.getSlots() : 0);
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(final int slot) {
        int trueSlot = slot;

        if (subItemHandler != null) {
            if (slot < subItemHandler.getSlots()) {
                return subItemHandler.getStackInSlot(slot);
            }
            trueSlot -= (subItemHandler.getSlots());
        }

        if (trueSlot >= itemStackList.size()) {
            return ItemStack.EMPTY;
        }
        return itemStackList.get(trueSlot);
    }

    @Nonnull
    @Override
    public synchronized ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
        if (slot >= itemStackList.size()) {
            return stack;
        }
        ItemStack stackInSlot = itemStackList.get(slot);
        if (stackInSlot.isEmpty()) {
            if (!simulate) {
                itemStackList.set(slot, stack.copy());
                if (onItemChanged != null) {
                    onItemChanged.accept(slot);
                }
            }
            return ItemStack.EMPTY;
        } else if (stackInSlot.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stackInSlot, stack)) {
            int maxToInsert = stack.getCount();
            int toInsert = Math.min(maxToInsert, Integer.MAX_VALUE - stackInSlot.getCount());

            if (!simulate) {
                stackInSlot.grow(toInsert);
                if (onItemChanged != null) {
                    onItemChanged.accept(slot);
                }
            }

            if (toInsert >= maxToInsert) {
                return ItemStack.EMPTY;
            }
            return ItemUtils.copyStackWithSize(stack, maxToInsert - toInsert);
        }
        return ItemStack.EMPTY;
    }

    public synchronized void appendItem(@Nonnull final ItemStack stack) {
        int toAppend = stack.getCount();

        for (int i = 0; i < itemStackList.size(); i++) {
            final ItemStack stackInSlot = itemStackList.get(i);
            if (stackInSlot.isEmpty()) {
                itemStackList.set(i, stack.copy());
                if (onItemChanged != null) {
                    onItemChanged.accept(i);
                }
                return;
            } else if (stackInSlot.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stackInSlot, stack)) {
                int maxToAppend = Math.min(stack.getCount(), Integer.MAX_VALUE - stackInSlot.getCount());
                stackInSlot.grow(maxToAppend);
                toAppend -= maxToAppend;
                if (toAppend <= 0) {
                    return;
                }
            }
        }

        if (toAppend > 0) {
            itemStackList.add(stack.copy());
            if (onItemChanged != null) {
                onItemChanged.accept(itemStackList.size() - 1);
            }
        }
    }

    @Nonnull
    @Override
    public synchronized ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        int trueSlot = slot;
        if (subItemHandler != null) {
            if (slot < subItemHandler.getSlots()) {
                return subItemHandler.extractItem(slot, amount, simulate);
            }
            trueSlot -= (subItemHandler.getSlots());
        }

        if (trueSlot >= itemStackList.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack stackInSlot = itemStackList.get(trueSlot);
        if (stackInSlot.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (amount >= stackInSlot.getCount()) {
            if (!simulate) {
                itemStackList.set(trueSlot, ItemStack.EMPTY);
                if (onItemChanged != null) {
                    onItemChanged.accept(trueSlot);
                }
            }
            return stackInSlot;
        } else {
            if (!simulate) {
                stackInSlot.shrink(amount);
                if (onItemChanged != null) {
                    onItemChanged.accept(trueSlot);
                }
            }
            return ItemUtils.copyStackWithSize(stackInSlot, amount);
        }
    }

    @Override
    public int getSlotLimit(final int slot) {
        if (subItemHandler != null) {
            if (slot < subItemHandler.getSlots()) {
                return subItemHandler.getSlotLimit(slot);
            }
        }
        return Integer.MAX_VALUE;
    }

    public boolean isEmpty() {
        return itemStackList.stream().allMatch(ItemStack::isEmpty) && fluidStackList.stream().allMatch(Objects::isNull);
    }

    public List<ItemStack> getItemStackList() {
        return itemStackList;
    }

    public List<FluidStack> getFluidStackList() {
        return fluidStackList;
    }

    public void setOnItemChanged(final IntConsumer onItemChanged) {
        this.onItemChanged = onItemChanged;
    }

    public void setOnFluidChanged(final IntConsumer onFluidChanged) {
        this.onFluidChanged = onFluidChanged;
    }

    public IItemHandlerModifiable getSubItemHandler() {
        return subItemHandler;
    }

    public void setSubItemHandler(final IItemHandlerModifiable subItemHandler) {
        this.subItemHandler = subItemHandler;
    }

    public IFluidHandler getSubFluidHandler() {
        return subFluidHandler;
    }

    public void setSubFluidHandler(final IFluidHandler subFluidHandler) {
        this.subFluidHandler = subFluidHandler;
    }

    public void writeToNBT(final NBTTagCompound tag, final String subTagName) {
        NBTTagCompound subTag = new NBTTagCompound();
        final NBTTagList fluidList = new NBTTagList();
        fluidStackList.stream()
                .filter(Objects::nonNull)
                .map(fluidStack -> fluidStack.writeToNBT(new NBTTagCompound()))
                .forEach(fluidList::appendTag);
        subTag.setTag("Fluids", fluidList);

        final NBTTagList itemList = new NBTTagList();
        itemStackList.stream()
                .filter(itemStack -> !itemStack.isEmpty())
                .map(itemStack -> itemStack.writeToNBT(new NBTTagCompound()))
                .forEach(itemList::appendTag);
        subTag.setTag("Items", itemList);

        tag.setTag(subTagName, subTag);
    }

    public void readFromNBT(final NBTTagCompound tag, final String subTagName) {
        NBTTagCompound subTag = tag.getCompoundTag(subTagName);
        fluidStackList.clear();
        final NBTTagList fluidList = subTag.getTagList("Fluids", Constants.NBT.TAG_COMPOUND);
        IntStream.range(0, fluidList.tagCount())
                .mapToObj(fluidList::getCompoundTagAt)
                .map(FluidStack::loadFluidStackFromNBT)
                .filter(Objects::nonNull)
                .forEach(fluidStackList::add);

        itemStackList.clear();
        final NBTTagList itemList = subTag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        IntStream.range(0, itemList.tagCount())
                .mapToObj(itemList::getCompoundTagAt)
                .map(ItemStack::new)
                .filter(itemStack -> !itemStack.isEmpty())
                .forEach(itemStackList::add);
    }

}
