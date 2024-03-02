package hellfirepvp.modularmachinery.common.crafting.requirement.jei;

import com.google.common.collect.Lists;
import github.kasuminova.mmce.common.itemtype.ChancedIngredientStack;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementIngredientArray;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.List;

public class JEIComponentIngredientArray extends ComponentRequirement.JEIComponent<ItemStack> {
    public final RequirementIngredientArray requirement;

    public JEIComponentIngredientArray(RequirementIngredientArray requirement) {
        this.requirement = requirement;
    }

    @Override
    public Class<ItemStack> getJEIRequirementClass() {
        return ItemStack.class;
    }

    @Override
    public List<ItemStack> getJEIIORequirements() {
        return Lists.transform(requirement.cachedJEIIORequirementList, ingredientItemStack -> ingredientItemStack == null ? null : ingredientItemStack.stack());
    }

    @Override
    public RecipeLayoutPart<ItemStack> getLayoutPart(Point offset) {
        return new RecipeLayoutPart.Item(offset);
    }

    @Override
    public void onJEIHoverTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
        tooltip.add("");
        IOType actionType = requirement.getActionType();

        switch (actionType) {
            case INPUT -> tooltip.add(I18n.format("tooltip.machinery.ingredient_array_input"));
            case OUTPUT -> tooltip.add(I18n.format("tooltip.machinery.ingredient_array_output"));
        }

        float totalChance = 1F;
        if (actionType == IOType.OUTPUT) {
            totalChance = 0;
            for (final ChancedIngredientStack reqIngredient : requirement.getIngredients()) {
                totalChance += reqIngredient.chance;
            }
        }

        StringBuilder tooltipBuilder = new StringBuilder();
        for (ChancedIngredientStack stack : requirement.getIngredients()) {
            switch (stack.ingredientType) {
                case ITEMSTACK -> tooltipBuilder.append(stack.itemStack.getDisplayName());
                case ORE_DICT -> tooltipBuilder.append(stack.oreDictName);
            }

            if (stack.minCount != stack.maxCount) {
                tooltipBuilder.append(" * ").append(String.format("%d ~ %d", stack.minCount, stack.maxCount));
            } else {
                tooltipBuilder.append(" * ").append(stack.count);
            }

            if (input) {
                continue;
            }

            float chance = totalChance == 0 ? 0 : stack.chance / totalChance;
            if (chance < 1F && chance >= 0F) {
                tooltipBuilder.append(" (");

                String keyNever = "tooltip.machinery.chance.out.never";
                String keyChance = "tooltip.machinery.ingredient_array_output.weight";

                if (chance == 0F) {
                    tooltipBuilder.append(I18n.format(keyNever));
                } else {
                    String chanceStr = chance < 0.0001F ? "< 0.01%" : MiscUtils.formatFloat(chance * 100F, 2) + "%";
                    tooltipBuilder.append(I18n.format(keyChance, chanceStr));
                }

                tooltipBuilder.append(")");
            }

            tooltip.add(tooltipBuilder.toString());
            tooltipBuilder.setLength(0);
        }
    }

}
