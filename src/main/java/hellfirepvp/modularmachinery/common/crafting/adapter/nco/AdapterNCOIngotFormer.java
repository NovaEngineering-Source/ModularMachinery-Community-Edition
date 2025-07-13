package hellfirepvp.modularmachinery.common.crafting.adapter.nco;

import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import nc.recipe.BasicRecipe;
import nc.recipe.NCRecipes;
import nc.recipe.ingredient.IFluidIngredient;
import nc.recipe.ingredient.IItemIngredient;
import nc.recipe.processor.IngotFormerRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;

public class AdapterNCOIngotFormer extends AdapterNCOMachine {

    public static final int WORK_TIME = 400;
    public static final int BASE_ENERGY_USAGE = 30;

    public AdapterNCOIngotFormer() {
        super(new ResourceLocation("nuclearcraft", "ingot_former"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {

        IngotFormerRecipes ingotFormerRecipes = NCRecipes.ingot_former;
        List<BasicRecipe> recipeList = ingotFormerRecipes.getRecipeList();
        List<MachineRecipe> machineRecipeList = new ArrayList<>(recipeList.size());

        for (BasicRecipe basicRecipe : recipeList) {
            MachineRecipe recipe = createRecipeShell(
                    new ResourceLocation("nuclearcraft", "ingot_former_" + incId),
                    owningMachineName,
                    (int) basicRecipe.getBaseProcessTime(Math.round(RecipeModifier.applyModifiers(
                            modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, WORK_TIME, false))),
                    incId,
                    false
            );

            // Fluid Input
            for (IFluidIngredient fluidIngredient : basicRecipe.getFluidIngredients()) {
                FluidStack stack = fluidIngredient.getStack();
                if (stack != null && stack.amount > 0) {
                    int inAmount = Math.round(RecipeModifier.applyModifiers(
                            modifiers, RequirementTypesMM.REQUIREMENT_FLUID, IOType.INPUT, stack.amount, false));
                    if (inAmount > 0) {
                        FluidStack copy = stack.copy();
                        copy.amount = inAmount;
                        recipe.addRequirement(new RequirementFluid(IOType.INPUT, copy));
                    }
                }
            }

            // Item Output
            for (IItemIngredient itemIngredient : basicRecipe.getItemProducts()) {
                ItemStack stack = itemIngredient.getStack();
                if (stack != null && !stack.isEmpty()) {
                    int outAmount = Math.round(RecipeModifier.applyModifiers(
                            modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, stack.getCount(), false));
                    if (outAmount > 0) {
                        recipe.addRequirement(new RequirementItem(IOType.OUTPUT,
                                ItemUtils.copyStackWithSize(stack, outAmount)));
                    }
                }
            }

            // Energy Input
            recipe.addRequirement(new RequirementEnergy(IOType.INPUT, Math.round(
                    RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT, BASE_ENERGY_USAGE, false))));

            machineRecipeList.add(recipe);
            incId++;
        }

        return machineRecipeList;
    }
}
