package github.kasuminova.mmce.common.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import github.kasuminova.mmce.common.tile.base.MEGasBus;
import github.kasuminova.mmce.common.util.IExtendedGasHandler;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class MEGasInputBus extends MEGasBus implements SettingsTransfer {

    private static final String CONFIG_TAG_KEY = "config";

    private final GasInventory config = new GasInventory(MEGasBus.TANK_SLOT_AMOUNT, this);

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(ItemsMM.meGasInputBus);
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);
        config.load(compound.getCompoundTag(CONFIG_TAG_KEY));
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setTag(CONFIG_TAG_KEY, config.save());
        upgrades.writeToNBT(compound, "upgrades");
    }

    public GasInventory getConfig() {
        return config;
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull final IGridNode node) {
        return new TickingRequest(10, 120, !needsUpdate(), true);
    }

    private boolean needsUpdate() {
        int capacity = tanks.getTanks()[0].getMaxGas();

        for (int slot = 0; slot < config.size(); slot++) {
            GasStack cfgStack = config.getGasStack(slot);
            GasStack invStack = tanks.getGasStack(slot);

            if (cfgStack == null) {
                if (invStack != null) {
                    return true;
                }
                continue;
            }

            if (invStack == null) {
                return true;
            }

            if (!cfgStack.isGasEqual(invStack) || invStack.amount != capacity) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull final IGridNode node, final int ticksSinceLastCall) {
        if (!proxy.isActive()) {
            return TickRateModulation.IDLE;
        }
        inTick = true;
        try {
            boolean successAtLeastOnce = false;

            IMEMonitor<IAEGasStack> inv = proxy.getStorage().getInventory(channel);
            int capacity = tanks.getTanks()[0].getMaxGas();

            synchronized (tanks) {
                for (final int slot : getNeedUpdateSlots()) {
                    changedSlots[slot] = false;
                    GasStack cfgStack = config.getGasStack(slot);
                    GasStack invStack = tanks.getGasStack(slot);

                    if (cfgStack == null) {
                        if (invStack == null) {
                            continue;
                        }
                        IAEGasStack left = insertStackToAE(inv, invStack);
                        tanks.setGas(slot, left == null ? null : left.getGasStack());
                        continue;
                    }

                    if (!cfgStack.isGasEqual(invStack)) {
                        if (invStack != null) {
                            IAEGasStack left = insertStackToAE(inv, invStack);
                            if (left != null) {
                                tanks.setGas(slot, left.getGasStack());
                                continue;
                            }
                        }

                        GasStack copied = cfgStack.copy();
                        copied.amount = capacity;
                        IAEGasStack stack = extractStackFromAE(inv, copied);
                        if (stack != null) {
                            tanks.setGas(slot, stack.getGasStack());
                            successAtLeastOnce = true;
                        }
                        continue;
                    }

                    // Because cfgStack is not null and cfgStack.isGasEqual(invStack) is true.
                    //noinspection DataFlowIssue
                    if (capacity == invStack.amount) {
                        continue;
                    }

                    if (capacity > invStack.amount) {
                        int countToReceive = capacity - invStack.amount;

                        GasStack copied = invStack.copy();
                        copied.amount = countToReceive;
                        IAEGasStack stack = extractStackFromAE(inv, copied);
                        if (stack != null) {
                            copied = invStack.copy();
                            copied.amount = (int) (invStack.amount + stack.getStackSize());
                            tanks.setGas(slot, copied);
                            successAtLeastOnce = true;
                        }
                    } else {
                        int countToExtract = invStack.amount - capacity;
                        GasStack copied = invStack.copy();
                        copied.amount = countToExtract;
                        IAEGasStack left = insertStackToAE(inv, copied);
                        if (left == null) {
                            copied.amount = invStack.amount - countToExtract;
                            tanks.setGas(slot, copied);
                        } else {
                            copied.amount = (int) ((invStack.amount - countToExtract) + left.getStackSize());
                            tanks.setGas(slot, copied);
                        }
                        successAtLeastOnce = true;
                    }
                }
            }
            inTick = false;
            return successAtLeastOnce ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
        } catch (GridAccessException e) {
            inTick = false;
            changedSlots = new boolean[TANK_SLOT_AMOUNT];
            return TickRateModulation.IDLE;
        }
    }

    private IAEGasStack extractStackFromAE(final IMEMonitor<IAEGasStack> inv, final GasStack stack) throws GridAccessException {
        return Platform.poweredExtraction(proxy.getEnergy(), inv, Objects.requireNonNull(AEGasStack.of(stack)), source);
    }

    private IAEGasStack insertStackToAE(final IMEMonitor<IAEGasStack> inv, final GasStack stack) throws GridAccessException {
        return Platform.poweredInsert(proxy.getEnergy(), inv, Objects.requireNonNull(AEGasStack.of(stack)), source);
    }

    @Override
    public boolean canGroupInput() {
        return true;
    }

    @Nullable
    @Override
    public MachineComponent<IExtendedGasHandler> provideComponent() {
        return new MachineComponent<>(IOType.INPUT) {
            @Override
            public ComponentType getComponentType() {
                return ComponentTypesMM.COMPONENT_GAS;
            }

            @Override
            public IExtendedGasHandler getContainerProvider() {
                return handler;
            }

            @Override
            public long getGroupID() {
                return getGroupId();
            }
        };
    }

    @Override
    public void markNoUpdate() {
        if (needsUpdate()) {
            try {
                proxy.getTick().alertDevice(proxy.getNode());
            } catch (GridAccessException e) {
                // NO-OP
            }
        }

        super.markNoUpdate();
    }

    @Override
    public NBTTagCompound downloadSettings() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(CONFIG_TAG_KEY, config.save());
        return tag;
    }

    @Override
    public void uploadSettings(NBTTagCompound settings) {
        config.load(settings.getCompoundTag(CONFIG_TAG_KEY));
        try {
            proxy.getTick().alertDevice(proxy.getNode());
        } catch (GridAccessException e) {
            ModularMachinery.log.warn("Error while uploading settings", e);
        }
    }
}
