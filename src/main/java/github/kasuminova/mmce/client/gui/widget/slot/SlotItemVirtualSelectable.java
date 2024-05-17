package github.kasuminova.mmce.client.gui.widget.slot;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.function.Consumer;

public class SlotItemVirtualSelectable extends SlotItemVirtual {

    protected Consumer<SlotItemVirtualSelectable> onClickedListener = null;
    protected boolean clicked = false;

    public SlotItemVirtualSelectable() {
    }

    public SlotItemVirtualSelectable(final ItemStack stackInSlot) {
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

    public Consumer<SlotItemVirtualSelectable> getOnClickedListener() {
        return onClickedListener;
    }

    public SlotItemVirtualSelectable setOnClickedListener(final Consumer<SlotItemVirtualSelectable> onClickedListener) {
        this.onClickedListener = onClickedListener;
        return this;
    }

    public boolean isClicked() {
        return clicked;
    }

    public SlotItemVirtualSelectable setClicked(final boolean clicked) {
        if (this.clicked != clicked) {
            this.clicked = clicked;
            if (onClickedListener != null) {
                onClickedListener.accept(this);
            }
        }
        return this;
    }
}
