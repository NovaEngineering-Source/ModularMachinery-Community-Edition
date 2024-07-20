package github.kasuminova.mmce.common.tile.base;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.util.IConfigManager;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.me.inventory.IGasInventoryHost;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import github.kasuminova.mmce.common.util.GasInventoryHandler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.IntStream;

public abstract class MEGasBus extends MEMachineComponent implements
        IGasInventoryHost,
        IUpgradeableHost,
        IConfigManagerHost,
        IAEAppEngInventory,
        IGridTickable {

    public static final int TANK_SLOT_AMOUNT = 9;
    public static final int TANK_DEFAULT_CAPACITY = 8000;

    protected final IGasStorageChannel channel = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
    protected final ConfigManager cm = new ConfigManager(this);
    protected final UpgradeInventory upgrades;
    protected final GasInventory tanks;
    protected final GasInventoryHandler handler;
    protected boolean[] changedSlots;
    protected int fullCheckCounter = 5;

    protected boolean inTick = false;

    public MEGasBus() {
        this.tanks = new GasInventory(TANK_SLOT_AMOUNT, TANK_DEFAULT_CAPACITY, this);
        this.handler = new GasInventoryHandler(tanks);
        this.upgrades = new StackUpgradeInventory(proxy.getMachineRepresentation(), this, 5);
        this.changedSlots = new boolean[TANK_SLOT_AMOUNT];
    }

    protected synchronized int[] getNeedUpdateSlots() {
        fullCheckCounter++;
        if (fullCheckCounter >= 5) {
            fullCheckCounter = 0;
            return IntStream.range(0, tanks.size()).toArray();
        }
        IntList list = new IntArrayList();
        IntStream.range(0, changedSlots.length)
                .filter(i -> changedSlots[i])
                .forEach(list::add);
        return list.toArray(new int[0]);
    }

    public GasInventory getTanks() {
        return tanks;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.GAS_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        Capability<IGasHandler> cap = Capabilities.GAS_HANDLER_CAPABILITY;
        if (capability == cap) {
            return cap.cast(handler);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);

        upgrades.readFromNBT(compound, "upgrades");
        tanks.load(compound.getCompoundTag("tanks"));
        updateTankCapacity();
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        upgrades.writeToNBT(compound, "upgrades");
        compound.setTag("tanks", tanks.save());
    }

    // AE Compat

    @Override
    public IConfigManager getConfigManager() {
        return cm;
    }

    @Override
    public IItemHandlerModifiable getInventoryByName(final String name) {
        if (name.equals("upgrades")) {
            return this.upgrades;
        }

        return null;
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        if (upgrades == null) {
            return 0;
        }
        return upgrades.getInstalledUpgrades(u);
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {

    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        updateTankCapacity();
        markNoUpdateSync();
    }

    @Override
    public void onGasInventoryChanged(final IGasInventory iGasInventory, final int slot) {
        if (!inTick) {
            changedSlots[slot] = true;
        }
        markNoUpdateSync();
    }

    private void updateTankCapacity() {
        tanks.setCap(
                (int) (Math.pow(4, getInstalledUpgrades(Upgrades.CAPACITY) + 1) * (MEGasBus.TANK_DEFAULT_CAPACITY / 4)));
    }

    @Override
    public void saveChanges() {

    }

    @Override
    public TileEntity getTile() {
        return this;
    }
}
