package kport.gugu_utils.compat.jei.components;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import kport.gugu_utils.compat.jei.LayoutWapper;
import kport.gugu_utils.compat.jei.ingedients.IngredientStarlight;
import kport.gugu_utils.compat.jei.renders.RendererStarlight;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class JEIComponentStarlight extends ComponentRequirement.JEIComponent<IngredientStarlight> {
    private final int starlight;
    private final IConstellation constellation;

    public JEIComponentStarlight(int starlight, IConstellation constellation) {
        this.starlight = starlight;
        this.constellation = constellation;
    }

    @Override
    public Class<IngredientStarlight> getJEIRequirementClass() {
        return IngredientStarlight.class;
    }

    @Override
    public List<IngredientStarlight> getJEIIORequirements() {
        return Collections.singletonList(new IngredientStarlight("Starlight", starlight, new ResourceLocation("astralsorcery", "starlight"), constellation));
    }

    @Override
    public RecipeLayoutPart<IngredientStarlight> getLayoutPart(Point offset) {
        return new LayoutPart(offset, constellation != null);
    }

    @Override
    public void onJEIHoverTooltip(int slotIndex, boolean input, IngredientStarlight ingredient, List<String> tooltip) {

    }

    public static class LayoutPart extends LayoutWapper<IngredientStarlight> {

        public LayoutPart(Point offset, boolean showConstellation) {
//            super(new Point(offset.x, offset.y - 32), 16, 80, 0, 32, 16, 16, 1, 100000);
            super(offset, 16, 16, 24, 80, 8, 16, 8, 8, 1, 80);
        }

        @Override
        public Class<IngredientStarlight> getLayoutTypeClass() {
            return IngredientStarlight.class;
        }

        @Override
        public IIngredientRenderer<IngredientStarlight> provideIngredientRenderer() {

            return RendererStarlight.INSTANCE;
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
