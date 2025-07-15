package hellfirepvp.modularmachinery.common.crafting.adapter.nco;

import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import github.kasuminova.mmce.common.itemtype.ChancedIngredientStack;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementIngredientArray;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import nc.recipe.BasicRecipe;
import nc.recipe.NCRecipes;
import nc.recipe.ingredient.IItemIngredient;
import nc.recipe.ingredient.ItemArrayIngredient;
import nc.recipe.ingredient.OreIngredient;
import nc.recipe.processor.SeparatorRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.*;

public class AdapterNCOSeparator extends AdapterNCOMachine {
    public static final int TOTAL_ENERGY_USAGE = 2000; // 原始为40 RF/t * 10 ticks = 400 RF总量
    public static final int ENERGY_PER_TICK = 10;

    public AdapterNCOSeparator() {
        super(new ResourceLocation("nuclearcraft", "separator"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {
        SeparatorRecipes separatorRecipes = NCRecipes.separator;

        List<BasicRecipe> recipeList = separatorRecipes.getRecipeList();
        List<MachineRecipe> machineRecipeList = new ArrayList<>(recipeList.size());

        for (BasicRecipe basicRecipe : recipeList) {
            // 计算总能耗和持续时间
            int totalEnergy = Math.round(RecipeModifier.applyModifiers(
                    modifiers, RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT, TOTAL_ENERGY_USAGE, false));
            int duration = Math.max(1, totalEnergy / ENERGY_PER_TICK); // 防止除零

            MachineRecipe recipe = createRecipeShell(new ResourceLocation("nuclearcraft", "separator_" + incId),
                    owningMachineName,
                    duration,
                    incId, false);

            // Item Inputs
            for (IItemIngredient iItemIngredient : basicRecipe.getItemIngredients()) {
                ItemStack stack = iItemIngredient.getStack();

                int inAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, stack.getCount(), false));
                if (inAmount <= 0) continue;

                if (iItemIngredient instanceof OreIngredient ore) {
                    recipe.addRequirement(new RequirementItem(IOType.INPUT, ore.oreName, inAmount));
                    continue;
                }

                if (iItemIngredient instanceof ItemArrayIngredient arrayIngredient) {
                    List<ChancedIngredientStack> ingredientStackList = new ArrayList<>();
                    for (IItemIngredient itemIngredient : arrayIngredient.ingredientList) {
                        if (itemIngredient instanceof OreIngredient ore) {
                            int subInAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, ore.stackSize, false));
                            ingredientStackList.add(new ChancedIngredientStack(ore.oreName, subInAmount));
                        } else {
                            ItemStack itemStack = itemIngredient.getStack();
                            int subInAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, itemStack.getCount(), false));
                            ingredientStackList.add(new ChancedIngredientStack(ItemUtils.copyStackWithSize(itemStack, subInAmount)));
                        }
                    }
                    recipe.addRequirement(new RequirementIngredientArray(ingredientStackList));
                    continue;
                }

                recipe.addRequirement(new RequirementItem(IOType.INPUT, ItemUtils.copyStackWithSize(stack, inAmount)));
            }

            // Item Outputs
            for (IItemIngredient iItemIngredient : basicRecipe.getItemProducts()) {
                ItemStack stack = iItemIngredient.getStack();
                int outAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, stack.getCount(), false));
                if (outAmount > 0) {
                    recipe.addRequirement(new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(stack, outAmount)));
                }
            }

            // Energy per tick (fixed 10 RF/t)
            recipe.addRequirement(new RequirementEnergy(IOType.INPUT, ENERGY_PER_TICK));

            machineRecipeList.add(recipe);
            incId++;
        }

        return machineRecipeList;
    }
}
