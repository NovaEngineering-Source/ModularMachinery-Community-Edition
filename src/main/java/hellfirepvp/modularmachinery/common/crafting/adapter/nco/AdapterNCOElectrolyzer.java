package hellfirepvp.modularmachinery.common.crafting.adapter.nco;

import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import nc.recipe.BasicRecipe;
import nc.recipe.NCRecipes;
import nc.recipe.ingredient.IFluidIngredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;

public class AdapterNCOElectrolyzer extends AdapterNCOMachine {
    public static final int ENERGY_PER_TICK = 5000;
    public static final int PROCESS_TIME = 100;

    public AdapterNCOElectrolyzer() {
        super(new ResourceLocation("nuclearcraft", "electrolyzer"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {
        List<BasicRecipe> recipeList = NCRecipes.electrolyzer.getRecipeList();
        List<MachineRecipe> machineRecipes = new ArrayList<>(recipeList.size());

        for (BasicRecipe basicRecipe : recipeList) {
            MachineRecipe recipe = createRecipeShell(
                    new ResourceLocation("nuclearcraft", "electrolyzer_" + incId),
                    owningMachineName,
                    PROCESS_TIME,
                    incId,
                    false
            );

            // Input Fluids
            for (IFluidIngredient input : basicRecipe.getFluidIngredients()) {
                FluidStack stack = input.getStack();
                if (stack != null) {
                    int modifiedAmount = Math.round(RecipeModifier.applyModifiers(
                            modifiers, RequirementTypesMM.REQUIREMENT_FLUID, IOType.INPUT, stack.amount, false));
                    if (modifiedAmount > 0) {
                        stack.amount = modifiedAmount;
                        recipe.addRequirement(new RequirementFluid(IOType.INPUT, stack));
                    }
                }
            }

            // Output Fluids
            for (IFluidIngredient output : basicRecipe.getFluidProducts()) {
                FluidStack stack = output.getStack();
                if (stack != null) {
                    int modifiedAmount = Math.round(RecipeModifier.applyModifiers(
                            modifiers, RequirementTypesMM.REQUIREMENT_FLUID, IOType.OUTPUT, stack.amount, false));
                    if (modifiedAmount > 0) {
                        stack.amount = modifiedAmount;
                        recipe.addRequirement(new RequirementFluid(IOType.OUTPUT, stack));
                    }
                }
            }

            // Fixed energy per tick
            recipe.addRequirement(new RequirementEnergy(IOType.INPUT, ENERGY_PER_TICK));

            machineRecipes.add(recipe);
            incId++;
        }

        return machineRecipes;
    }
}
