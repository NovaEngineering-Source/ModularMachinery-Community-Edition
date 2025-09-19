package github.kasuminova.mmce.common.upgrade;

import crafttweaker.annotations.ZenRegister;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.helper.UpgradeEventHandlerCT;
import hellfirepvp.modularmachinery.common.tiles.TileUpgradeBus;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ZenRegister
@ZenClass("mods.modularmachinery.MachineUpgrade")
public abstract class MachineUpgrade {

    protected final UpgradeType                                type;
    protected final Map<Class<?>, List<UpgradeEventHandlerCT>> eventProcessor = new HashMap<>();

    protected TileUpgradeBus parentBus = null;

    protected int stackSize = 1;

    public MachineUpgrade(final UpgradeType type) {
        this.type = type;
    }

    public void readNBT(NBTTagCompound tag) {

    }

    public NBTTagCompound writeNBT() {
        return new NBTTagCompound();
    }

    public abstract MachineUpgrade copy(ItemStack owner);

    @SideOnly(Side.CLIENT)
    public abstract List<String> getDescriptions();

    public abstract List<String> getBusGUIDescriptions();

    public void addEventHandler(Class<?> eventClass, UpgradeEventHandlerCT handler) {
        eventProcessor.computeIfAbsent(eventClass, v -> new ArrayList<>()).add(handler);
    }

    public List<UpgradeEventHandlerCT> getEventHandlers(Class<?> eventClass) {
        return eventProcessor.getOrDefault(eventClass, Collections.emptyList());
    }

    public TileUpgradeBus getParentBus() {
        return parentBus;
    }

    public MachineUpgrade setParentBus(final TileUpgradeBus parentBus) {
        this.parentBus = parentBus;
        return this;
    }

    public int incrementStackSize(int increment) {
        return stackSize += increment;
    }

    public int decrementStackSize(int decrement) {
        return stackSize -= decrement;
    }

    @ZenGetter("stackSize")
    public int getStackSize() {
        return stackSize;
    }

    public UpgradeType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof final MachineUpgrade another)) {
            return false;
        }
        return type.equals((another).type);
    }
}
