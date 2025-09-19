package github.kasuminova.mmce.common.upgrade;

import hellfirepvp.modularmachinery.common.tiles.TileUpgradeBus;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class DynamicMachineUpgrade extends MachineUpgrade {
    protected int       busInventoryIndex = -1;
    protected boolean   valid             = true;
    protected ItemStack parentStack       = ItemStack.EMPTY;

    public DynamicMachineUpgrade(final UpgradeType type) {
        super(type);
    }

    public abstract void readItemNBT(NBTTagCompound tag);

    public abstract NBTTagCompound writeItemNBT();

    @Override
    public abstract DynamicMachineUpgrade copy(ItemStack owner);

    public ItemStack getParentStack() {
        return parentStack;
    }

    public DynamicMachineUpgrade setParentStack(final ItemStack parentStack) {
        this.parentStack = parentStack;
        return this;
    }

    @Override
    public DynamicMachineUpgrade setParentBus(final TileUpgradeBus parentBus) {
        return (DynamicMachineUpgrade) super.setParentBus(parentBus);
    }

    public int getBusInventoryIndex() {
        return busInventoryIndex;
    }

    public DynamicMachineUpgrade setBusInventoryIndex(final int busInventoryIndex) {
        this.busInventoryIndex = busInventoryIndex;
        return this;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isInvalid() {
        return !valid;
    }

    public void validate() {
        this.valid = true;
    }

    public void invalidate() {
        this.valid = false;
    }
}
