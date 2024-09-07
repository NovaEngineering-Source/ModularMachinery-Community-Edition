/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.recipe;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementIngredientArray;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import hellfirepvp.modularmachinery.common.integration.ingredient.IngredientItemStackRenderer;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.gas.GasStackRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;
import java.util.Collections;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeLayoutPart
 * Created by HellFirePvP
 * Date: 12.07.2017 / 10:59
 */
public abstract class RecipeLayoutPart<T> {

    private final Point offset;

    protected RecipeLayoutPart(Point offset) {
        this.offset = offset;
    }

    public abstract int getComponentWidth();

    public abstract int getComponentHeight();

    public final Point getOffset() {
        return offset;
    }

    public abstract Class<T> getLayoutTypeClass();

    public IIngredientRenderer<T> provideIngredientRenderer() {
        return ModIntegrationJEI.ingredientRegistry.getIngredientRenderer(getLayoutTypeClass());
    }

    public IIngredientRenderer<T> provideIngredientRenderer(final ComponentRequirement<?, ?> req) {
        return provideIngredientRenderer();
    }

    public abstract int getRendererPaddingX();

    public abstract int getRendererPaddingY();

    //Defines how many of them can be placed next to each other horizontally, before
    //a new 'line' is used for more.
    public abstract int getMaxHorizontalCount();

    public int getMaxHorizontalCount(int partAmount) {
        return getMaxHorizontalCount();
    }

    public abstract int getComponentHorizontalGap();

    public abstract int getComponentVerticalGap();

    //The higher number, the more left (for inputs) and the more right (for outputs) the component is gonna appear.
    //Should be unique/final depending on component type and NOT vary between different recipe instances or components!!

    //Defaults:
    //1000 is energy
    //100 is fluids/mek gases
    //10 is items
    public abstract int getComponentHorizontalSortingOrder();

    @Deprecated
    public abstract boolean canBeScaled();

    public abstract void drawBackground(Minecraft mc);

    public static class FluidTank extends RecipeLayoutPart<FluidStack> {

        public FluidTank(Point offset) {
            super(offset);
        }

        @Override
        public int getComponentHeight() {
            return 18;
        }

        @Override
        public int getComponentWidth() {
            return 18;
        }

        @Override
        public Class<FluidStack> getLayoutTypeClass() {
            return FluidStack.class;
        }

        @Override
        public int getComponentHorizontalGap() {
            return 0;
        }

        @Override
        public int getComponentVerticalGap() {
            return 0;
        }

        @Override
        public int getMaxHorizontalCount() {
            return 1;
        }

        @Override
        public int getMaxHorizontalCount(final int partAmount) {
            return Math.max((int) Math.ceil((double) partAmount / 4), 1);
        }

        @Override
        public int getComponentHorizontalSortingOrder() {
            return 100;
        }

        @Override
        public boolean canBeScaled() {
            return false;
        }

        @Override
        public IIngredientRenderer<FluidStack> provideIngredientRenderer() {
            return new FluidStackRenderer(1, false, getComponentWidth(), getComponentHeight(), RecipeLayoutHelper.PART_TANK_SHELL.drawable);
        }

        @Override
        public int getRendererPaddingX() {
            return 0;
        }

        @Override
        public int getRendererPaddingY() {
            return 0;
        }

        @Override
        public void drawBackground(Minecraft mc) {
            RecipeLayoutHelper.PART_TANK_SHELL_BACKGROUND.drawable.draw(mc, getOffset().x, getOffset().y);
        }
    }

    public static class GasTank extends RecipeLayoutPart<GasStack> {

        public GasTank(Point offset) {
            super(offset);
        }

        @Override
        public int getComponentHeight() {
            return 18;
        }

        @Override
        public int getComponentWidth() {
            return 18;
        }

        @Override
        public IIngredientRenderer<GasStack> provideIngredientRenderer() {
            return new GasStackRenderer(1, false, getComponentWidth(), getComponentHeight(), RecipeLayoutHelper.PART_GAS_TANK_SHELL.drawable);
        }

        @Override
        public Class<GasStack> getLayoutTypeClass() {
            return GasStack.class;
        }

        @Override
        public int getComponentHorizontalGap() {
            return 0;
        }

        @Override
        public int getComponentVerticalGap() {
            return 0;
        }

        @Override
        public int getMaxHorizontalCount() {
            return 1;
        }

        @Override
        public int getMaxHorizontalCount(final int partAmount) {
            return Math.max((int) Math.ceil((double) partAmount / 4), 1);
        }

        @Override
        public int getComponentHorizontalSortingOrder() {
            return 100;
        }

