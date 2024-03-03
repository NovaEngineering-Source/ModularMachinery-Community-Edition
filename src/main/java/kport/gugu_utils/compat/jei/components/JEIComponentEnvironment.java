package kport.gugu_utils.compat.jei.components;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import kport.gugu_utils.compat.jei.LayoutWapper;
import kport.gugu_utils.compat.jei.ingedients.IngredientEnvironment;
import kport.gugu_utils.compat.jei.renders.RendererEnvironment;
import kport.gugu_utils.common.envtypes.EnvironmentType;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class JEIComponentEnvironment extends ComponentRequirement.JEIComponent<IngredientEnvironment> {
    private final EnvironmentType type;

    public JEIComponentEnvironment(EnvironmentType type) {
        this.type = type;
    }

    public EnvironmentType getType() {
        return type;
    }

    @Override
    public Class<IngredientEnvironment> getJEIRequirementClass() {
        return IngredientEnvironment.class;
    }

    @Override
    public List<IngredientEnvironment> getJEIIORequirements() {
        return Collections.singletonList(new IngredientEnvironment("Environment", type, new ResourceLocation(ModularMachinery.MODID, "environment")));
    }

    @Override
    public RecipeLayoutPart<IngredientEnvironment> getLayoutPart(Point offset) {
        return new LayoutPart(offset);
    }

    @Override
    public void onJEIHoverTooltip(int slotIndex, boolean input, IngredientEnvironment ingredient, List<String> tooltip) {

    }

    public static class LayoutPart extends LayoutWapper<IngredientEnvironment> {

        public LayoutPart(Point offset) {
            super(offset, 16, 16, 0, 0, 4, 4, 3, 50);
        }

        @Override
        public Class<IngredientEnvironment> getLayoutTypeClass() {
            return IngredientEnvironment.class;
        }

        @Override
        public IIngredientRenderer<IngredientEnvironment> provideIngredientRenderer() {
            return RendererEnvironment.INSTANCE;
        }
    }
}
