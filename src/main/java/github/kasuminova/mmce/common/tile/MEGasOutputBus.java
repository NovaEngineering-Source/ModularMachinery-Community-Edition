package github.kasuminova.mmce.common.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import github.kasuminova.mmce.common.tile.base.MEGasBus;
import github.kasuminova.mmce.common.util.IExtendedGasHandler;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class MEGasOutputBus extends MEGasBus {

    public MEGasOutputBus() {
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(ItemsMM.meGasOutputBus);
    }

    @Nullable
    @Override
    public MachineComponent<IExtendedGasHandler> provideComponent() {
        return new MachineComponent<>(IOType.OUTPUT) {
            @Override
            public ComponentType getComponentType() {
                return ComponentTypesMM.COMPONENT_GAS;
            }

            @Override
            public IExtendedGasHandler getContainerProvider() {
                return handler;
            }
        };
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull final IGridNode node) {
        return new TickingRequest(5, 60, !hasGas(), true);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull final IGridNode node, final int ticksSinceLastCall) {
        if (!proxy.isActive()) {
            return TickRateModulation.IDLE;
        }

        inTick = true;
        boolean successAtLeastOnce = false;

        try {
            IMEMonitor<IAEGasStack> inv = proxy.getStorage().getInventory(channel);
            synchronized (tanks) {
                for (final int slot : getNeedUpdateSlots()) {
                    changedSlots[slot] = false;
                    GasStack gas = tanks.getGasStack(slot);

                    if (gas == null) {
                        continue;
                    }

                    IAEGasStack left = Platform.poweredInsert(proxy.getEnergy(), inv, Objects.requireNonNull(AEGasStack.of(gas)), source);

                    if (left != null) {
                        if (gas.amount != left.getStackSize()) {
                            successAtLeastOnce = true;
                        }
                    } else {
                        successAtLeastOnce = true;
                    }

                    tanks.setGas(slot, left == null ? null : left.getGasStack());
                }
            }
        } catch (GridAccessException e) {
            inTick = false;
            changedSlots = new boolean[TANK_SLOT_AMOUNT];
            return TickRateModulation.IDLE;
        }

        inTick = false;
        return successAtLeastOnce ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    public boolean hasGas() {
        for (int i = 0; i < tanks.size(); i++) {
            GasStack stack = tanks.getGasStack(i);
            if (stack != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void markNoUpdate() {
        if (hasGas()) {
            try {
                proxy.getTick().alertDevice(proxy.getNode());
            } catch (GridAccessException e) {
                // NO-OP
            }
        }

        super.markNoUpdate();
    }

}
