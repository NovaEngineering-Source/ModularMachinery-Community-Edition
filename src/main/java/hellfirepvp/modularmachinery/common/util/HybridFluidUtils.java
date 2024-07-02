package hellfirepvp.modularmachinery.common.util;

import com.google.common.collect.Lists;
import github.kasuminova.mmce.common.util.IExtendedGasHandler;
import github.kasuminova.mmce.common.util.MultiFluidTank;
import github.kasuminova.mmce.common.util.MultiGasTank;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
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
        long totalIO = maxDrainOrFill;

        FluidStack stack = drainOrFill.copy();
        for (final IFluidHandler handler : fluidHandlers) {
            stack.amount = totalIO >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) totalIO;

            switch (actionType) {
                case INPUT -> {
                    FluidStack drained = handler.drain(stack, true);
                    if (drained != null) {
                        totalIO -= drained.amount;
                    }
                }
                case OUTPUT -> totalIO -= handler.fill(stack, true);
            }

            if (totalIO <= 0) {
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
        long totalIO = maxDrainOrFill;

        GasStack stack = drainOrFill.copy();

        for (final IExtendedGasHandler handler : gasHandlers) {
            stack.amount = totalIO >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) totalIO;

            switch (actionType) {
                case INPUT -> {
                    GasStack drawn = handler.drawGas(stack, true);
                    if (drawn != null) {
                        totalIO += drawn.amount;
                    }
                }
                case OUTPUT -> {
                    if (!handler.canReceiveGas(null, stack.getGas())) {
                        continue;
                    }
                    totalIO += handler.receiveGas(null, stack, true);
                }
            }

            if (totalIO <= 0) {
                break;
            }
        }
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
            ProcessingComponent<Object> objectProcessingComponent = new ProcessingComponent<>(
                    (MachineComponent<Object>) component.component(),
                    new MultiFluidTank((IFluidHandler) component.getProvidedComponent()),
                    component.getTag());
            list.add(objectProcessingComponent);
        }
        return list;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    @Optional.Method(modid = "mekanism")
    public static List<ProcessingComponent<?>> copyGasHandlerComponents(final List<ProcessingComponent<?>> components) {
        List<ProcessingComponent<?>> list = new ArrayList<>();
        for (ProcessingComponent<?> component : components) {
            if (!(component.getProvidedComponent() instanceof IGasHandler)) {
                continue;
            }
            ProcessingComponent<Object> objectProcessingComponent = new ProcessingComponent<>(
                    (MachineComponent<Object>) component.component(),
                    new MultiGasTank((IGasHandler) component.getProvidedComponent()),
                    component.getTag());
            list.add(objectProcessingComponent);
        }
        return list;
    }

}
