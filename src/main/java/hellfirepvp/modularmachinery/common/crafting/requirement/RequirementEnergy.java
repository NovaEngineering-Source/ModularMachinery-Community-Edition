/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirement;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.jei.JEIComponentEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeEnergy;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import hellfirepvp.modularmachinery.common.util.IEnergyHandler;
import hellfirepvp.modularmachinery.common.util.IEnergyHandlerAsync;
import hellfirepvp.modularmachinery.common.util.IEnergyHandlerImpl;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementEnergy
 * Created by HellFirePvP
 * Date: 24.02.2018 / 12:26
 */
public class RequirementEnergy extends ComponentRequirement.PerTickParallelizable<Long, RequirementTypeEnergy>
    implements ComponentRequirement.Parallelizable, Asyncable {

    public final long requirementPerTick;

    public RequirementEnergy(IOType ioType, long requirementPerTick) {
        super(RequirementTypesMM.REQUIREMENT_ENERGY, ioType);
        this.requirementPerTick = requirementPerTick;
    }

    @Override
    public int getSortingWeight() {
        return PRIORITY_WEIGHT_ENERGY;
    }

    @Override
    public ComponentRequirement<Long, RequirementTypeEnergy> deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public ComponentRequirement<Long, RequirementTypeEnergy> deepCopyModified(List<RecipeModifier> modifiers) {
        long requirement = Math.round(RecipeModifier.applyModifiers(modifiers, this, (double) this.requirementPerTick, false));
        return new RequirementEnergy(this.actionType, requirement);
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        ResourceLocation compKey = this.requirementType.getRegistryName();
        return String.format("component.missing.%s.%s.%s",
            compKey.getNamespace(), compKey.getPath(), ioType.name().toLowerCase());
    }

    public long getRequiredEnergyPerTick() {
        return requirementPerTick;
    }

    @Override
    public JEIComponent<Long> provideJEIComponent() {
        return new JEIComponentEnergy(this);
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.component();
        return cmp.getComponentType().equals(ComponentTypesMM.COMPONENT_ENERGY) &&
            cmp.getContainerProvider() instanceof IEnergyHandler &&
            cmp instanceof MachineComponent.EnergyHatch &&
            cmp.ioType == this.actionType;
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        return doEnergyIO(components, context, 1F, true);
    }

    public CraftCheck doIOTick(List<ProcessingComponent<?>> components, RecipeCraftingContext context, final float durationMultiplier) {
        return doEnergyIO(components, context, durationMultiplier, false);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        List<ProcessingComponent<?>> list = new ArrayList<>();
        for (ProcessingComponent<?> component : components) {
            ProcessingComponent<Object> objectProcessingComponent = new ProcessingComponent<>(
                (MachineComponent<Object>) component.component(),
                new IEnergyHandlerImpl((IEnergyHandler) component.getProvidedComponent()),
                component.getTag());
            list.add(objectProcessingComponent);
        }
        return list;
    }

    @Override
    public int getMaxParallelism(List<ProcessingComponent<?>> components, RecipeCraftingContext context, int maxParallelism) {
        if (ignoreOutputCheck && actionType == IOType.OUTPUT) {
            return maxParallelism;
        }
        if (parallelizeUnaffected) {
            if (doEnergyIOInternal(components, context, 1, true) >= 1) {
                return maxParallelism;
            }
            return 0;
        }
        return (int) doEnergyIOInternal(components, context, maxParallelism, true);
    }

    private CraftCheck doEnergyIO(final List<ProcessingComponent<?>> components,
                                  final RecipeCraftingContext context,
                                  final float durationMultiplier,
                                  final boolean simulate) {
        float maxMultiplier = parallelism * durationMultiplier;
        float mul = doEnergyIOInternal(components, context, maxMultiplier, simulate);
        if (mul < maxMultiplier) {
            return switch (actionType) {
                case INPUT -> CraftCheck.failure("craftcheck.failure.energy.input");
                case OUTPUT -> {
                    if (ignoreOutputCheck) {
                        yield CraftCheck.success();
                    }
                    yield CraftCheck.failure("craftcheck.failure.energy.output.space");
                }
            };
        }
        return CraftCheck.success();
    }

    private float doEnergyIOInternal(final List<ProcessingComponent<?>> components,
                                     final RecipeCraftingContext context,
                                     final float maxMultiplier,
                                     final boolean simulate) {
        long required = (long) RecipeModifier.applyModifiers(context, this, (double) this.requirementPerTick, false);
        long maxRequired = (long) (required * maxMultiplier);

        List<IEnergyHandler> handlers;
        if (components.size() == 1) {
            handlers = Collections.singletonList((IEnergyHandler) components.get(0).getProvidedComponent());
        } else {
            handlers = Lists.transform(components, component -> component != null ? (IEnergyHandler) component.getProvidedComponent() : null);
        }

        float consumed = consumeOrInsertEnergy(handlers, maxRequired, required, maxMultiplier, true);
        if (simulate) {
            return consumed;
        }

        if (consumed >= maxMultiplier) {
            return consumeOrInsertEnergy(handlers, maxRequired, required, maxMultiplier, false);
        } else {
            return 0;
        }
    }

    private float consumeOrInsertEnergy(final List<IEnergyHandler> handlers,
                                        final double total,
                                        final long required,
                                        final float multiplier,
                                        final boolean simulate) {
        double maxRequired = total;
        switch (actionType) {
            case INPUT -> {
                for (final IEnergyHandler handler : handlers) {
                    long current = handler.getCurrentEnergy();
                    long toConsume = (long) Math.min(current, maxRequired);
                    if (!simulate) {
                        if (handler instanceof IEnergyHandlerAsync handlerAsync) {
                            if (handlerAsync.extractEnergy(toConsume)) {
                                maxRequired -= toConsume;
                            }
                        } else {
                            handler.setCurrentEnergy(current - toConsume);
                            maxRequired -= toConsume;
                        }
                    } else {
                        maxRequired -= toConsume;
                    }
                }
            }
            case OUTPUT -> {
                for (final IEnergyHandler handler : handlers) {
                    long remaining = handler.getRemainingCapacity();
                    long toReceive = (long) Math.min(remaining, maxRequired);
                    if (!simulate) {
                        if (handler instanceof IEnergyHandlerAsync handlerAsync) {
                            if (handlerAsync.receiveEnergy(toReceive)) {
                                maxRequired -= toReceive;
                            }
                        } else {
                            handler.setCurrentEnergy(handler.getCurrentEnergy() + toReceive);
                            maxRequired -= toReceive;
                        }
                    } else {
                        maxRequired -= toReceive;
                    }
                }
            }
        }

        if (maxRequired > 0) {
            return (float) ((total - maxRequired) / required);
        }
        return multiplier;
    }
}
