package hellfirepvp.modularmachinery.common.crafting.adapter.te5;

import cofh.thermalexpansion.util.managers.machine.PulverizerManager;
import cofh.thermalexpansion.util.managers.machine.PulverizerManager.PulverizerRecipe;
import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import github.kasuminova.mmce.common.util.HashedItemStack;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.*;

public class PulverizerRecipeAdapter extends RecipeAdapter {

    private static final int ENERGY_PER_TICK = 20;

    public PulverizerRecipeAdapter() {
        super(new ResourceLocation("thermalexpansion", "pulverizer"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {

        List<MachineRecipe> recipes = new ArrayList<>();
        PulverizerRecipe[] pulverizerRecipes = PulverizerManager.getRecipeList();
        Set<HashedItemStack> inputs = new ObjectOpenHashSet<>();

        for (PulverizerRecipe recipeEntry : pulverizerRecipes) {
            ItemStack input = recipeEntry.getInput();
            ItemStack primaryOutput = recipeEntry.getPrimaryOutput();
            ItemStack secondaryOutput = recipeEntry.getSecondaryOutput();
            int secondaryChance = recipeEntry.getSecondaryOutputChance();
            int energy = recipeEntry.getEnergy();

            // 去重
            HashedItemStack hashedInput = HashedItemStack.ofUnsafe(input);
            if (!inputs.add(hashedInput.copy())) continue;

            MachineRecipe recipe = createRecipeShell(
                    new ResourceLocation("thermalexpansion", "pulverizer_" + incId),
                    owningMachineName,
                    Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, (float) energy / ENERGY_PER_TICK, false)),
                    incId++, false
            );

            // Energy
            int energyPerTick = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT, ENERGY_PER_TICK, false));
            if (energyPerTick > 0) {
                recipe.addRequirement(new RequirementEnergy(IOType.INPUT, energyPerTick));
            }

            // Input
            int inputAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, input.getCount(), false));
            if (inputAmount > 0) {
                recipe.addRequirement(new RequirementItem(IOType.INPUT, ItemUtils.copyStackWithSize(input, inputAmount)));
            }

            // Primary Output
            int primaryAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, primaryOutput.getCount(), false));
            if (primaryAmount > 0) {
                recipe.addRequirement(new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(primaryOutput, primaryAmount)));
            }

            // Secondary Output
            int secondaryAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, secondaryOutput.getCount(), false));
            float chance = RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, secondaryChance / 100.0f, true);

            if (secondaryAmount > 0 && !secondaryOutput.isEmpty()) {
                if (chance >= 1.0f) {
                    int guaranteed = (int) chance;
                    float extraChance = chance - guaranteed;
                    recipe.addRequirement(new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(secondaryOutput, guaranteed * secondaryAmount)));
                    if (extraChance > 0) {
                        RequirementItem extra = new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(secondaryOutput, secondaryAmount));
                        extra.setChance(extraChance);
                        recipe.addRequirement(extra);
                    }
                } else {
                    RequirementItem withChance = new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(secondaryOutput, secondaryAmount));
                    withChance.setChance(chance);
                    recipe.addRequirement(withChance);
                }
            }

            recipes.add(recipe);
        }

        return recipes;
    }
}