        @Override
        public boolean canBeScaled() {
            return false;
        }

        @Override
        public int getRendererPaddingX() {
            return 0;
        }

        @Override
        public int getRendererPaddingY() {
            return 0;
        }

        @Override
        public void drawBackground(Minecraft mc) {
            RecipeLayoutHelper.PART_TANK_SHELL_BACKGROUND.drawable.draw(mc, getOffset().x, getOffset().y);
        }
    }

    public static class Energy extends RecipeLayoutPart<Long> {

        public Energy(Point offset) {
            super(offset);
        }

        @Override
        public int getComponentWidth() {
            return 18;
        }

        @Override
        public int getComponentHeight() {
            return 63;
        }

        @Override
        public Class<Long> getLayoutTypeClass() {
            return Long.class;
        }

        @Override
        public int getMaxHorizontalCount() {
            return 1;
        }

        @Override
        public int getComponentHorizontalGap() {
            return 0;
        }

        @Override
        public int getComponentVerticalGap() {
            return 0;
        }

        @Override
        public int getComponentHorizontalSortingOrder() {
            return 1000;
        }

        @Override
        public boolean canBeScaled() {
            return true;
        }

        @Override
        public IIngredientRenderer<Long> provideIngredientRenderer() {
            throw new UnsupportedOperationException("Cannot provide Energy ingredientrenderer as this is no ingredient!");
        }

        @Override
        public int getRendererPaddingX() {
            return 0;
        }

        @Override
        public int getRendererPaddingY() {
            return 0;
        }

        @Override
        public void drawBackground(Minecraft mc) {
            RecipeLayoutHelper.PART_ENERGY_BACKGROUND.drawable.draw(mc, getOffset().x, getOffset().y);
        }

        public void drawEnergy(Minecraft mc, Long energy) {
            if (energy > 0) {
                RecipeLayoutHelper.PART_ENERGY_FOREGROUND.drawable.draw(mc, getOffset().x, getOffset().y);
            }
        }

    }

    public static class Item extends RecipeLayoutPart<ItemStack> {

        public Item(Point offset) {
            super(offset);
        }

        @Override
        public int getComponentHeight() {
            return 18;
        }

        @Override
        public int getComponentWidth() {
            return 18;
        }

        @Override
        public Class<ItemStack> getLayoutTypeClass() {
            return ItemStack.class;
        }

        @Override
        public int getMaxHorizontalCount() {
            return 4;
        }

        @Override
        public int getMaxHorizontalCount(final int partAmount) {
            if (partAmount <= 3) {
                return 1;
            }

            if (partAmount == 4) {
                return 2;
            }

            if (partAmount <= 9) {
                return 3;
            }

            if (partAmount == 12) {
                return 4;
            }

            int sqrt = (int) Math.round(Math.sqrt(partAmount));
            if (partAmount % sqrt == 0) {
                return sqrt;
            }

            int range = sqrt <= 3 ? sqrt : sqrt / 2;
            for (int i = 1; i < range; i++) {
                if (partAmount % (sqrt + i) == 0) {
                    return sqrt + i;
                }
                if (partAmount % (sqrt - i) == 0) {
                    return sqrt - i;
                }
            }

            return sqrt;
        }

        @Override
        public int getComponentVerticalGap() {
            return 0;
        }

        @Override
        public int getComponentHorizontalGap() {
            return 0;
        }

        @Override
        public int getComponentHorizontalSortingOrder() {
            return 10;
        }

        @Override
        public boolean canBeScaled() {
            return false;
        }

        @Override
        public IIngredientRenderer<ItemStack> provideIngredientRenderer() {
            return new IngredientItemStackRenderer(Collections.emptyList());
        }

        @Override
        public IIngredientRenderer<ItemStack> provideIngredientRenderer(final ComponentRequirement<?, ?> req) {
            if (req instanceof RequirementItem reqItem) {
                return new IngredientItemStackRenderer(reqItem.cachedJEIIORequirementList);
            }
            if (req instanceof RequirementIngredientArray ingredientArray) {
                return new IngredientItemStackRenderer(ingredientArray.cachedJEIIORequirementList);
            }
            return super.provideIngredientRenderer(req);
        }

        @Override
        public int getRendererPaddingX() {
            return 1;
        }

        @Override
        public int getRendererPaddingY() {
            return 1;
        }

        @Override
        public void drawBackground(Minecraft mc) {
            RecipeLayoutHelper.PART_INVENTORY_CELL.drawable.draw(mc, getOffset().x, getOffset().y);
        }
    }

}
