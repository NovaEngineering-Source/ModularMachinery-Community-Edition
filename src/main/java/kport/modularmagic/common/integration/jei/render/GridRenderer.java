package kport.modularmagic.common.integration.jei.render;

import com.rwtema.extrautils2.ExtraUtils2;
import kport.modularmagic.common.integration.JeiPlugin;
import kport.modularmagic.common.integration.jei.ingredient.Grid;
import mezz.jei.api.gui.IDrawableBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GridRenderer implements IIngredientRenderer<Grid> {

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable Grid ingredient) {

        ResourceLocation texture = new ResourceLocation(ExtraUtils2.MODID, "textures/eye_redstone.png");
        IDrawableBuilder builder = JeiPlugin.GUI_HELPER.drawableBuilder(texture, 0, 0, 16, 16);
        builder.setTextureSize(16, 16);
        builder.build().draw(minecraft, xPosition, yPosition);
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, Grid ingredient, ITooltipFlag tooltipFlag) {
        List<String> tooltip = new ArrayList<>();
        if (ingredient.getPower() > 0) {
            tooltip.add(I18n.format("info.modularmachinery.grid.required", ingredient.getPower()));
        } else {
            tooltip.add(I18n.format("info.modularmachinery.grid.produced", Math.abs(ingredient.getPower())));
        }
        return tooltip;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, Grid ingredient) {
        return minecraft.fontRenderer;
    }
}
