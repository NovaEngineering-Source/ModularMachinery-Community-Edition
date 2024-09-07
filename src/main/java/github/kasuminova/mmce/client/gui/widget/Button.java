package github.kasuminova.mmce.client.gui.widget;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A button component with three states: Normal / Hovered / Unavailable.
 */
@SuppressWarnings("unused")
public class Button extends DynamicWidget {

    protected ResourceLocation textureLocation = null;
    protected TextureProperties texture = TextureProperties.EMPTY;
    protected TextureProperties hoveredTexture = TextureProperties.EMPTY;
    protected TextureProperties unavailableTexture= TextureProperties.EMPTY;

    protected boolean available = true;

    protected Consumer<Button> onClickedListener = null;
    protected Function<Button, List<String>> tooltipFunction = null;

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (!isVisible()) {
            return;
        }
        if (isUnavailable()) {
            unavailableTexture.render(textureLocation, renderPos, renderSize, gui);
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
        if (isVisible() && onClickedListener != null && mouseButton == 0) {
            onClickedListener.accept(this);
            return true;
        }
        return false;
    }

    // Tooltips

    @Override
    public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
        if (available && tooltipFunction != null) {
            return tooltipFunction.apply(this);
        }
        return super.getHoverTooltips(widgetGui, mousePos);
    }

    // Getter Setter

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public Button setTextureLocation(final ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
        return this;
    }

    public Button setTexture(final int textureX, final int textureY) {
        return setTexture(TextureProperties.of(textureX, textureY));
    }

    public Button setHoveredTexture(final int hoveredTextureX, final int hoveredTextureY) {
        return setHoveredTexture(TextureProperties.of(hoveredTextureX, hoveredTextureY));
    }

    public Button setUnavailableTexture(final int unavailableTextureX, final int unavailableTextureY) {
        return setUnavailableTexture(TextureProperties.of(unavailableTextureX, unavailableTextureY));
    }

    public TextureProperties getTexture() {
        return texture;
    }

    public Button setTexture(final TextureProperties texture) {
        this.texture = Optional.ofNullable(texture).orElse(TextureProperties.EMPTY);
        return this;
    }

    public TextureProperties getHoveredTexture() {
        return hoveredTexture;
    }

    public Button setHoveredTexture(final TextureProperties hoveredTexture) {
        this.hoveredTexture = Optional.ofNullable(hoveredTexture).orElse(TextureProperties.EMPTY);
        return this;
    }

    public TextureProperties getUnavailableTexture() {
        return unavailableTexture;
    }

    public Button setUnavailableTexture(final TextureProperties unavailableTexture) {
        this.unavailableTexture = Optional.ofNullable(unavailableTexture).orElse(TextureProperties.EMPTY);
        return this;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isUnavailable() {
        return !available;
    }

    public Button setAvailable(final boolean available) {
        this.available = available;
        return this;
    }

    public Button setAvailable() {
        this.available = true;
        return this;
    }

    public Button setUnavailable() {
        this.available = false;
        return this;
    }

    public Consumer<Button> getOnClickedListener() {
        return onClickedListener;
    }

    public Button setOnClickedListener(final Consumer<Button> onClickedListener) {
        this.onClickedListener = onClickedListener;
        return this;
    }

    public Function<Button, List<String>> getTooltipFunction() {
        return tooltipFunction;
    }

    public Button setTooltipFunction(final Function<Button, List<String>> tooltipFunction) {
        this.tooltipFunction = tooltipFunction;
        return this;
    }
}
