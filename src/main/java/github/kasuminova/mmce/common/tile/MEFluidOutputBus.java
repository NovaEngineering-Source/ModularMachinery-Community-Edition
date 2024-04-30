package github.kasuminova.mmce.common.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import github.kasuminova.mmce.common.tile.base.MEFluidBus;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MEFluidOutputBus extends MEFluidBus {

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(ItemsMM.meFluidOutputBus);
    }

    @Nullable
    @Override
    public MachineComponent<IFluidHandler> provideComponent() {
        return new MachineComponent.FluidHatch(IOType.OUTPUT) {
            @Override
            public IFluidHandler getContainerProvider() {
                return tanks;
            }
        };
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull final IGridNode node) {
        return new TickingRequest(5, 60, !hasFluid(), true);
    }

    @Nonnull
    @Override
    public synchronized TickRateModulation tickingRequest(@Nonnull final IGridNode node, final int ticksSinceLastCall) {
        if (!proxy.isActive()) {
            return TickRateModulation.IDLE;
        }

        boolean successAtLeastOnce = false;

        try {
            IMEMonitor<IAEFluidStack> inv = proxy.getStorage().getInventory(channel);
            for (final int slot : getNeedUpdateSlots()) {
                IAEFluidStack fluid = tanks.getFluidInSlot(slot);

                if (fluid == null) {
                    continue;
                }

                IAEFluidStack left = Platform.poweredInsert(proxy.getEnergy(), inv, fluid.copy(), source);

                if (left != null) {
                    if (fluid.getStackSize() != left.getStackSize()) {
                        successAtLeastOnce = true;
                    }
                } else {
                    successAtLeastOnce = true;
                }

                tanks.setFluidInSlot(slot, left);
            }
        } catch (GridAccessException e) {
            changedSlots.clear();
            return TickRateModulation.IDLE;
        }

        changedSlots.clear();
        return successAtLeastOnce ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    public boolean hasFluid() {
        for (int i = 0; i < tanks.getSlots(); i++) {
            IAEFluidStack stack = tanks.getFluidInSlot(i);
            if (stack != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void markNoUpdate() {
        if (proxy.isActive() && hasFluid()) {
            try {
                proxy.getTick().alertDevice(proxy.getNode());
            } catch (GridAccessException e) {
                // NO-OP
            }
        }

        super.markNoUpdate();
    }


}
