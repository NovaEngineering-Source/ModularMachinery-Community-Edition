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

public class AdapterNCOCentrifuge extends AdapterNCOMachine {
    public static final int BASE_ENERGY_PER_TICK = 10;

    public AdapterNCOCentrifuge() {
        super(new ResourceLocation("nuclearcraft", "centrifuge"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {
        List<BasicRecipe> recipeList = NCRecipes.centrifuge.getRecipeList();
        List<MachineRecipe> machineRecipes = new ArrayList<>(recipeList.size());

        for (BasicRecipe basicRecipe : recipeList) {
            ResourceLocation recipeName = new ResourceLocation("nuclearcraft", "centrifuge_" + incId);

            int totalEnergy = Math.round(RecipeModifier.applyModifiers(modifiers,
                    RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT,
                    (float) basicRecipe.getBaseProcessPower(1.0), false));

            int duration = Math.max(1, totalEnergy / BASE_ENERGY_PER_TICK);

            MachineRecipe recipe = createRecipeShell(recipeName, owningMachineName, duration, incId, false);

            // 输入流体
            for (IFluidIngredient input : basicRecipe.getFluidIngredients()) {
                FluidStack stack = input.getStack();
                if (stack != null && stack.amount > 0) {
                    int modAmount = Math.round(RecipeModifier.applyModifiers(modifiers,
                            RequirementTypesMM.REQUIREMENT_FLUID, IOType.INPUT, stack.amount, false));
                    if (modAmount > 0) {
                        stack.amount = modAmount;
                        recipe.addRequirement(new RequirementFluid(IOType.INPUT, stack));
                    }
                }
            }

            // 输出流体
            for (IFluidIngredient output : basicRecipe.getFluidProducts()) {
                FluidStack stack = output.getStack();
                if (stack != null && stack.amount > 0) {
                    int modAmount = Math.round(RecipeModifier.applyModifiers(modifiers,
                            RequirementTypesMM.REQUIREMENT_FLUID, IOType.OUTPUT, stack.amount, false));
                    if (modAmount > 0) {
                        stack.amount = modAmount;
                        recipe.addRequirement(new RequirementFluid(IOType.OUTPUT, stack));
                    }
                }
            }

            // 能源输入
            recipe.addRequirement(new RequirementEnergy(IOType.INPUT, BASE_ENERGY_PER_TICK));

            machineRecipes.add(recipe);
            incId++;
        }

        return machineRecipes;
    }
}
