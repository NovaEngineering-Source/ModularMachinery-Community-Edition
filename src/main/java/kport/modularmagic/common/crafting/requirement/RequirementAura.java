package kport.modularmagic.common.crafting.requirement;

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
import kport.modularmagic.common.crafting.component.ComponentAura;
import kport.modularmagic.common.crafting.requirement.types.ModularMagicRequirements;
import kport.modularmagic.common.crafting.requirement.types.RequirementTypeAura;
import kport.modularmagic.common.integration.jei.component.JEIComponentAura;
import kport.modularmagic.common.integration.jei.ingredient.Aura;
import kport.modularmagic.common.tile.TileAuraProvider;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentAuraProvider;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequirementAura extends ComponentRequirement.MultiCompParallelizable<Aura, RequirementTypeAura>
    implements Asyncable, ComponentRequirement.Parallelizable {

    public Aura aura;
    public int  max;
    public int  min;

    public RequirementAura(IOType type, Aura aura, int max, int min) {
        super((RequirementTypeAura) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(ModularMagicRequirements.KEY_REQUIREMENT_AURA), type);
        this.aura = aura;
        this.max = max;
        this.min = min;
    }

    @Nonnull
    private static Map<ChunkPos, TileAuraProvider> buildChunkAuraProviderMap(final List<ProcessingComponent<?>> components) {
        Map<ChunkPos, TileAuraProvider> chunkAuraProviders = new HashMap<>();
        for (final ProcessingComponent<?> component : components) {
            TileAuraProvider provider = (TileAuraProvider) component.getComponent().getContainerProvider();
            chunkAuraProviders.putIfAbsent(provider.getChunkPos(), provider);
        }
        return chunkAuraProviders;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentAura &&
            cmp instanceof MachineComponentAuraProvider &&
            cmp.ioType == getActionType();
    }

    @Nonnull
    @Override
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        return components;
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        Map<ChunkPos, TileAuraProvider> chunkAuraProviders = buildChunkAuraProviderMap(components);
        switch (actionType) {
            case INPUT -> {
                int removed = removeAll(chunkAuraProviders.values(), context, parallelism, true);
                if (removed < parallelism) {
                    return CraftCheck.failure("error.modularmachinery.requirement.aura.less");
                }
            }
            case OUTPUT -> {
                int added = addAll(chunkAuraProviders.values(), context, parallelism, true);
                if (added < parallelism) {
                    return CraftCheck.failure("error.modularmachinery.requirement.aura.more");
                }
            }
        }
        return CraftCheck.success();
    }

    @Override
    public void startCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (actionType == IOType.INPUT) {
            Map<ChunkPos, TileAuraProvider> chunkAuraProviders = buildChunkAuraProviderMap(components);
            removeAll(chunkAuraProviders.values(), context, parallelism, false);
        }
    }

    @Override
    public void finishCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (actionType == IOType.OUTPUT) {
            Map<ChunkPos, TileAuraProvider> chunkAuraProviders = buildChunkAuraProviderMap(components);
            addAll(chunkAuraProviders.values(), context, parallelism, false);
        }
    }

    @Override
    public int getMaxParallelism(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxParallelism) {
        if (ignoreOutputCheck && actionType == IOType.OUTPUT) {
            return maxParallelism;
        }
        Map<ChunkPos, TileAuraProvider> chunkAuraProviders = buildChunkAuraProviderMap(components);
        if (parallelizeUnaffected) {
            int max = switch (actionType) {
                case INPUT -> removeAll(chunkAuraProviders.values(), context, 1, true);
                case OUTPUT -> addAll(chunkAuraProviders.values(), context, 1, true);
            };
            if (max >= 1) {
                return maxParallelism;
            }
            return 0;
        }
        return switch (actionType) {
            case INPUT -> removeAll(chunkAuraProviders.values(), context, parallelism, true);
            case OUTPUT -> addAll(chunkAuraProviders.values(), context, parallelism, true);
        };
    }

    private int addAll(final Collection<TileAuraProvider> chunkAuraProviders,
                       final RecipeCraftingContext context,
                       final float maxMultiplier,
                       final boolean simulate) {
        int toAdd = (int) Math.round(RecipeModifier.applyModifiers(context, this, (double) aura.getAmount(), false));
        int maxAdd = (int) (toAdd * maxMultiplier);

        int totalAdded = 0;
        for (final TileAuraProvider auraProvider : chunkAuraProviders) {
            Aura aura = auraProvider.getAura();
            if (aura.getType() == this.aura.getType() && aura.getAmount() < max) {
                int added = max - aura.getAmount();
                totalAdded += added;
                if (!simulate) {
                    auraProvider.addAura(new Aura(added, this.aura.getType()));
                }
                if (totalAdded >= maxAdd) {
                    break;
                }
            }
        }

        if (totalAdded < maxAdd) {
            return totalAdded / toAdd;
        }
        return totalAdded;
    }

    private int removeAll(final Collection<TileAuraProvider> chunkAuraProviders,
                          final RecipeCraftingContext context,
                          final float maxMultiplier,
                          final boolean simulate) {
        int toRemove = (int) Math.round(RecipeModifier.applyModifiers(context, this, (double) aura.getAmount(), false));
        int maxRemove = (int) (toRemove * maxMultiplier);

        int totalRemoved = 0;
        for (final TileAuraProvider auraProvider : chunkAuraProviders) {
            Aura aura = auraProvider.getAura();
            if (aura.getType() == this.aura.getType() && aura.getAmount() > min) {
                int removed = aura.getAmount() - min;
                totalRemoved += removed;
                if (!simulate) {
                    auraProvider.removeAura(new Aura(removed, this.aura.getType()));
                }
                if (totalRemoved >= maxRemove) {
                    break;
                }
            }
        }

        if (totalRemoved < maxRemove) {
            return totalRemoved / toRemove;
        }
        return totalRemoved;
    }

    @Override
    public ComponentRequirement<Aura, RequirementTypeAura> deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public ComponentRequirement<Aura, RequirementTypeAura> deepCopyModified(List<RecipeModifier> modifiers) {
        Aura aura = new Aura(Math.round(
            RecipeModifier.applyModifiers(modifiers, this, this.aura.getAmount(), false)),
            this.aura.getType()
        );
        return new RequirementAura(actionType, aura, this.max, this.min);
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid";
    }

    @Override
    public JEIComponent<Aura> provideJEIComponent() {
        return new JEIComponentAura(this);
    }
}
