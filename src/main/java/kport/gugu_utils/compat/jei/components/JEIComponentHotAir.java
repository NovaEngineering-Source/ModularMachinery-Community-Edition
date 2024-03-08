package kport.gugu_utils.compat.jei.components;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import kport.gugu_utils.compat.jei.LayoutWapper;
import kport.gugu_utils.compat.jei.ingedients.IngredientHotAir;
import kport.gugu_utils.compat.jei.renders.RendererHotAir;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class JEIComponentHotAir extends ComponentRequirement.JEIComponent<IngredientHotAir> {

    //最低工作温度
    private int minTemperature;

    //最高工作温度
    private int maxTemperature;
    //热量消耗
    private int heat;


    public JEIComponentHotAir(int minTemperature, int maxTemperature, int heat) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.heat = heat;
    }

    @Override
    public Class<IngredientHotAir> getJEIRequirementClass() {
        return IngredientHotAir.class;
    }

    @Override
    public List<IngredientHotAir> getJEIIORequirements() {
        return Collections.singletonList(new IngredientHotAir("Hot Air", heat, new ResourceLocation("prodigytech", "hotair"), minTemperature,maxTemperature));

    }

    @Override
    public RecipeLayoutPart<IngredientHotAir> getLayoutPart(Point offset) {
        return new LayoutPart(offset);
    }

    @Override
    public void onJEIHoverTooltip(int slotIndex, boolean input, IngredientHotAir ingredient, List<String> tooltip) {

    }

    public static class LayoutPart extends LayoutWapper<IngredientHotAir> {
        public LayoutPart(Point offset) {
            super(offset, 16, 16, 18, 18, 1, 1, 1, 1, 3, 60);
        }

        @Override
        public Class<IngredientHotAir> getLayoutTypeClass() {
            return IngredientHotAir.class;
        }

        @Override
        public IIngredientRenderer<IngredientHotAir> provideIngredientRenderer() {
            return RendererHotAir.INSTANCE;
        }
    }
}
