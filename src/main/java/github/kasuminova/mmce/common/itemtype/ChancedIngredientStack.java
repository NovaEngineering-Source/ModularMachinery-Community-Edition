package github.kasuminova.mmce.common.itemtype;

import hellfirepvp.modularmachinery.common.integration.ingredient.IngredientItemStack;
import net.minecraft.item.ItemStack;

public class ChancedIngredientStack extends IngredientStack {
    public float chance = 1.0F;

    public ChancedIngredientStack(ItemStack itemStack) {
        super(itemStack);
    }

    public ChancedIngredientStack(String oreDictName, int count) {
        super(oreDictName, count);
    }

    public ChancedIngredientStack(ItemStack itemStack, float chance) {
        super(itemStack);
        this.chance = chance;
    }

    public ChancedIngredientStack(String oreDictName, int count, float chance) {
        super(oreDictName, count);
        this.chance = chance;
    }

    public IngredientItemStack asIngredientItemStack(ItemStack stack) {
        return new IngredientItemStack(stack, minCount, maxCount, chance);
    }

    @Override
    public ChancedIngredientStack copy() {
        return switch (ingredientType) {
            case ITEMSTACK -> {
                ChancedIngredientStack ingredient = new ChancedIngredientStack(itemStack.copy(), chance);
                ingredient.minCount = minCount;
                ingredient.maxCount = maxCount;
                ingredient.tag = tag;
                ingredient.itemChecker = itemChecker;
                ingredient.itemModifierList.addAll(itemModifierList);
                yield ingredient;
            }
            case ORE_DICT -> {
                ChancedIngredientStack ingredient = new ChancedIngredientStack(oreDictName, count, chance);
                ingredient.minCount = minCount;
                ingredient.maxCount = maxCount;
                ingredient.tag = tag;
                ingredient.itemChecker = itemChecker;
                ingredient.itemModifierList.addAll(itemModifierList);
                yield ingredient;
            }
        };
    }
}
