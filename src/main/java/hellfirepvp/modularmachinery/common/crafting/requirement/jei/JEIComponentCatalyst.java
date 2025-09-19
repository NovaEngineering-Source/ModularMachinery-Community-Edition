package hellfirepvp.modularmachinery.common.crafting.requirement.jei;

import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementCatalyst;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;

public class JEIComponentCatalyst extends JEIComponentIngredientArray {
    RequirementCatalyst requirement;

    public JEIComponentCatalyst(RequirementCatalyst requirement) {
        super(requirement);
        this.requirement = requirement;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RecipeLayoutPart<ItemStack> getLayoutPart(Point offset) {
        return new RecipeLayoutPart.Item(offset);
    }

    @Override
    public void onJEIHoverTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
        super.onJEIHoverTooltip(slotIndex, input, ingredient, tooltip);

        List<String> toolTipList = requirement.getToolTipList();
        tooltip.add(I18n.format("tooltip.machinery.catalyst"));
        if (!toolTipList.isEmpty()) {
            tooltip.add(I18n.format("tooltip.machinery.catalyst.effect"));
            tooltip.addAll(toolTipList);
        }
    }
}
