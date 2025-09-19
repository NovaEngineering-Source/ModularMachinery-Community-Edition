package kport.modularmagic.common.integration.jei.render;

import WayofTime.bloodmagic.BloodMagic;
import WayofTime.bloodmagic.soul.EnumDemonWillType;
import kport.modularmagic.common.integration.JeiPlugin;
import kport.modularmagic.common.integration.jei.ingredient.DemonWill;
import mezz.jei.api.gui.IDrawableBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DemonWillRenderer implements IIngredientRenderer<DemonWill> {

    private static IDrawableBuilder  willCrystal;
    private static EnumDemonWillType willType;

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable DemonWill ingredient) {
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();

        if (ingredient == null) {
            return;
        }

        if ((willCrystal == null || willType == null || willType != ingredient.getWillType()) && ingredient.getWillType() != null) {
            willType = ingredient.getWillType();
            ResourceLocation texture;
            if (willType != EnumDemonWillType.DEFAULT) {
                texture = new ResourceLocation(BloodMagic.MODID, "textures/items/soulgemgrand_" + ingredient.getWillType().name + ".png");
            } else {
                texture = new ResourceLocation(BloodMagic.MODID, "textures/items/soulgemgrand.png");
            }
            willCrystal = JeiPlugin.GUI_HELPER.drawableBuilder(texture, 0, 0, 16, 16);
        }
        willCrystal.setTextureSize(16, 16);
        willCrystal.build().draw(minecraft, xPosition, yPosition);

        FontRenderer fr = getFontRenderer(minecraft, ingredient);
        String s = String.valueOf((int) ingredient.getWillAmount());
        fr.drawStringWithShadow(s, (float) (xPosition + 19 - 2 - fr.getStringWidth(s)), (float) (yPosition + 6 + 3), 16777215);

        GlStateManager.disableBlend();
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, DemonWill ingredient, ITooltipFlag tooltipFlag) {
        ArrayList<String> tooltip = new ArrayList<String>();
        String name = ingredient.getWillType().name;
        tooltip.add(name.substring(0, 1).toUpperCase() + name.substring(1) + " Will");
        return tooltip;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, DemonWill ingredient) {
        return minecraft.fontRenderer;
    }
}
