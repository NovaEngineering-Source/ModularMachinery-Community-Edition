package hellfirepvp.modularmachinery.common.crafting.adapter.te5;

import cofh.thermalexpansion.util.managers.machine.CompactorManager;
import cofh.thermalexpansion.util.managers.machine.CompactorManager.CompactorRecipe;
import cofh.thermalexpansion.util.managers.machine.CompactorManager.Mode;
import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.*;

public class CompactorGearRecipeAdapter extends RecipeAdapter {

    private static final int ENERGY_PER_TICK = 20;

    public CompactorGearRecipeAdapter() {
        super(new ResourceLocation("thermalexpansion", "compactor_gear"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {

        List<MachineRecipe> recipes = new ArrayList<>();
        Set<String> addedInputs = new HashSet<>();

        for (CompactorRecipe recipeEntry : CompactorManager.getRecipeList(Mode.GEAR)) {
            ItemStack input = recipeEntry.getInput();
            ItemStack output = recipeEntry.getOutput();
            int energy = recipeEntry.getEnergy();

            if (input.isEmpty() || output.isEmpty()) continue;
            String key = input.getItem().getRegistryName() + "@" + input.getMetadata();
            if (!addedInputs.add(key)) continue;

            MachineRecipe recipe = createRecipeShell(
                    new ResourceLocation("thermalexpansion", "compactor_gear_" + incId),
                    owningMachineName,
                    Math.round(RecipeModifier.applyModifiers(modifiers,
                            RequirementTypesMM.REQUIREMENT_DURATION,
                            IOType.INPUT, (float) energy / ENERGY_PER_TICK, false)),
                    incId++, false
            );

            // Energy
            int energyPerTick = Math.round(RecipeModifier.applyModifiers(modifiers,
                    RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT, ENERGY_PER_TICK, false));
            recipe.addRequirement(new RequirementEnergy(IOType.INPUT, energyPerTick));

            // Input
            int inAmount = Math.round(RecipeModifier.applyModifiers(modifiers,
                    RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, input.getCount(), false));
            if (inAmount > 0) {
                recipe.addRequirement(new RequirementItem(IOType.INPUT, ItemUtils.copyStackWithSize(input, inAmount)));
            }

            // Output
            int outAmount = Math.round(RecipeModifier.applyModifiers(modifiers,
                    RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, output.getCount(), false));
            if (outAmount > 0) {
                recipe.addRequirement(new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(output, outAmount)));
            }

            recipes.add(recipe);
        }

        return recipes;
    }
}
