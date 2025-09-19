package kport.modularmagic.common.integration.jei.render;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import kport.modularmagic.common.integration.JeiPlugin;
import kport.modularmagic.common.integration.jei.ingredient.Mana;
import mezz.jei.api.gui.IDrawableBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class ManaRenderer implements IIngredientRenderer<Mana> {

    private static IDrawableBuilder manaBar;
    private static IDrawableBuilder manaBarFilled;

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable Mana ingredient) {
        if (ingredient == null) {
            return;
        }
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();

        ResourceLocation texture;
        texture = new ResourceLocation(ModularMachinery.MODID, "textures/gui/widgets.png");
        manaBar = JeiPlugin.GUI_HELPER.drawableBuilder(texture, 0, 0, 5, 63);
        manaBarFilled = JeiPlugin.GUI_HELPER.drawableBuilder(texture, 5, 0, 3, 61);

        GlStateManager.color(1F, 1F, 1F, 1F);
        manaBar.build().draw(minecraft, xPosition, yPosition);

        int manaPercentage = Math.max(0, (int) ((double) ingredient.manaAmount() / (double) 10000 * 61));

        if (manaPercentage == 0 && ingredient.manaAmount() > 0) {
            manaPercentage = 1;
        }

        manaBarFilled.build().draw(minecraft, xPosition + 1, yPosition + 1);

        GlStateManager.enableAlpha();
        Color color = new Color(Color.HSBtoRGB(0.55F, (float) Math.min(1F, Math.sin(System.currentTimeMillis() / 200D) * 0.5 + 1F), 1F));
        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) (255F * 1F));
        manaBarFilled.build().draw(minecraft, xPosition + 1, yPosition + 1, MathHelper.clamp(61 - manaPercentage, 0, 61), 0, 0, 0);
        GL11.glColor4ub((byte) 255, (byte) 255, (byte) 255, (byte) 255);

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, Mana ingredient, ITooltipFlag tooltipFlag) {
        return Lists.newArrayList("Mana");
    }

}
