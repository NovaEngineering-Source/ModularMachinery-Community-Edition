package hellfirepvp.modularmachinery.common.crafting.adapter.te5;

import cofh.thermalexpansion.util.managers.machine.CrucibleManager;
import cofh.thermalexpansion.util.managers.machine.CrucibleManager.CrucibleRecipe;
import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import github.kasuminova.mmce.common.util.HashedItemStack;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;

public class CrucibleRecipeAdapter extends RecipeAdapter {

    private static final int ENERGY_PER_TICK = 40;

    public CrucibleRecipeAdapter() {
        super(new ResourceLocation("thermalexpansion", "crucible"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {

        List<MachineRecipe> recipes = new ArrayList<>();
        CrucibleRecipe[] crucibleRecipes = CrucibleManager.getRecipeList();
        Set<HashedItemStack> inputs = new HashSet<>();

        for (CrucibleRecipe recipeEntry : crucibleRecipes) {
            ItemStack input = recipeEntry.getInput();
            FluidStack output = recipeEntry.getOutput();
            int energy = recipeEntry.getEnergy();

            if (input.isEmpty() || output == null || output.amount <= 0) continue;

            HashedItemStack hashedInput = HashedItemStack.ofUnsafe(input);
            if (!inputs.add(hashedInput.copy())) continue;

            MachineRecipe recipe = createRecipeShell(
                    new ResourceLocation("thermalexpansion", "crucible_" + incId),
                    owningMachineName,
                    Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, (float) energy / ENERGY_PER_TICK, false)),
                    incId++, false
            );

            // Energy
            int energyPerTick = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT, ENERGY_PER_TICK, false));
            if (energyPerTick > 0) {
                recipe.addRequirement(new RequirementEnergy(IOType.INPUT, energyPerTick));
            }

            // Item Input
            int inputAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, input.getCount(), false));
            if (inputAmount > 0) {
                recipe.addRequirement(new RequirementItem(IOType.INPUT, ItemUtils.copyStackWithSize(input, inputAmount)));
            }

            // Fluid Output
            int outputAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_FLUID, IOType.OUTPUT, output.amount, false));
            if (outputAmount > 0) {
                FluidStack outCopy = output.copy();
                outCopy.amount = outputAmount;
                recipe.addRequirement(new RequirementFluid(IOType.OUTPUT, outCopy));
            }

            recipes.add(recipe);
        }

        return recipes;
    }
}
