/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.recipe;

import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementIngredientArray;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluid;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluidRenderer;
import hellfirepvp.modularmachinery.common.integration.ingredient.IngredientItemStack;
import hellfirepvp.modularmachinery.common.integration.ingredient.IngredientItemStackRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

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

    public abstract IIngredientRenderer<T> provideIngredientRenderer();

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

    public static class Tank extends RecipeLayoutPart<HybridFluid> {

        public Tank(Point offset) {
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
        public Class<HybridFluid> getLayoutTypeClass() {
            return HybridFluid.class;
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
        public IIngredientRenderer<HybridFluid> provideIngredientRenderer() {
            HybridFluidRenderer<HybridFluid> copy = new HybridFluidRenderer<>().
                    copyPrepareFluidRender(
                            getComponentWidth(), getComponentHeight(),
                            1, false,
                            RecipeLayoutHelper.PART_TANK_SHELL.drawable);
            if (Mods.MEKANISM.isPresent()) {
                copy = addGasRenderer(copy);
            }
            return copy;
        }

        @Override
        public int getRendererPaddingX() {
            return 0;
        }

        @Override
        public int getRendererPaddingY() {
            return 0;
        }

        @Optional.Method(modid = "mekanism")
        private HybridFluidRenderer<HybridFluid> addGasRenderer(HybridFluidRenderer<HybridFluid> copy) {
            return copy.copyPrepareGasRender(
                    getComponentWidth(), getComponentHeight(),
                    1, false,
                    RecipeLayoutHelper.PART_GAS_TANK_SHELL.drawable);
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
