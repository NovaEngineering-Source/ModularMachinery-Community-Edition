package kport.gugu_utils.compat.jei.components;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import kport.gugu_utils.compat.jei.LayoutWapper;
import kport.gugu_utils.compat.jei.renders.RendererMana;
import kport.gugu_utils.compat.jei.ingedients.IngredientMana;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class JEIComponentMana extends ComponentRequirement.JEIComponent<IngredientMana> {
    private final int mana;
    private final boolean pertick;
    private final int totalTick;

    public JEIComponentMana(int mana) {
        this.mana = mana;
        this.pertick = false;
        this.totalTick = 0;
    }

    public JEIComponentMana(int mana, boolean pertick, int totalTick) {
        this.mana = mana;
        this.pertick = pertick;
        this.totalTick = totalTick;
    }

    @Override
    public Class<IngredientMana> getJEIRequirementClass() {
        return IngredientMana.class;
    }

    @Override
    public List<IngredientMana> getJEIIORequirements() {
        if (this.pertick) {
            return Collections.singletonList(new IngredientMana("Mana", mana, new ResourceLocation("botania", "mana"), totalTick));
        }
        return Collections.singletonList(new IngredientMana("Mana", mana, new ResourceLocation("botania", "mana")));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RecipeLayoutPart<IngredientMana> getLayoutPart(Point point) {
        return new LayoutPart(point);
    }

    @Override
    public void onJEIHoverTooltip(int slotIndex, boolean input, IngredientMana ingredient, List<String> tooltip) {

    }

    public static class LayoutPart extends LayoutWapper<IngredientMana> {

        public LayoutPart(Point offset) {
//            super(offset, 5, 118, 0, 0, 16, 16, 5, 900);
            super(offset, 5, 102, 5, 118, 0, 0, 8, 8, 3, 200);
        }

        @Override
        public Class<IngredientMana> getLayoutTypeClass() {
            return IngredientMana.class;
        }

        @Override
        public IIngredientRenderer<IngredientMana> provideIngredientRenderer() {
            return RendererMana.INSTANCE;
        }

        @Override
        public boolean canBeScaled() {
            return false;
        }
    }
}