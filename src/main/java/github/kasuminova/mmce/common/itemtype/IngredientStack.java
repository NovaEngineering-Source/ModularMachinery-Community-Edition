package github.kasuminova.mmce.common.itemtype;

import github.kasuminova.mmce.common.helper.AdvancedItemChecker;
import github.kasuminova.mmce.common.helper.AdvancedItemModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

public class IngredientStack {
    public final IngredientType ingredientType;
    public       String         oreDictName = "";
    public       ItemStack      itemStack;

    public int count;
    public int minCount;
    public int maxCount;

    public NBTTagCompound             tag              = null;
    public AdvancedItemChecker        itemChecker      = null;
    public List<AdvancedItemModifier> itemModifierList = new ArrayList<>();

    public IngredientStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.count = itemStack.getCount();
        this.minCount = this.count;
        this.maxCount = this.count;
        this.ingredientType = IngredientType.ITEMSTACK;
    }

    public IngredientStack(String oreDictName, int count) {
        this.itemStack = null;
        this.oreDictName = oreDictName;
        this.count = count;
        this.minCount = count;
        this.maxCount = count;
        this.ingredientType = IngredientType.ORE_DICT;
    }

    public IngredientStack copy() {
        return switch (ingredientType) {
            case ITEMSTACK -> {
                IngredientStack ingredient = new IngredientStack(itemStack.copy());
                ingredient.minCount = minCount;
                ingredient.maxCount = maxCount;
                ingredient.tag = tag;
                ingredient.itemChecker = itemChecker;
                ingredient.itemModifierList.addAll(itemModifierList);
                yield ingredient;
            }
            case ORE_DICT -> {
                IngredientStack ingredient = new IngredientStack(oreDictName, count);
                ingredient.minCount = minCount;
                ingredient.maxCount = maxCount;
                ingredient.tag = tag;
                ingredient.itemChecker = itemChecker;
                ingredient.itemModifierList.addAll(itemModifierList);
                yield ingredient;
            }
        };
    }

    public enum IngredientType {
        ITEMSTACK,
        ORE_DICT
    }
}
