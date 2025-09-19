package kport.modularmagic.common.integration.jei.render;

import kport.modularmagic.common.integration.JeiPlugin;
import mezz.jei.api.gui.IDrawableBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AspectRenderer implements IIngredientRenderer<AspectList> {

    private static IDrawableBuilder aspectRender;
    private static Aspect           aspect;

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable AspectList ingredient) {
        if (ingredient == null) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        Color c = new Color(ingredient.getAspects()[0].getColor());
        GlStateManager.color((float) c.getRed() / 255.0F, (float) c.getGreen() / 255.0F, (float) c.getBlue() / 255.0F, 1.0F);

        if (aspectRender == null || aspect == null || aspectRender != ingredient.getAspects()[0]) {
            aspect = ingredient.getAspects()[0];
            ResourceLocation texture = aspect.getImage();
            aspectRender = JeiPlugin.GUI_HELPER.drawableBuilder(texture, 0, 0, 16, 16);
        }
        aspectRender.setTextureSize(16, 16);
        aspectRender.build().draw(minecraft, xPosition, yPosition);

        FontRenderer fr = getFontRenderer(minecraft, ingredient);
        String s = String.valueOf(ingredient.getAmount(aspect));
        fr.drawStringWithShadow(s, (float) (xPosition + 19 - 2 - fr.getStringWidth(s)), (float) (yPosition + 6 + 3), 16777215);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, AspectList ingredient, ITooltipFlag tooltipFlag) {
        List<String> tooltip = new ArrayList<String>();
        tooltip.add(ingredient.getAspects()[0].getName());
        return tooltip;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, AspectList ingredient) {
        return minecraft.fontRenderer;
    }
}
