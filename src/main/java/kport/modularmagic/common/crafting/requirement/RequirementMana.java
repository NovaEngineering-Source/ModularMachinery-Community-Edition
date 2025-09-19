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
import kport.modularmagic.common.crafting.component.ComponentMana;
import kport.modularmagic.common.crafting.helper.ManaProviderCopy;
import kport.modularmagic.common.crafting.requirement.types.ModularMagicRequirements;
import kport.modularmagic.common.crafting.requirement.types.RequirementTypeMana;
import kport.modularmagic.common.integration.jei.component.JEIComponentMana;
import kport.modularmagic.common.integration.jei.ingredient.Mana;
import kport.modularmagic.common.tile.TileManaProvider;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentManaProvider;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequirementMana extends ComponentRequirement.PerTickParallelizable<Mana, RequirementTypeMana> implements Asyncable {

    public int     manaAmount;
    public boolean perTick;

    public RequirementMana(IOType actionType, int manaAmount, boolean perTick) {
        super((RequirementTypeMana) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(ModularMagicRequirements.KEY_REQUIREMENT_MANA), actionType);
        this.manaAmount = manaAmount;
        this.perTick = perTick;
    }

    @Nonnull
    private static List<ManaProviderCopy> convertManaProviders(final List<ProcessingComponent<?>> components) {
        if (components.size() == 1) {
            return Collections.singletonList((ManaProviderCopy) components.get(0).getProvidedComponent());
        } else {
            return Lists.transform(components, component -> component != null ? (ManaProviderCopy) component.getProvidedComponent() : null);
        }
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentMana &&
            cmp instanceof MachineComponentManaProvider &&
            cmp.ioType == getActionType();
    }

    @Nonnull
    @Override
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        List<ProcessingComponent<?>> list = new ArrayList<>();
        for (final ProcessingComponent<?> component : components) {
            list.add(new ProcessingComponent<>((
                MachineComponent<Object>) component.component(),
                new ManaProviderCopy(((ManaProviderCopy) component.providedComponent()).getOriginal()),
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
                int taken = reduceAll(convertManaProviders(components), context, parallelism, true);
                if (taken < parallelism) {
                    return CraftCheck.failure("error.modularmachinery.requirement.mana.less");
                }
            }
            case OUTPUT -> {
                if (!ignoreOutputCheck) {
                    int added = receiveAll(convertManaProviders(components), context, parallelism, true);
                    if (added < parallelism) {
                        return CraftCheck.failure("error.modularmachinery.requirement.mana.space");
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
                int taken = reduceAll(convertManaProviders(components), context, parallelism, false);
                if (taken < parallelism) {
                    return CraftCheck.failure("error.modularmachinery.requirement.mana.less");
                }
            }
            case OUTPUT -> {
                int added = receiveAll(convertManaProviders(components), context, parallelism, false);
                if (added < parallelism) {
                    return CraftCheck.failure("error.modularmachinery.requirement.mana.space");
                }
            }
        }

        return CraftCheck.success();
    }

    @Override
    public void startCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (!perTick && this.getActionType() == IOType.INPUT) {
            reduceAll(convertManaProviders(components), context, parallelism, false);
        }
    }

    @Override
    public void finishCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (!perTick && this.getActionType() == IOType.OUTPUT) {
            receiveAll(convertManaProviders(components), context, parallelism, false);
        }
    }

    @Override
    public int getMaxParallelism(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxParallelism) {
        if (ignoreOutputCheck && actionType == IOType.OUTPUT) {
            return maxParallelism;
        }
        if (parallelizeUnaffected) {
            int max = switch (actionType) {
                case INPUT -> reduceAll(convertManaProviders(components), context, 1, true);
                case OUTPUT -> receiveAll(convertManaProviders(components), context, 1, true);
            };
            if (max >= 1) {
                return maxParallelism;
            }
            return 0;
        }

        return switch (actionType) {
            case INPUT -> reduceAll(convertManaProviders(components), context, maxParallelism, true);
            case OUTPUT -> receiveAll(convertManaProviders(components), context, maxParallelism, true);
        };
    }

    private int receiveAll(final List<ManaProviderCopy> manaProviderCopies,
                           final RecipeCraftingContext context,
                           final float maxMultiplier,
                           final boolean simulate) {
        int toReceive = Math.round(RecipeModifier.applyModifiers(context, this, (float) this.manaAmount, false));
        int maxReceive = (int) (toReceive * maxMultiplier);

        int totalReceived = 0;
        for (final ManaProviderCopy manaProviderCopy : manaProviderCopies) {
            int capacity = manaProviderCopy.getManaCapacity();
            if (simulate) {
                int maxCanReceive = Math.min(maxReceive - totalReceived, capacity - manaProviderCopy.getCurrentMana());
                if (maxCanReceive > 0) {
                    manaProviderCopy.recieveMana(maxCanReceive);
                    totalReceived += maxCanReceive;
                }
            } else {
                TileManaProvider original = manaProviderCopy.getOriginal();
                int maxCanReceive = Math.min(maxReceive - totalReceived, capacity - original.getCurrentMana());
                if (maxCanReceive > 0) {
                    original.recieveMana(maxCanReceive);
                    totalReceived += maxCanReceive;
                }
            }
            if (totalReceived >= maxReceive) {
                break;
            }
        }

        if (totalReceived < maxReceive) {
            return totalReceived / toReceive;
        }
        return totalReceived;
    }

    private int reduceAll(final List<ManaProviderCopy> manaProviderCopies,
                          final RecipeCraftingContext context,
                          final float maxMultiplier,
                          final boolean simulate) {
        int toReduce = Math.round(RecipeModifier.applyModifiers(context, this, (float) this.manaAmount, false));
        int maxReduce = (int) (toReduce * maxMultiplier);

        int totalReduced = 0;
        for (final ManaProviderCopy manaProviderCopy : manaProviderCopies) {
            if (simulate) {
                int current = manaProviderCopy.getCurrentMana();
                if (current > 0) {
                    int maxCanReduce = Math.min(maxReduce - totalReduced, current);
                    manaProviderCopy.reduceMana(maxCanReduce);
                    totalReduced += maxCanReduce;
                }
            } else {
                TileManaProvider original = manaProviderCopy.getOriginal();
                int current = original.getCurrentMana();
                if (current > 0) {
                    int maxCanReduce = Math.min(maxReduce - totalReduced, current);
                    original.reduceMana(maxCanReduce);
                    totalReduced += maxCanReduce;
                }
            }
            if (totalReduced >= maxReduce) {
                break;
            }
        }

        if (totalReduced < maxReduce) {
            return totalReduced / toReduce;
        }
        return totalReduced;
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid";
    }

    @Override
    public RequirementMana deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementMana deepCopyModified(List<RecipeModifier> list) {
        int manaAmount = Math.round(RecipeModifier.applyModifiers(list, this, this.manaAmount, false));
        return new RequirementMana(actionType, manaAmount, perTick);
    }

    @Override
    public JEIComponentMana provideJEIComponent() {
        return new JEIComponentMana(this);
    }
}
