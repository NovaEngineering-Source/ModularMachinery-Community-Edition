package kport.gugu_utils.compat.jei.components;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import kport.gugu_utils.compat.jei.LayoutWapper;
import kport.gugu_utils.compat.jei.ingedients.IngredientCompressedAir;
import kport.gugu_utils.compat.jei.renders.RendererCompressedAir;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class JEIComponentCompressedAir extends ComponentRequirement.JEIComponent<IngredientCompressedAir> {
    private final int air;
    private final float pressure;
    private final boolean pertick;
    private final int totalTick;

    public JEIComponentCompressedAir(int air, float pressure, boolean pertick, int totalTick) {
        this.air = air;
        this.pressure = pressure;
        this.pertick = pertick;
        this.totalTick = totalTick;
    }

    public JEIComponentCompressedAir(int air, float pressure) {
        this(air, pressure, false, 0);
    }


    @Override
    public Class<IngredientCompressedAir> getJEIRequirementClass() {
        return IngredientCompressedAir.class;
    }

    @Override
    public List<IngredientCompressedAir> getJEIIORequirements() {
        if (this.pertick) {
            return Collections.singletonList(new IngredientCompressedAir("Compressed Air", air, new ResourceLocation("pneumaticcraft", "compressed_air"), pressure, totalTick));
        }
        return Collections.singletonList(new IngredientCompressedAir("Compressed Air", air, new ResourceLocation("pneumaticcraft", "compressed_air"), pressure));
    }

    @Override
    public RecipeLayoutPart<IngredientCompressedAir> getLayoutPart(Point offset) {
        return new LayoutPart(offset);
    }

    @Override
    public void onJEIHoverTooltip(int slotIndex, boolean input, IngredientCompressedAir ingredient, List<String> tooltip) {

    }

    public static class LayoutPart extends LayoutWapper<IngredientCompressedAir> {

        public LayoutPart(Point offset) {
//            super(new Point(offset.x, offset.y - 32), 16, 80, 0, 32, 16, 16, 1, 100000);
            super(offset, 40, 40, 50, 50, 5, 5, 8, 8, 1, 80);
        }

        @Override
        public Class<IngredientCompressedAir> getLayoutTypeClass() {
            return IngredientCompressedAir.class;
        }

        @Override
        public IIngredientRenderer<IngredientCompressedAir> provideIngredientRenderer() {
            return RendererCompressedAir.INSTANCE;

//            return RendererStarlight.INSTANCE;
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
