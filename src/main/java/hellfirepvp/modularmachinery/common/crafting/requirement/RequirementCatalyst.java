package hellfirepvp.modularmachinery.common.crafting.requirement;

import github.kasuminova.mmce.common.itemtype.ChancedIngredientStack;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.jei.JEIComponentCatalyst;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequirementCatalyst extends RequirementIngredientArray {
    protected final List<RecipeModifier> modifierList = new ArrayList<>();
    protected final List<String>         toolTipList  = new ArrayList<>();
    protected       boolean              isRequired   = false;

    public RequirementCatalyst(ItemStack item) {
        super(Collections.singletonList(new ChancedIngredientStack(item)));
        setParallelizeUnaffected(true);
    }

    public RequirementCatalyst(String oreDictName, int amount) {
        super(Collections.singletonList(new ChancedIngredientStack(oreDictName, amount)));
        setParallelizeUnaffected(true);
    }

    public RequirementCatalyst(List<ChancedIngredientStack> ingredients) {
        super(ingredients);
        setParallelizeUnaffected(true);
    }

    public void addModifier(RecipeModifier modifier) {
        modifierList.add(modifier);
    }

    public void addTooltip(String tooltip) {
        toolTipList.add(tooltip);
    }

    public List<String> getToolTipList() {
        return toolTipList;
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context) {
        if (super.canStartCrafting(components, context).isSuccess() && !isRequired) {
            addModifierToContext(context);
            isRequired = true;
            return CraftCheck.success();
        } else {
            isRequired = false;
        }
        return CraftCheck.skipComponent();
    }

    @Override
    public int getMaxParallelism(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxParallelism) {
        int result = super.getMaxParallelism(components, context, maxParallelism);
        if (result >= 1 && !isRequired) {
            addModifierToContext(context);
            isRequired = true;
        } else {
            isRequired = false;
        }
        // It is an optional input, so it should not theoretically return the maximum number of consumable quantities.
        return maxParallelism;
    }

    protected void addModifierToContext(final RecipeCraftingContext context) {
        if (parallelism > 1) {
            for (RecipeModifier mod : modifierList) {
                context.addPermanentModifier(mod.multiply(parallelism));
            }
        } else {
            for (RecipeModifier modifier : modifierList) {
                context.addPermanentModifier(modifier);
            }
        }
    }

    @Override
    public void startCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        if (isRequired) {
            super.startCrafting(components, context, chance);
            isRequired = false;
        }
    }

    @Override
    public RequirementCatalyst deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementCatalyst deepCopyModified(List<RecipeModifier> modifiers) {
        ArrayList<ChancedIngredientStack> copiedIngredients = new ArrayList<>();

        ingredients.forEach(item -> {
            ChancedIngredientStack copied = item.copy();

            switch (copied.ingredientType) {
                case ITEMSTACK -> {
                    ItemStack itemStack = copied.itemStack;
                    int amt = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, actionType, itemStack.getCount(), false));
                    itemStack.setCount(amt);
                }
                case ORE_DICT ->
                    copied.count = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, actionType, item.count, false));
            }
            copied.chance = RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, actionType, item.chance, true);

            copiedIngredients.add(copied);
        });

        RequirementCatalyst catalyst = new RequirementCatalyst(copiedIngredients);
        catalyst.modifierList.addAll(this.modifierList);
        catalyst.toolTipList.addAll(toolTipList);
        catalyst.chance = RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, actionType, chance, true);
        return catalyst;
    }

    @Override
    public JEIComponent<ItemStack> provideJEIComponent() {
        return new JEIComponentCatalyst(this);
    }
}
