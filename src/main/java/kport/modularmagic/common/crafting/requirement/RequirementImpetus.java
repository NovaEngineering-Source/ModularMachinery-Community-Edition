package kport.modularmagic.common.crafting.requirement;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import kport.modularmagic.common.crafting.component.ComponentImpetus;
import kport.modularmagic.common.crafting.helper.ImpetusProviderCopy;
import kport.modularmagic.common.crafting.requirement.types.RequirementTypeImpetus;
import kport.modularmagic.common.integration.jei.component.JEIComponentImpetus;
import kport.modularmagic.common.integration.jei.ingredient.Impetus;
import kport.modularmagic.common.tile.TileImpetusComponent;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentImpetus;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author youyihj
 */
public class RequirementImpetus extends ComponentRequirement.MultiCompParallelizable<Impetus, RequirementTypeImpetus> implements Asyncable {
    private final int impetus;

    public RequirementImpetus(IOType actionType, int impetus) {
        super(RequirementTypeImpetus.INSTANCE, actionType);
        this.impetus = impetus;
    }

    @Nonnull
    private static List<ImpetusProviderCopy> convertImpetusProviders(final List<ProcessingComponent<?>> components) {
        if (components.size() == 1) {
            return Collections.singletonList((ImpetusProviderCopy) components.get(0).getProvidedComponent());
        } else {
            return Lists.transform(components, component -> component != null ? (ImpetusProviderCopy) component.getProvidedComponent() : null);
        }
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentImpetus &&
            cmp instanceof MachineComponentImpetus &&
            cmp.getIOType() == this.getActionType();
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        List<ProcessingComponent<?>> list = new ArrayList<>();
        for (final ProcessingComponent<?> component : components) {
            list.add(new ProcessingComponent<>((
                MachineComponent<Object>) component.component(),
                new ImpetusProviderCopy(((ImpetusProviderCopy) component.providedComponent()).getOriginal()),
                component.tag())
            );
        }
        return list;
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        List<ImpetusProviderCopy> impetusCopies = convertImpetusProviders(components);

        switch (getActionType()) {
            case INPUT -> {
                int taken = consumeAll(impetusCopies, context, parallelism, true);
                if (taken < parallelism) {
                    return CraftCheck.failure("error.modularmachinery.impetus.less");
                }
            }
            case OUTPUT -> {
                if (!ignoreOutputCheck) {
                    int added = supplyAll(impetusCopies, context, parallelism, true);
                    if (added < parallelism) {
                        return CraftCheck.failure("error.modularmachinery.impetus.space");
                    }
                }
            }
        }

        return CraftCheck.success();
    }

    @Override
    public void startCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (this.getActionType() == IOType.INPUT) {
            consumeAll(convertImpetusProviders(components), context, parallelism, false);
        }
    }

    @Override
    public void finishCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (this.getActionType() == IOType.OUTPUT) {
            supplyAll(convertImpetusProviders(components), context, parallelism, false);
        }
    }

    @Override
    public int getMaxParallelism(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxParallelism) {
        if (ignoreOutputCheck && actionType == IOType.OUTPUT) {
            return maxParallelism;
        }
        if (parallelizeUnaffected) {
            int max = switch (actionType) {
                case INPUT -> consumeAll(convertImpetusProviders(components), context, 1, true);
                case OUTPUT -> supplyAll(convertImpetusProviders(components), context, 1, true);
            };
            if (max >= 1) {
                return maxParallelism;
            }
            return 0;
        }
        return switch (actionType) {
            case INPUT -> consumeAll(convertImpetusProviders(components), context, maxParallelism, true);
            case OUTPUT -> supplyAll(convertImpetusProviders(components), context, maxParallelism, true);
        };
    }

    private int supplyAll(final List<ImpetusProviderCopy> impetusCopies,
                          final RecipeCraftingContext context,
                          final float maxMultiplier,
                          final boolean simulate) {
        int toSupply = Math.round(RecipeModifier.applyModifiers(context, this, (float) this.impetus, false));
        int maxSupply = (int) (toSupply * maxMultiplier);

        int totalSupplied = 0;
        for (final ImpetusProviderCopy impetusProviderCopy : impetusCopies) {
            if (simulate) {
                totalSupplied += impetusProviderCopy.supplyImpetus(maxSupply - totalSupplied);
            } else {
                totalSupplied += impetusProviderCopy.getOriginal().supplyImpetus(maxSupply - totalSupplied);
            }
            if (totalSupplied >= maxSupply) {
                break;
            }
        }

        if (totalSupplied < maxSupply) {
            return totalSupplied / toSupply;
        }
        return totalSupplied;
    }

    private int consumeAll(final List<ImpetusProviderCopy> impetusCopies,
                           final RecipeCraftingContext context,
                           final float maxMultiplier,
                           final boolean simulate) {
        int toConsume = Math.round(RecipeModifier.applyModifiers(context, this, (float) this.impetus, false));
        int maxConsume = (int) (toConsume * maxMultiplier);

        int totalConsumed = 0;
        for (final ImpetusProviderCopy impetusProviderCopy : impetusCopies) {
            if (simulate) {
                int impetus = impetusProviderCopy.getImpetus();
                totalConsumed += impetusProviderCopy.consumeImpetus(Math.min(impetus, maxConsume - totalConsumed));
            } else {
                TileImpetusComponent original = impetusProviderCopy.getOriginal();
                int impetus = original.getImpetus();
                totalConsumed += original.consumeImpetus(Math.min(impetus, maxConsume - totalConsumed));
            }
            if (totalConsumed >= maxConsume) {
                break;
            }
        }

        if (totalConsumed < maxConsume) {
            return totalConsumed / toConsume;
        }
        return totalConsumed;
    }

    @Override
    public RequirementImpetus deepCopy() {
        return new RequirementImpetus(getActionType(), this.impetus);
    }

    @Override
    public RequirementImpetus deepCopyModified(List<RecipeModifier> modifiers) {
        float newAmount = RecipeModifier.applyModifiers(modifiers, this, this.impetus, false);
        return new RequirementImpetus(getActionType(), MathHelper.ceil(newAmount));
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "component.missing.impetus." + ioType.name().toLowerCase(Locale.ENGLISH);
    }

    public Impetus getImpetus() {
        return new Impetus(impetus);
    }

    @Override
    public JEIComponent<Impetus> provideJEIComponent() {
        return new JEIComponentImpetus(this);
    }
}
