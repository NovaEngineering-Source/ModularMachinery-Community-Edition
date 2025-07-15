package hellfirepvp.modularmachinery.common.crafting.adapter.te5;

import cofh.thermalexpansion.util.managers.machine.CentrifugeManager;
import cofh.thermalexpansion.util.managers.machine.CentrifugeManager.CentrifugeRecipe;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.*;

public class CentrifugeRecipeAdapter extends RecipeAdapter {

    private static final int FIXED_ENERGY_PER_TICK = 10;

    public CentrifugeRecipeAdapter() {
        super(new ResourceLocation("thermalexpansion", "centrifuge"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {

        List<MachineRecipe> recipes = new ArrayList<>();
        Set<HashedItemStack> inputTracker = new HashSet<>();

        for (CentrifugeRecipe recipeEntry : CentrifugeManager.getRecipeList()) {
            ItemStack input = recipeEntry.getInput();
            List<ItemStack> outputs = recipeEntry.getOutput();
            int totalEnergy = recipeEntry.getEnergy();

            if (input.isEmpty() || outputs.isEmpty() || totalEnergy <= 0) continue;

            HashedItemStack hashedInput = HashedItemStack.ofUnsafe(input);
            if (!inputTracker.add(hashedInput)) continue;

            int baseDuration = totalEnergy / FIXED_ENERGY_PER_TICK;
            int modifiedDuration = Math.round(RecipeModifier.applyModifiers(modifiers,
                    RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, baseDuration, false));

            MachineRecipe recipe = createRecipeShell(
                    new ResourceLocation("thermalexpansion", "centrifuge_" + incId),
                    owningMachineName,
                    modifiedDuration,
                    incId++, false
            );

            // Energy requirement (fixed per tick)
            int energyPerTick = Math.round(RecipeModifier.applyModifiers(modifiers,
                    RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT, FIXED_ENERGY_PER_TICK, false));
            recipe.addRequirement(new RequirementEnergy(IOType.INPUT, energyPerTick));

            // Input item
            int inputAmount = Math.round(RecipeModifier.applyModifiers(modifiers,
                    RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, input.getCount(), false));
            if (inputAmount > 0) {
                recipe.addRequirement(new RequirementItem(IOType.INPUT, ItemUtils.copyStackWithSize(input, inputAmount)));
            }

            // Output items
            for (ItemStack output : outputs) {
                if (output.isEmpty()) continue;
                int outAmount = Math.round(RecipeModifier.applyModifiers(modifiers,
                        RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, output.getCount(), false));
                if (outAmount > 0) {
                    recipe.addRequirement(new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(output, outAmount)));
                }
            }

            recipes.add(recipe);
        }

        return recipes;
    }
}
