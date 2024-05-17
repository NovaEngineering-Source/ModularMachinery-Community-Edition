package github.kasuminova.mmce.client.gui.widget.slot;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public abstract class SlotVirtual extends DynamicWidget {

    protected ResourceLocation slotTexLocation = null;
    protected int slotTexX = 0;
    protected int slotTexY = 0;

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
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    // Texture Location.

    public SlotVirtual setSlotTexLocation(final ResourceLocation slotTexLocation) {
        this.slotTexLocation = slotTexLocation;
        return this;
    }

    public SlotVirtual setSlotTexX(final int slotTexX) {
        this.slotTexX = slotTexX;
        return this;
    }

    public SlotVirtual setSlotTexY(final int slotTexY) {
        this.slotTexY = slotTexY;
        return this;
    }

}