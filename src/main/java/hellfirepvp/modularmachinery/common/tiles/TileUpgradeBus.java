package hellfirepvp.modularmachinery.common.tiles;

import github.kasuminova.mmce.common.capability.CapabilityUpgrade;
import github.kasuminova.mmce.common.upgrade.DynamicMachineUpgrade;
import github.kasuminova.mmce.common.upgrade.MachineUpgrade;
import github.kasuminova.mmce.common.upgrade.UpgradeType;
import github.kasuminova.mmce.common.upgrade.registry.RegistryUpgrade;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.SelectiveUpdateTileEntity;
import hellfirepvp.modularmachinery.common.tiles.base.TileEntityRestrictedTick;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TileUpgradeBus extends TileEntityRestrictedTick implements MachineComponentTile, SelectiveUpdateTileEntity {
    private final UpgradeBusProvider provider = new UpgradeBusProvider();
    private final Map<BlockPos, DynamicMachine> boundedMachine = new HashMap<>();

    private final Int2ObjectMap<List<DynamicMachineUpgrade>> foundDynamicUpgrades = new Int2ObjectOpenHashMap<>();
    private final Map<UpgradeType, MachineUpgrade> foundUpgrades = new HashMap<>();

    private NBTTagCompound upgradeCustomData = new NBTTagCompound();

    private IOInventory inventory = null;

    public TileUpgradeBus() {
    }

    public TileUpgradeBus(int maxUpgradeSlot) {
        int[] slots = new int[maxUpgradeSlot];
        for (int i = 0; i < maxUpgradeSlot; i++) {
            slots[i] = i;
        }
        this.inventory = new IOInventory(this, slots, new int[0]);
        this.inventory.setListener(this::onUpgradeInventoryChanged);
    }

    @Override
    public void doRestrictedTick() {
        if (world.isRemote || ticksExisted % 20 != 0) {
            return;
        }

        int prevSize = boundedMachine.size();
        Iterator<Map.Entry<BlockPos, DynamicMachine>> it = boundedMachine.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<BlockPos, DynamicMachine> entry = it.next();
            BlockPos key = entry.getKey();
            DynamicMachine machine = entry.getValue();

            TileEntity te = world.getTileEntity(key);
            if (!(te instanceof final TileMultiblockMachineController controller)) {
                it.remove();
                continue;
            }

            if (!machine.equals(controller.getFoundMachine())) {
                it.remove();
            }
        }
        if (prevSize != boundedMachine.size()) {
            markNoUpdate();
        }
    }

    public synchronized void onUpgradeInventoryChanged(int changedSlot) {
        foundUpgrades.clear();

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (stackInSlot.isEmpty()) {
                removeDynamicUpgrades(i);
                continue;
            }
            if (!RegistryUpgrade.supportsUpgrade(stackInSlot)) {
                removeDynamicUpgrades(i);
                continue;
            }
            CapabilityUpgrade capability = stackInSlot.getCapability(CapabilityUpgrade.MACHINE_UPGRADE_CAPABILITY, null);
            if (capability == null) {
                removeDynamicUpgrades(i);
                continue;
            }

            List<MachineUpgrade> upgrades = capability.getUpgrades();
            updateUpgrades(upgrades, stackInSlot);
            if (changedSlot == i || changedSlot == -1) {
                updateDynamicUpgrades(upgrades, stackInSlot, i);
            }
        }
    }

    public void removeDynamicUpgrades(int slotID) {
        List<DynamicMachineUpgrade> removed = foundDynamicUpgrades.remove(slotID);
        if (removed != null) {
            for (final DynamicMachineUpgrade dynamicUpgrade : removed) {
                dynamicUpgrade.invalidate();
            }
        }
    }

    public void updateDynamicUpgrades(final List<MachineUpgrade> upgrades, final ItemStack parentStack, final int slotID) {
        foundDynamicUpgrades.put(slotID, upgrades.stream()
                .filter(DynamicMachineUpgrade.class::isInstance)
                .map(DynamicMachineUpgrade.class::cast)
                .map(dynamicUpgrade -> dynamicUpgrade.setParentBus(this).setParentStack(parentStack).setBusInventoryIndex(slotID))
                .collect(Collectors.toCollection(LinkedList::new)));
    }

    public void updateUpgrades(final List<MachineUpgrade> upgrades, final ItemStack parentStack) {
        for (final MachineUpgrade upgrade : upgrades) {
            if (upgrade instanceof DynamicMachineUpgrade) {
                continue;
            }

            UpgradeType type = upgrade.getType();
            MachineUpgrade founded = foundUpgrades.get(type);
            if (founded != null) {
                founded.incrementStackSize(upgrade.getStackSize());
                continue;
            }
            upgrade.incrementStackSize(parentStack.getCount() - 1);

            foundUpgrades.put(type, upgrade.setParentBus(this));
        }
    }

    public IOInventory getInventory() {
        return inventory;
    }

    public Map<BlockPos, DynamicMachine> getBoundedMachine() {
        return boundedMachine;
    }

    @Nonnull
    @Override
    public UpgradeBusProvider provideComponent() {
        return provider;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) inventory;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);
        if (compound.hasKey("inv")) {
            inventory = IOInventory.deserialize(this, compound.getCompoundTag("inv"));
            inventory.setListener(this::onUpgradeInventoryChanged);
        } else {
            inventory.clear();
        }
        if (compound.hasKey("upgradeCustomData")) {
            upgradeCustomData = compound.getCompoundTag("upgradeCustomData");
        }

        boundedMachine.clear();
        if (compound.hasKey("boundedMachine", Constants.NBT.TAG_LIST)) {
            NBTTagList tagList = compound.getTagList("boundedMachine", Constants.NBT.TAG_COMPOUND);
            IntStream.range(0, tagList.tagCount()).mapToObj(tagList::getCompoundTagAt).forEach(tagCompound -> {
                BlockPos pos = BlockPos.fromLong(tagCompound.getLong("pos"));
                DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(tagCompound.getString("machine")));
                if (machine == null) {
                    return;
                }
                boundedMachine.put(pos, machine);
            });
        }

        onUpgradeInventoryChanged(-1);
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setTag("inv", inventory.writeNBT());
        compound.setTag("upgradeCustomData", upgradeCustomData);
        if (!boundedMachine.isEmpty()) {
            NBTTagList tagList = new NBTTagList();
            boundedMachine.forEach((pos, machine) -> {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setLong("pos", pos.toLong());
                tagCompound.setString("machine", machine.getRegistryName().toString());
                tagList.appendTag(tagCompound);
            });
            compound.setTag("boundedMachine", tagList);
        }
    }

    public class UpgradeBusProvider extends MachineComponent<UpgradeBusProvider> {

        public UpgradeBusProvider() {
            super(IOType.INPUT);
        }

        public void boundMachine(TileMultiblockMachineController controller) {
            BlockPos pos = controller.getPos();
            DynamicMachine foundMachine = controller.getFoundMachine();
            if (boundedMachine.containsKey(pos) || foundMachine == null) {
                return;
            }

            boundedMachine.put(pos, foundMachine);
            markNoUpdateSync();
        }

        public Map<BlockPos, DynamicMachine> getBoundedMachine() {
            return boundedMachine;
        }

        public Map<UpgradeType, List<MachineUpgrade>> getUpgrades(@Nullable TileMultiblockMachineController controller) {
            DynamicMachine foundMachine = controller == null ? null : controller.getFoundMachine();

            HashMap<UpgradeType, List<MachineUpgrade>> compatible = new HashMap<>();

            synchronized (TileUpgradeBus.this) {
                foundUpgrades.forEach((type, upgrade) -> {
                    if (foundMachine == null || type.isCompatible(foundMachine)) {
                        compatible.computeIfAbsent(type, v -> new LinkedList<>()).add(upgrade);
                    }
                });
                foundDynamicUpgrades.values().forEach((dynamicUpgrades) -> {
                    for (final DynamicMachineUpgrade dynamicUpgrade : dynamicUpgrades) {
                        UpgradeType type = dynamicUpgrade.getType();
                        if (foundMachine == null || type.isCompatible(foundMachine)) {
                            compatible.computeIfAbsent(type, v -> new LinkedList<>()).add(dynamicUpgrade);
                        }
                    }
                });
            }

            return compatible;
        }

        public NBTTagCompound getUpgradeCustomData(MachineUpgrade upgrade) {
            return upgradeCustomData.getCompoundTag(upgrade.getType().getName());
        }

        public void setUpgradeCustomData(MachineUpgrade upgrade, NBTTagCompound tagCompound) {
            upgradeCustomData.setTag(upgrade.getType().getName(), tagCompound);
            markNoUpdateSync();
        }

        public int size() {
            return inventory.getSlots();
        }

        @Override
        public ComponentType getComponentType() {
            return ComponentTypesMM.COMPONENT_UPGRADE_BUS;
        }

        @Override
        public UpgradeBusProvider getContainerProvider() {
            return this;
        }
    }
}
