package kport.modularmagic.common.integration.jei.render;

import WayofTime.bloodmagic.BloodMagic;
import kport.modularmagic.common.integration.JeiPlugin;
import kport.modularmagic.common.integration.jei.ingredient.LifeEssence;
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

public class LifeEssenceRenderer implements IIngredientRenderer<LifeEssence> {

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable LifeEssence ingredient) {

        ResourceLocation texture = new ResourceLocation(BloodMagic.MODID, "textures/items/weakbloodorb.png");
        IDrawableBuilder builder = JeiPlugin.GUI_HELPER.drawableBuilder(texture, 0, 0, 16, 16);
        builder.setTextureSize(16, 16);
        builder.build().draw(minecraft, xPosition, yPosition);
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, LifeEssence ingredient, ITooltipFlag tooltipFlag) {
        List<String> tooltip = new ArrayList<>();
        tooltip.add(I18n.format("info.modularmachinery.lifeessence"));
        if (!ingredient.isPerTick()) {
            tooltip.add(ingredient.getEssenceAmount() + I18n.format("info.modularmachinery.lp"));
        } else {
            tooltip.add(ingredient.getEssenceAmount() + I18n.format("info.modularmachinery.lppertick"));
        }
        return tooltip;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, LifeEssence ingredient) {
        return minecraft.fontRenderer;
    }
}
