package kport.gugu_utils.compat.jei.components;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import kport.gugu_utils.compat.jei.LayoutWapper;
import kport.gugu_utils.compat.jei.renders.RendererEmber;
import kport.gugu_utils.compat.jei.ingedients.IngredientEmber;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class JEIComponentEmber extends ComponentRequirement.JEIComponent<IngredientEmber> {
    private final double ember;

    public JEIComponentEmber(double ember) {
        this.ember = ember;
    }

    @Override
    public Class<IngredientEmber> getJEIRequirementClass() {
        return IngredientEmber.class;
    }

    @Override
    public List<IngredientEmber> getJEIIORequirements() {
        return Collections.singletonList(new IngredientEmber("Ember", ember, new ResourceLocation("embers", "ember")));
    }

    @Override
    public RecipeLayoutPart<IngredientEmber> getLayoutPart(Point offset) {
        return new LayoutPart(offset);
    }

    @Override
    public void onJEIHoverTooltip(int slotIndex, boolean input, IngredientEmber ingredient, List<String> tooltip) {

    }

    public static class LayoutPart extends LayoutWapper<IngredientEmber> {

        public LayoutPart(Point offset) {
            super(offset, 5, 102, 5, 102, 0, 0, 8, 16, 4, 200);
        }

        @Override
        public Class<IngredientEmber> getLayoutTypeClass() {
            return IngredientEmber.class;
        }

        @Override
        public IIngredientRenderer<IngredientEmber> provideIngredientRenderer() {

            return RendererEmber.INSTANCE;
        }

        @Override
        public void drawBackground(Minecraft mc) {

        }

        @Override
        public boolean canBeScaled() {
            return false;
        }
    }
}
