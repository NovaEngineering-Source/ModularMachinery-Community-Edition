package github.kasuminova.mmce.client.gui.widget.slot;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.function.Consumer;

public class SlotVirtualSelectable extends SlotVirtual {

    protected Consumer<SlotVirtualSelectable> onClickedListener = null;
    protected boolean clicked = false;

    public SlotVirtualSelectable() {
    }

    public SlotVirtualSelectable(final ItemStack stackInSlot) {
        super(stackInSlot);
    }

    @Override
    protected void drawHoverOverlay(final MousePos mousePos, final int rx, final int ry) {
        if (clicked) {
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            GuiScreen.drawRect(rx, ry, rx + 16, ry + 16, new Color(0, 255, 127, 75).getRGB());
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        } else {
            super.drawHoverOverlay(mousePos, rx, ry);
        }
    }

    @Override
    public boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        clicked = !clicked;
        if (onClickedListener != null) {
            onClickedListener.accept(this);
        }
        return true;
    }

    public Consumer<SlotVirtualSelectable> getOnClickedListener() {
        return onClickedListener;
    }

    public SlotVirtualSelectable setOnClickedListener(final Consumer<SlotVirtualSelectable> onClickedListener) {
        this.onClickedListener = onClickedListener;
        return this;
    }

    public boolean isClicked() {
        return clicked;
    }

    public SlotVirtualSelectable setClicked(final boolean clicked) {
        if (this.clicked != clicked) {
            this.clicked = clicked;
            if (onClickedListener != null) {
                onClickedListener.accept(this);
            }
        }
        return this;
    }
}
