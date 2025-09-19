package kport.modularmagic.common.crafting.requirement;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import kport.modularmagic.common.crafting.component.ComponentAspect;
import kport.modularmagic.common.crafting.helper.AspectProviderCopy;
import kport.modularmagic.common.crafting.requirement.types.ModularMagicRequirements;
import kport.modularmagic.common.crafting.requirement.types.RequirementTypeAspect;
import kport.modularmagic.common.integration.jei.component.JEIComponentAspect;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentAspectProvider;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.tiles.essentia.TileJarFillable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequirementAspect extends ComponentRequirement.MultiCompParallelizable<AspectList, RequirementTypeAspect>
    implements Asyncable, ComponentRequirement.Parallelizable {

    public int    amount;
    public Aspect aspect;

    public RequirementAspect(IOType actionType, int amount, Aspect aspect) {
        super((RequirementTypeAspect) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(ModularMagicRequirements.KEY_REQUIREMENT_ASPECT), actionType);

        this.amount = amount;
        this.aspect = aspect;
    }

    @Nonnull
    private static List<AspectProviderCopy> convertJars(final List<ProcessingComponent<?>> components) {
        if (components.size() == 1) {
            return Collections.singletonList((AspectProviderCopy) components.get(0).getProvidedComponent());
        } else {
            return Lists.transform(components, component -> component != null ? (AspectProviderCopy) component.getProvidedComponent() : null);
        }
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentAspect &&
            cmp instanceof MachineComponentAspectProvider &&
            cmp.ioType == getActionType();
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        List<ProcessingComponent<?>> list = new ArrayList<>();
        for (final ProcessingComponent<?> component : components) {
            list.add(new ProcessingComponent<>((
                MachineComponent<Object>) component.component(),
                new AspectProviderCopy(((AspectProviderCopy) component.providedComponent()).getOriginal()),
                component.tag())
            );
        }
        return list;
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        List<AspectProviderCopy> jars = convertJars(components);

        switch (getActionType()) {
            case INPUT -> {
                int taken = takeAll(jars, context, parallelism, true);
                if (taken < parallelism) {
                    return CraftCheck.failure("error.modularmachinery.requirement.aspect.less");
                }
            }
            case OUTPUT -> {
                if (!ignoreOutputCheck) {
                    int added = addAll(jars, context, parallelism, true);
                    if (added < parallelism) {
                        return CraftCheck.failure("error.modularmachinery.requirement.aspect.out");
                    }
                }
            }
        }

        return CraftCheck.success();
    }

    @Override
    public void startCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (actionType == IOType.INPUT) {
            takeAll(convertJars(components), context, parallelism, false);
        }
    }

    @Override
    public void finishCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (actionType == IOType.OUTPUT) {
            addAll(convertJars(components), context, parallelism, false);
        }
    }

    @Override
    public int getMaxParallelism(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxParallelism) {
        if (ignoreOutputCheck && actionType == IOType.OUTPUT) {
            return maxParallelism;
        }
        if (parallelizeUnaffected) {
            int max = switch (actionType) {
                case INPUT -> takeAll(convertJars(components), context, 1, true);
                case OUTPUT -> addAll(convertJars(components), context, 1, true);
            };
            if (max >= 1) {
                return maxParallelism;
            }
            return 0;
        }

        return switch (actionType) {
            case INPUT -> takeAll(convertJars(components), context, maxParallelism, true);
            case OUTPUT -> addAll(convertJars(components), context, maxParallelism, true);
        };
    }

    private int addAll(final List<AspectProviderCopy> jars,
                       final RecipeCraftingContext context,
                       final float maxMultiplier,
                       final boolean simulate) {
        int toAdd = Math.round(RecipeModifier.applyModifiers(context, this, (float) this.amount, false));
        int maxAdd = (int) (toAdd * maxMultiplier);

        int totalAdded = 0;
        for (final AspectProviderCopy jar : jars) {
            int notAdded;

            if (simulate) {
                notAdded = jar.addToContainer(aspect, maxAdd - totalAdded);
            } else {
                notAdded = jar.getOriginal().addToContainer(aspect, maxAdd - totalAdded);
            }

            totalAdded += maxAdd - totalAdded - notAdded;
            if (totalAdded >= maxAdd) {
                break;
            }
        }

        if (totalAdded < maxAdd) {
            return totalAdded / toAdd;
        }
        return totalAdded;
    }

    private int takeAll(final List<AspectProviderCopy> jars,
                        final RecipeCraftingContext context,
                        final float maxMultiplier,
                        final boolean simulate) {
        int toTake = Math.round(RecipeModifier.applyModifiers(context, this, (float) this.amount, false));
        int maxTake = (int) (toTake * maxMultiplier);

        int totalTaken = 0;
        for (final AspectProviderCopy jar : jars) {
            if (simulate) {
                int jarAmount = Math.min(jar.getAmount(), maxTake - totalTaken);
                if (jar.takeFromContainer(aspect, jarAmount)) {
                    totalTaken += jarAmount;
                }
            } else {
                TileJarFillable original = jar.getOriginal();
                int jarAmount = Math.min(original.amount, maxTake - totalTaken);
                if (original.takeFromContainer(aspect, jarAmount)) {
                    totalTaken += jarAmount;
                }
            }
            if (totalTaken >= maxTake) {
                break;
            }
        }

        if (totalTaken < maxTake) {
            return totalTaken / toTake;
        }
        return totalTaken;
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid";
    }

    @Override
    public RequirementAspect deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementAspect deepCopyModified(List<RecipeModifier> list) {
        int amount = Math.round(RecipeModifier.applyModifiers(list, this, this.amount, false));
        return new RequirementAspect(actionType, amount, aspect);
    }

    @Override
    public JEIComponentAspect provideJEIComponent() {
        return new JEIComponentAspect(this);
    }
}
