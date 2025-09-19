package hellfirepvp.modularmachinery.common.util;

import com.google.common.collect.Lists;
import github.kasuminova.mmce.common.concurrent.Sync;
import github.kasuminova.mmce.common.util.IExtendedGasHandler;
import github.kasuminova.mmce.common.util.IOneToOneFluidHandler;
import github.kasuminova.mmce.common.util.MultiFluidTank;
import github.kasuminova.mmce.common.util.MultiGasTank;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HybridFluidUtils {

    public static long doSimulateDrainOrFill(final FluidStack drainOrFill, final List<IFluidHandler> fluidHandlers, final long maxDrainOrFill, final IOType actionType) {
        long totalIO = 0;

        FluidStack stack = drainOrFill.copy();
        for (final IFluidHandler handler : fluidHandlers) {
            stack.amount = maxDrainOrFill - totalIO >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) (maxDrainOrFill - totalIO);

            switch (actionType) {
                case INPUT -> {
                    FluidStack drained = handler.drain(stack, false);
                    if (drained != null) {
                        totalIO += drained.amount;
                    }
                }
                case OUTPUT -> totalIO += handler.fill(stack, false);
            }

            if (totalIO >= maxDrainOrFill) {
                break;
            }
        }

        return totalIO;
    }

    public static void doDrainOrFill(final FluidStack drainOrFill, long maxDrainOrFill, final List<IFluidHandler> fluidHandlers, final IOType actionType) {
        final long[] totalIO = {maxDrainOrFill};

        FluidStack stack = drainOrFill.copy();
        for (final IFluidHandler handler : fluidHandlers) {
            stack.amount = totalIO[0] >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) totalIO[0];

            Sync.executeSyncIfPresent(handler, () -> {
                switch (actionType) {
                    case INPUT -> {
                        FluidStack drained = handler.drain(stack, true);
                        if (drained != null) {
                            totalIO[0] -= drained.amount;
                        }
                    }
                    case OUTPUT -> totalIO[0] -= handler.fill(stack, true);
                }
            });

            if (totalIO[0] <= 0) {
                break;
            }
        }
    }

    @Optional.Method(modid = "mekanism")
    public static long doSimulateDrainOrFill(final GasStack drainOrFill, final List<IExtendedGasHandler> gasHandlers, final long maxDrainOrFill, final IOType actionType) {
        long totalIO = 0;

        GasStack stack = drainOrFill.copy();
        for (final IExtendedGasHandler handler : gasHandlers) {
            stack.amount = maxDrainOrFill - totalIO >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) (maxDrainOrFill - totalIO);

            switch (actionType) {
                case INPUT -> {
                    GasStack drawn = handler.drawGas(stack, false);
                    if (drawn != null) {
                        totalIO += drawn.amount;
                    }
                }
                case OUTPUT -> {
                    if (!handler.canReceiveGas(null, stack.getGas())) {
                        continue;
                    }
                    totalIO += handler.receiveGas(null, stack, false);
                }
            }

            if (totalIO >= maxDrainOrFill) {
                break;
            }
        }

        return totalIO;
    }

    @Optional.Method(modid = "mekanism")
    public static void doDrainOrFill(final GasStack drainOrFill, final long maxDrainOrFill, final List<IExtendedGasHandler> gasHandlers, final IOType actionType) {
        final long[] totalIO = {maxDrainOrFill};

        GasStack stack = drainOrFill.copy();

        for (final IExtendedGasHandler handler : gasHandlers) {
            stack.amount = totalIO[0] >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) totalIO[0];

            Sync.executeSyncIfPresent(handler, () -> {
                switch (actionType) {
                    case INPUT -> {
                        GasStack drawn = handler.drawGas(stack, true);
                        if (drawn != null) {
                            totalIO[0] -= drawn.amount;
                        }
                    }
                    case OUTPUT -> {
                        if (!handler.canReceiveGas(null, stack.getGas())) {
                            return;
                        }
                        totalIO[0] -= handler.receiveGas(null, stack, true);
                    }
                }
            });

            if (totalIO[0] <= 0) {
                break;
            }
        }
    }

    public static int findSlotWithFluid(final IOneToOneFluidHandler handler, final IFluidTankProperties[] props, final FluidStack fluid) {
        int found = -1;
        if (handler.isOneFluidOneSlot()) {
            for (int i = 0; i < props.length; i++) {
                FluidStack fluidInSlot = props[i].getContents();
                if (fluidInSlot != null && fluidInSlot.getFluid() == fluid.getFluid()) {
                    found = i;
                    break;
                }
            }
        }
        return found;
    }

    @Nonnull
    public static List<IFluidHandler> castFluidHandlerComponents(final List<ProcessingComponent<?>> components) {
        if (components.size() == 1) {
            return Collections.singletonList((IFluidHandler) components.get(0).getProvidedComponent());
        } else {
            return Lists.transform(components, component -> component != null ? (IFluidHandler) component.getProvidedComponent() : null);
        }
    }

    @Nonnull
    @Optional.Method(modid = "mekanism")
    public static List<IExtendedGasHandler> castGasHandlerComponents(final List<ProcessingComponent<?>> components) {
        List<IExtendedGasHandler> list = new LinkedList<>();
        for (ProcessingComponent<?> component : components) {
            Object providedComponent = component.getProvidedComponent();
            if (providedComponent instanceof final IExtendedGasHandler gasHandler) {
                list.add(gasHandler);
            }
        }
        return list;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static List<ProcessingComponent<?>> copyFluidHandlerComponents(final List<ProcessingComponent<?>> components) {
        List<ProcessingComponent<?>> list = new ArrayList<>();
        for (ProcessingComponent<?> component : components) {
            Object providedComponent = component.getProvidedComponent();
            if (providedComponent instanceof IFluidHandler) {
                ProcessingComponent<Object> objectProcessingComponent = new ProcessingComponent<>(
                    (MachineComponent<Object>) component.component(),
                    new MultiFluidTank((IFluidHandler) providedComponent),
                    component.getTag());
                list.add(objectProcessingComponent);
            }
        }
        return list;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static List<ProcessingComponent<?>> copyGasHandlerComponents(final List<ProcessingComponent<?>> components) {
        List<ProcessingComponent<?>> list = new ArrayList<>();
        for (ProcessingComponent<?> component : components) {
            Object providedComponent = component.getProvidedComponent();
            if (providedComponent instanceof IGasHandler) {
                ProcessingComponent<Object> objectProcessingComponent = new ProcessingComponent<>(
                    (MachineComponent<Object>) component.component(),
                    new MultiGasTank((IGasHandler) providedComponent),
                    component.getTag());
                list.add(objectProcessingComponent);
            }
        }
        return list;
    }

}
