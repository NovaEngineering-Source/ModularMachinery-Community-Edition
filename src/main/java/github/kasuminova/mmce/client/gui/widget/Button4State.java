package github.kasuminova.mmce.client.gui.widget;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;

import java.util.Collections;
import java.util.List;

/**
 * Inherits the normal button with an additional press state.
 */
@SuppressWarnings("unused")
public class Button4State extends Button {

    protected int mouseDownTextureX = 0;
    protected int mouseDownTextureY = 0;

    protected boolean mouseDown = false;

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (!isVisible() || textureLocation == null) {
            return;
        }

        int texX;
        int texY;

        if (isUnavailable()) {
            texX = unavailableTextureX;
            texY = unavailableTextureY;
        } else if (mouseDown) {
            texX = mouseDownTextureX;
            texY = mouseDownTextureY;
        } else if (isMouseOver(mousePos)) {
            texX = hoveredTextureX;
            texY = hoveredTextureY;
        } else {
            texX = textureX;
            texY = textureY;
        }

        gui.getGui().mc.getTextureManager().bindTexture(textureLocation);
        gui.getGui().drawTexturedModalRect(renderPos.posX(), renderPos.posY(), texX, texY, width, height);
    }

    @Override
    public boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        if (isVisible() && isAvailable() && mouseButton == 0) {
            return mouseDown = true;
        }
        return super.onMouseClick(mousePos, renderPos, mouseButton);
    }

    @Override
    public boolean onMouseReleased(final MousePos mousePos, final RenderPos renderPos) {
        if (isVisible() && isMouseOver(mousePos) && mouseDown) {
            mouseDown = false;
            if (onClickedListener != null) {
                onClickedListener.accept(this);
            }
            return true;
        }
        mouseDown = false;
        return false;
    }

    @Override
    public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
        if (mouseDown) {
            return Collections.emptyList();
        }
        return super.getHoverTooltips(widgetGui, mousePos);
    }

    public Button4State setMouseDownTextureXY(final int mouseDownTextureX, final int mouseDownTextureY) {
        this.mouseDownTextureX = mouseDownTextureX;
        this.mouseDownTextureY = mouseDownTextureY;
        return this;
    }

    public int getMouseDownTextureX() {
        return mouseDownTextureX;
    }

    public Button4State setMouseDownTextureX(final int mouseDownTextureX) {
        this.mouseDownTextureX = mouseDownTextureX;
        return this;
    }

    public int getMouseDownTextureY() {
        return mouseDownTextureY;
    }

    public Button4State setMouseDownTextureY(final int mouseDownTextureY) {
        this.mouseDownTextureY = mouseDownTextureY;
        return this;
    }
}
