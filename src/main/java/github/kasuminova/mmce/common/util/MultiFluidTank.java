package github.kasuminova.mmce.common.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("unused")
public class MultiFluidTank implements IFluidHandler {
    private final FluidStack[] contents;
    private final IFluidTankProperties[] props;
    private int capacity;

    public MultiFluidTank(int capacity, int tankCount) {
        this.capacity = capacity;
        this.contents = new FluidStack[tankCount];
        this.props = new IFluidTankProperties[tankCount];

        for (int i = 0; i < this.props.length; i++) {
            props[i] = new FluidTankProperties(i);
        }
    }

    public MultiFluidTank(MultiFluidTank from) {
        this(from.capacity, from.contents.length);

        for (int i = 0; i < contents.length; i++) {
            FluidStack stack = from.contents[i];
            if (stack != null) {
                contents[i] = stack.copy();
            }
        }
    }

    public MultiFluidTank(IFluidHandler from) {
        this(0, from.getTankProperties().length);

        IFluidTankProperties[] properties = from.getTankProperties();
        for (int i = 0; i < contents.length; i++) {
            IFluidTankProperties prop = properties[i];
            this.capacity = Math.max(capacity, prop.getCapacity());

            FluidStack stack = prop.getContents();
            if (stack != null) {
                contents[i] = stack.copy();
            }
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public MultiFluidTank setCapacity(final int capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return props;
    }

    @Override
    public synchronized int fill(final FluidStack fluid, final boolean doFill) {
        if (fluid == null || fluid.amount <= 0) {
            return 0;
        }

        final FluidStack insert = fluid.copy();

        int totalFillAmount = 0;
        for (int i = 0; i < contents.length; i++) {
            int filled = fill(i, insert, doFill);
            totalFillAmount += filled;

            if (insert.amount <= filled) {
                break;
            }

            insert.amount -= filled;
        }
        return totalFillAmount;
    }

    public int fill(final int slot, final FluidStack insert, final boolean doFill) {
        if (insert == null || insert.amount <= 0) {
            return 0;
        }

        FluidStack content = contents[slot];

        if (content == null) {
            if (capacity > insert.amount) {
                if (doFill) {
                    contents[slot] = insert;
                }
                return insert.amount;
            }
            if (doFill) {
                FluidStack copied = insert.copy();
                copied.amount = capacity;
                contents[slot] = copied;
            }
            return capacity;
        }

        if (!content.isFluidEqual(insert) || content.amount >= capacity) {
            return 0;
        }

        int maxCanFill = capacity - content.amount;

        if (maxCanFill > insert.amount) {
            if (doFill) {
                content.amount += insert.amount;
            }
            return insert.amount;
        }
        if (doFill) {
            content.amount = capacity;
        }
        return maxCanFill;
    }

    @Nullable
    @Override
    public synchronized FluidStack drain(final FluidStack resource, final boolean doDrain) {
        if (resource == null || resource.amount <= 0) {
            return null;
        }

        FluidStack res = resource.copy();

        int totalDrainAmount = 0;
        for (int i = 0; i < contents.length; i++) {
            FluidStack content = contents[i];
            if (content == null || !content.isFluidEqual(res)) {
                continue;
            }

            FluidStack drainedStack = drain(i, res.amount, doDrain);
            if (drainedStack == null) {
                continue;
            }

            int drained = drainedStack.amount;
            totalDrainAmount += drained;

            if (drained >= res.amount) {
                break;
            }
            res.amount -= drained;
        }

        FluidStack drained = res.copy();
        drained.amount = totalDrainAmount;
        return drained;
    }

    @Nullable
    public FluidStack drain(final int slot, final int maxDrain, final boolean doDrain) {
        if (maxDrain <= 0) {
            return null;
        }

        FluidStack content = contents[slot];
        if (content == null) {
            return null;
        }

        if (content.amount < maxDrain) {
            if (doDrain) {
                contents[slot] = null;
                return content;
            }
            return content.copy();
        }

        if (doDrain) {
            content.amount -= maxDrain;
        }

        FluidStack copied = content.copy();
        copied.amount = maxDrain;
        return copied;
    }

    @Nullable
    @Override
    public FluidStack drain(final int maxDrain, final boolean doDrain) {
        if (maxDrain <= 0) {
            return null;
        }

        FluidStack toDrain = Arrays.stream(contents)
                .filter(Objects::nonNull)
                .findFirst()
                .map(FluidStack::copy)
                .orElse(null);

        if (toDrain == null) {
            return null;
        }

        toDrain.amount = maxDrain;

        return drain(toDrain, doDrain);
    }

    public void readFromNBT(final NBTTagCompound compound, final String name) {
        NBTTagCompound tag = compound.getCompoundTag(name);
        Arrays.fill(contents, null);

        if (tag.isEmpty()) {
            return;
        }

        for (int i = 0; i < contents.length; i++) {
            NBTTagCompound t = tag.getCompoundTag("#" + i);
            if (t.isEmpty()) {
                contents[i] = null;
                continue;
            }

            contents[i] = FluidStack.loadFluidStackFromNBT(t);
        }
    }

    public void writeToNBT(final NBTTagCompound compound, final String name) {
        NBTTagCompound tag = new NBTTagCompound();

        for (int i = 0; i < contents.length; i++) {
            FluidStack stack = contents[i];
            if (stack == null) {
                continue;
            }

            NBTTagCompound t = new NBTTagCompound();
            stack.writeToNBT(t);

            tag.setTag("#" + i, t);
        }

        compound.setTag(name, tag);
    }

    public class FluidTankProperties implements IFluidTankProperties {

        private final int index;

        public FluidTankProperties(final int index) {
            this.index = index;
        }

        @Nullable
        @Override
        public FluidStack getContents() {
            return contents[index];
        }

        @Override
        public int getCapacity() {
            return capacity;
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
        public boolean canFillFluidType(final FluidStack fluidStack) {
            return true;
        }

        @Override
        public boolean canDrainFluidType(final FluidStack fluidStack) {
            return true;
        }
    }
}
