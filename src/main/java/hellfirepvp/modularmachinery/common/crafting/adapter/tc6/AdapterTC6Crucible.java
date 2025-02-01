package hellfirepvp.modularmachinery.common.crafting.adapter.tc6;

import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import github.kasuminova.mmce.common.itemtype.ChancedIngredientStack;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementIngredientArray;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import kport.modularmagic.common.crafting.requirement.RequirementAspect;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.CrucibleRecipe;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class AdapterTC6Crucible extends RecipeAdapter {
    public static final int BASE_WORK_TIME = 100;

    public AdapterTC6Crucible() {
        super(new ResourceLocation("thaumcraft", "crucible"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName, List<RecipeModifier> modifiers, List<ComponentRequirement<?, ?>> additionalRequirements, Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers, List<String> recipeTooltips) {
        List<MachineRecipe> machineRecipeList = new ArrayList<>();

        ThaumcraftApi.getCraftingRecipes().forEach((recipeName, tcRecipe) -> {
            if (!(tcRecipe instanceof CrucibleRecipe recipe)) {
                return;
            }
            if (recipe.getCatalyst() == null) {
                return;
            }
            if (recipe.getRecipeOutput() == null) {
                return;
            }
            int inAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, 1, false));
            if (inAmount <= 0) {
                return;
            }

            MachineRecipe machineRecipe = createRecipeShell(
                    new ResourceLocation("thaumcraft", "auto_crucible" + incId),
                    owningMachineName,
                    BASE_WORK_TIME,
                    incId, false);

            // Input
            ItemStack[] inputMain = recipe.getCatalyst().getMatchingStacks();
            List<ChancedIngredientStack> inputMainList = Arrays.stream(inputMain)
                    .map(itemStack -> new ChancedIngredientStack(ItemUtils.copyStackWithSize(itemStack, inAmount)))
                    .collect(Collectors.toList());
            if (!inputMainList.isEmpty()) {
                machineRecipe.addRequirement(new RequirementIngredientArray(inputMainList));
            }

            // Aspect Inputs
            recipe.getAspects().aspects.forEach((aspect, amount) -> {
                machineRecipe.addRequirement(new RequirementAspect(IOType.INPUT, amount, aspect));
            });

            // Output
            ItemStack output = recipe.getRecipeOutput();
            int outAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, output.getCount(), false));
            if (outAmount > 0) {
                machineRecipe.addRequirement(new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(output, outAmount)));
            }

            machineRecipeList.add(machineRecipe);
            incId++;
        });

        return machineRecipeList;
    }
}
