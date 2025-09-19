package github.kasuminova.mmce.client.gui.widget.slot;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.Optional;

public abstract class SlotVirtual extends DynamicWidget {

    protected TextureProperties slotTex = TextureProperties.EMPTY;

    protected boolean mouseOver = false;

    public SlotVirtual() {
        setWidthHeight(18, 18);
    }

    protected void drawHoverOverlay(final MousePos mousePos, final int rx, final int ry) {
        if (mouseOver) {
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            GuiScreen.drawRect(rx, ry, rx + 16, ry + 16, new Color(255, 255, 255, 150).getRGB());
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    // Texture Location.

    public TextureProperties getSlotTex() {
        return slotTex;
    }

    public SlotVirtual setSlotTex(final TextureProperties slotTex) {
        this.slotTex = Optional.ofNullable(slotTex).orElse(TextureProperties.EMPTY);
        return this;
    }

}