package github.kasuminova.mmce.client.gui.widget;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;

import java.util.Optional;

/**
 * Inherits the 4 states of the button with the addition of a persistent press state.
 */
@SuppressWarnings("unused")
public class Button5State extends Button4State {

    protected TextureProperties clickedTexture = TextureProperties.EMPTY;

    protected boolean clicked = false;

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (!isVisible()) {
            return;
        }
        if (isUnavailable()) {
            unavailableTexture.render(textureLocation, renderPos, renderSize, gui);
            return;
        }
        if (clicked) {
            clickedTexture.render(textureLocation, renderPos, renderSize, gui);
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
    public boolean onMouseReleased(final MousePos mousePos, final RenderPos renderPos) {
        if (isVisible() && isMouseOver(mousePos) && mouseDown) {
            mouseDown = false;
            clicked = !clicked;
            if (onClickedListener != null) {
                onClickedListener.accept(this);
            }
            return true;
        }
        mouseDown = false;
        return false;
    }

    public Button5State setClickedTexture(final int clickedTextureX, final int clickedTextureY) {
        return setClickedTexture(TextureProperties.of(clickedTextureX, clickedTextureY));
    }

    public TextureProperties getClickedTexture() {
        return clickedTexture;
    }

    public Button5State setClickedTexture(final TextureProperties clickedTexture) {
        this.clickedTexture = Optional.ofNullable(clickedTexture).orElse(TextureProperties.EMPTY);;
        return this;
    }

    public boolean isClicked() {
        return clicked;
    }

    public Button5State setClicked(final boolean clicked) {
        this.clicked = clicked;
        return this;
    }
}
