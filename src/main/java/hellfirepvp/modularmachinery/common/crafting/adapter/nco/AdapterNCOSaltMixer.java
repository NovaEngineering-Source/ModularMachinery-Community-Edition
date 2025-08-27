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
import nc.recipe.processor.SaltMixerRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Adapter for NuclearCraft Overhauled Salt Mixer (aka Fluid Mixer).
 */
public class AdapterNCOSaltMixer extends AdapterNCOMachine {
    public static final int WORK_TIME = 400;
    public static final int BASE_ENERGY_USAGE = 5;

    public AdapterNCOSaltMixer() {
        super(new ResourceLocation("nuclearcraft", "salt_mixer"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {
        SaltMixerRecipes saltMixer = NCRecipes.salt_mixer;

        List<BasicRecipe> recipeList = saltMixer.getRecipeList();
        List<MachineRecipe> machineRecipeList = new ArrayList<>(recipeList.size());

        for (BasicRecipe basicRecipe : recipeList) {
            MachineRecipe recipe = createRecipeShell(
                    new ResourceLocation("nuclearcraft", "salt_mixer_" + incId),
                    owningMachineName,
                    (int) basicRecipe.getBaseProcessTime(Math.round(RecipeModifier.applyModifiers(
                            modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, WORK_TIME, false))),
                    incId,
                    false
            );

            // Fluid inputs
            for (IFluidIngredient fluidIngredient : basicRecipe.getFluidIngredients()) {
                FluidStack stack = fluidIngredient.getStack();
                if (stack == null) continue;
                FluidStack copy = stack.copy();
                int inAmount = Math.round(RecipeModifier.applyModifiers(
                        modifiers, RequirementTypesMM.REQUIREMENT_FLUID, IOType.INPUT, copy.amount, false));
                if (inAmount > 0) {
                    copy.amount = inAmount;
                    recipe.addRequirement(new RequirementFluid(IOType.INPUT, copy));
                }
            }

            // Fluid outputs
            for (IFluidIngredient fluidProduct : basicRecipe.getFluidProducts()) {
                FluidStack stack = fluidProduct.getStack();
                if (stack == null) continue;
                FluidStack copy = stack.copy();
                int outAmount = Math.round(RecipeModifier.applyModifiers(
                        modifiers, RequirementTypesMM.REQUIREMENT_FLUID, IOType.OUTPUT, copy.amount, false));
                if (outAmount > 0) {
                    copy.amount = outAmount;
                    recipe.addRequirement(new RequirementFluid(IOType.OUTPUT, copy));
                }
            }

            // Energy input per tick
            recipe.addRequirement(new RequirementEnergy(IOType.INPUT, Math.round(RecipeModifier.applyModifiers(
                    modifiers, RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT, BASE_ENERGY_USAGE, false))));

            machineRecipeList.add(recipe);
            incId++;
        }

        return machineRecipeList;
    }
}
