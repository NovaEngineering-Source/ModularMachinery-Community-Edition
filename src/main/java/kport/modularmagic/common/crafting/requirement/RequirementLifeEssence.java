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
import hellfirepvp.modularmachinery.common.util.ResultChance;
import kport.modularmagic.common.crafting.component.ComponentLifeEssence;
import kport.modularmagic.common.crafting.helper.LifeEssenceProviderCopy;
import kport.modularmagic.common.crafting.requirement.types.ModularMagicRequirements;
import kport.modularmagic.common.crafting.requirement.types.RequirementTypeLifeEssence;
import kport.modularmagic.common.integration.jei.component.JEIComponentLifeEssence;
import kport.modularmagic.common.integration.jei.ingredient.LifeEssence;
import kport.modularmagic.common.tile.TileLifeEssenceProvider;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentLifeEssenceProvider;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequirementLifeEssence extends ComponentRequirement.PerTickParallelizable<LifeEssence, RequirementTypeLifeEssence> {

    public int     essenceAmount;
    public boolean perTick;

    public RequirementLifeEssence(IOType actionType, int essenceAmount, boolean perTick) {
        super((RequirementTypeLifeEssence) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(ModularMagicRequirements.KEY_REQUIREMENT_LIFE_ESSENCE), actionType);

        this.essenceAmount = essenceAmount;
        this.perTick = perTick;
    }

    @Nonnull
    private static List<LifeEssenceProviderCopy> convertLifeEssenceProviders(final List<ProcessingComponent<?>> components) {
        if (components.size() == 1) {
            return Collections.singletonList((LifeEssenceProviderCopy) components.get(0).getProvidedComponent());
        } else {
            return Lists.transform(components, component -> component != null ? (LifeEssenceProviderCopy) component.getProvidedComponent() : null);
        }
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentLifeEssence &&
            cmp instanceof MachineComponentLifeEssenceProvider &&
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
                new LifeEssenceProviderCopy(((LifeEssenceProviderCopy) component.providedComponent()).getOriginal()),
                component.tag())
            );
        }
        return list;
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        switch (getActionType()) {
            case INPUT -> {
                int taken = removeAll(convertLifeEssenceProviders(components), context, parallelism, true);
                if (taken < parallelism) {
                    return CraftCheck.failure("error.modularmachinery.requirement.lifeessence.lp.less");
                }
            }
            case OUTPUT -> {
                if (!ignoreOutputCheck) {
                    int added = addAll(convertLifeEssenceProviders(components), context, parallelism, true);
                    if (added < parallelism) {
                        return CraftCheck.failure("error.modularmachinery.requirement.lifeessence.lp.more");
                    }
                }
            }
        }

        return CraftCheck.success();
    }

    @Override
    public CraftCheck doIOTick(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final float durationMultiplier) {
        if (!perTick) {
            return CraftCheck.success();
        }

        switch (getActionType()) {
            case INPUT -> {
                int taken = removeAll(convertLifeEssenceProviders(components), context, parallelism, false);
                if (taken < parallelism) {
                    return CraftCheck.failure("error.modularmachinery.requirement.lifeessence.lp.less");
                }
            }
            case OUTPUT -> {
                int added = addAll(convertLifeEssenceProviders(components), context, parallelism, false);
                if (added < parallelism) {
                    return CraftCheck.failure("error.modularmachinery.requirement.lifeessence.lp.more");
                }
            }
        }

        return CraftCheck.success();
    }

    @Override
    public void startCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (!perTick && actionType == IOType.INPUT) {
            removeAll(convertLifeEssenceProviders(components), context, parallelism, false);
        }
    }

    @Override
    public void finishCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (!perTick && actionType == IOType.INPUT) {
            addAll(convertLifeEssenceProviders(components), context, parallelism, false);
        }
    }

    @Override
    public int getMaxParallelism(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxParallelism) {
        if (ignoreOutputCheck && actionType == IOType.OUTPUT) {
            return maxParallelism;
        }
        if (parallelizeUnaffected) {
            int max = switch (actionType) {
                case INPUT -> removeAll(convertLifeEssenceProviders(components), context, 1, true);
                case OUTPUT -> addAll(convertLifeEssenceProviders(components), context, 1, true);
            };
            if (max >= 1) {
                return maxParallelism;
            }
            return 0;
        }
        return switch (actionType) {
            case INPUT -> removeAll(convertLifeEssenceProviders(components), context, maxParallelism, true);
            case OUTPUT -> addAll(convertLifeEssenceProviders(components), context, maxParallelism, true);
        };
    }

    private int addAll(final List<LifeEssenceProviderCopy> lifeEssenceCopies,
                       final RecipeCraftingContext context,
                       final float maxMultiplier,
                       final boolean simulate) {
        int toAdd = Math.round(RecipeModifier.applyModifiers(context, this, (float) this.essenceAmount, false));
        int maxAdd = (int) (toAdd * maxMultiplier);

        int totalAdded = 0;
        for (final LifeEssenceProviderCopy lifeEssenceProviderCopy : lifeEssenceCopies) {
            if (simulate) {
                int lifeEssenceCache = lifeEssenceProviderCopy.getLifeEssenceCache();
                int orbCapacity = lifeEssenceProviderCopy.getOrbCapacity();
                if (orbCapacity <= 0) {
                    continue;
                }
                int maxCanAdd = (orbCapacity / 10) - lifeEssenceCache;
                totalAdded += lifeEssenceProviderCopy.addLifeEssenceCache(Math.min(maxAdd - totalAdded, maxCanAdd));
            } else {
                TileLifeEssenceProvider original = lifeEssenceProviderCopy.getOriginal();
                int lifeEssenceCache = original.getLifeEssenceCache();
                int orbCapacity = original.getOrbCapacity();
                if (orbCapacity <= 0) {
                    continue;
                }
                int maxCanAdd = (orbCapacity / 10) - lifeEssenceCache;
                totalAdded += original.addLifeEssenceCache(Math.min(maxAdd - totalAdded, maxCanAdd));
            }
            if (totalAdded >= maxAdd) {
                break;
            }
        }

        if (totalAdded < maxAdd) {
            return totalAdded / toAdd;
        }
        return totalAdded;
    }

    private int removeAll(final List<LifeEssenceProviderCopy> lifeEssenceCopies,
                          final RecipeCraftingContext context,
                          final float maxMultiplier,
                          final boolean simulate) {
        int toRemove = Math.round(RecipeModifier.applyModifiers(context, this, (float) this.essenceAmount, false));
        int maxRemove = (int) (toRemove * maxMultiplier);

        int totalRemoved = 0;
        for (final LifeEssenceProviderCopy lifeEssenceProviderCopy : lifeEssenceCopies) {
            if (simulate) {
                int impetus = lifeEssenceProviderCopy.getLifeEssenceCache();
                totalRemoved += lifeEssenceProviderCopy.removeLifeEssenceCache(Math.min(impetus, maxRemove - totalRemoved));
            } else {
                TileLifeEssenceProvider original = lifeEssenceProviderCopy.getOriginal();
                int impetus = original.getLifeEssenceCache();
                totalRemoved += original.removeLifeEssenceCache(Math.min(impetus, maxRemove - totalRemoved));
            }
            if (totalRemoved >= maxRemove) {
                break;
            }
        }

        if (totalRemoved < maxRemove) {
            return totalRemoved / toRemove;
        }
        return totalRemoved;
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid";
    }

    @Override
    public RequirementLifeEssence deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementLifeEssence deepCopyModified(List<RecipeModifier> list) {
        int essenceAmount = Math.round(RecipeModifier.applyModifiers(list, this, this.essenceAmount, false));
        return new RequirementLifeEssence(actionType, essenceAmount, perTick);
    }

    @Override
    public JEIComponentLifeEssence provideJEIComponent() {
        return new JEIComponentLifeEssence(this);
    }

}
