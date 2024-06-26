package hellfirepvp.modularmachinery.common.crafting.adapter.ic2;

import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AdapterIC2Compressor extends AdapterIC2Machine {
    public static final int BASE_WORK_TIME = 300;

    public AdapterIC2Compressor() {
        super(new ResourceLocation("ic2", "te_compressor"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {
        Iterable<? extends ic2.api.recipe.MachineRecipe<IRecipeInput, Collection<ItemStack>>> machineRecipes = Recipes.compressor.getRecipes();

        List<MachineRecipe> recipes = new ArrayList<>(40);
        convertIC2RecipeToMMRecipe(owningMachineName, modifiers, machineRecipes, recipes, "te_compressor_recipe_", BASE_WORK_TIME);

        return recipes;
    }
}
