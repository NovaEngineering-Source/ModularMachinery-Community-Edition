package github.kasuminova.mmce.client.gui.widget;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Inherits the normal button with an additional press state.
 */
@SuppressWarnings("unused")
public class Button4State extends Button {

    protected TextureProperties mouseDownTexture = TextureProperties.EMPTY;

    protected boolean mouseDown = false;

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (!isVisible()) {
            return;
        }
        if (isUnavailable()) {
            unavailableTexture.render(textureLocation, renderPos, renderSize, gui);
            return;
        }
        if (mouseDown) {
            mouseDownTexture.render(textureLocation, renderPos, renderSize, gui);
            return;
        }
        if (isMouseOver(mousePos)) {
            hoveredTexture.render(textureLocation, renderPos, renderSize, gui);
            return;
        }
        texture.render(textureLocation, renderPos, renderSize, gui);
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

    public Button4State setMouseDownTexture(final int mouseDownTextureX, final int mouseDownTextureY) {
        return setMouseDownTexture(TextureProperties.of(mouseDownTextureX, mouseDownTextureY));
    }

    public TextureProperties getMouseDownTexture() {
        return mouseDownTexture;
    }

    public Button4State setMouseDownTexture(final TextureProperties mouseDownTexture) {
        this.mouseDownTexture = Optional.ofNullable(mouseDownTexture).orElse(TextureProperties.EMPTY);
        return this;
    }

    public boolean isMouseDown() {
        return mouseDown;
    }

}
