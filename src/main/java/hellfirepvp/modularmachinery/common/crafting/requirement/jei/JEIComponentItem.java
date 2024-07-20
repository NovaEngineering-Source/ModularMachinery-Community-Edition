/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirement.jei;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.integration.ingredient.IngredientItemStack;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

/**
 * This class is part of the Fracture Mod
 * The complete source code for this mod can be found on github.
 * Class: JEIComponentItem
 * Created by HellFirePvP
 * Date: 08.04.2018 / 12:44
 */
public class JEIComponentItem extends ComponentRequirement.JEIComponent<ItemStack> {

    private final RequirementItem requirement;

    public JEIComponentItem(RequirementItem requirement) {
        this.requirement = requirement;
    }

    @Override
    public Class<ItemStack> getJEIRequirementClass() {
        return ItemStack.class;
    }

    @Override
    public List<ItemStack> getJEIIORequirements() {
        return Lists.transform(requirement.cachedJEIIORequirementList, ingredient -> ingredient == null ? null : ingredient.stack());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RecipeLayoutPart<ItemStack> getLayoutPart(Point offset) {
        return new RecipeLayoutPart.Item(offset);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onJEIHoverTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
        if (requirement.requirementType == RequirementItem.ItemRequirementType.FUEL) {
            addFuelTooltip(ingredient, tooltip);
        }
        addChanceTooltip(input, tooltip, requirement.chance);
        addMinMaxTooltip(input, tooltip);
    }

    public static void addFuelTooltip(final ItemStack ingredient, final List<String> tooltip) {
        int burn = TileEntityFurnace.getItemBurnTime(ingredient);
        if (burn > 0) {
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.machinery.fuel.item", burn));
        }
        tooltip.add(I18n.format("tooltip.machinery.fuel"));
    }

    public void addMinMaxTooltip(final boolean input, final List<String> tooltip) {
        if (requirement.minAmount != requirement.maxAmount) {
            String key = input ? "tooltip.machinery.min_max_amount.input" : "tooltip.machinery.min_max_amount.output";
            tooltip.add(I18n.format(key, requirement.minAmount, requirement.maxAmount));
        }
    }

    public static void addChanceTooltip(final boolean input, final List<String> tooltip, final float chance) {
        if (chance < 1F && chance >= 0F) {
            String keyNever = input ? "tooltip.machinery.chance.in.never" : "tooltip.machinery.chance.out.never";
            String keyChance = input ? "tooltip.machinery.chance.in" : "tooltip.machinery.chance.out";

            if (chance == 0F) {
                tooltip.add(I18n.format(keyNever));
            } else {
                String chanceStr = chance < 0.0001F ? "< 0.01%" : MiscUtils.formatFloat(chance * 100F, 2) + "%";
                tooltip.add(I18n.format(keyChance, chanceStr));
            }
        }
    }

    public static class IngredientItemStackTransformer implements Function<ItemStack, IngredientItemStack> {

        public static final IngredientItemStackTransformer INSTANCE = new IngredientItemStackTransformer();

        private IngredientItemStackTransformer() {
        }

        @Nullable
        @Override
        public IngredientItemStack apply(@Nullable final ItemStack input) {
            return input == null ? null : new IngredientItemStack(input, input.getCount(), input.getCount(), 1.0F);
        }
    }
}
