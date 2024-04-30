package github.kasuminova.mmce.client.gui.widget;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A button component with three states: Normal / Hovered / Unavailable.
 */
@SuppressWarnings("unused")
public class Button extends DynamicWidget {

    protected ResourceLocation textureLocation = null;

    protected int textureX = 0;
    protected int textureY = 0;

    protected int hoveredTextureX = 0;
    protected int hoveredTextureY = 0;

    protected int unavailableTextureX = 0;
    protected int unavailableTextureY = 0;

    protected boolean available = true;

    protected Consumer<Button> onClickedListener = null;
    protected Function<Button, List<String>> tooltipFunction = null;

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (isVisible() && textureLocation != null) {
            int texX;
            int texY;

            if (isUnavailable()) {
                texX = unavailableTextureX;
                texY = unavailableTextureY;
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
    }

    @Override
    public boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        if (isVisible() && onClickedListener != null) {
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

    public int getTextureX() {
        return textureX;
    }

    public Button setTextureX(final int textureX) {
        this.textureX = textureX;
        return this;
    }

    public int getTextureY() {
        return textureY;
    }

    public Button setTextureY(final int textureY) {
        this.textureY = textureY;
        return this;
    }

    public Button setTextureXY(final int textureX, final int textureY) {
        this.textureX = textureX;
        this.textureY = textureY;
        return this;
    }

    public int getHoveredTextureX() {
        return hoveredTextureX;
    }

    public Button setHoveredTextureX(final int hoveredTextureX) {
        this.hoveredTextureX = hoveredTextureX;
        return this;
    }

    public int getHoveredTextureY() {
        return hoveredTextureY;
    }

    public Button setHoveredTextureY(final int hoveredTextureY) {
        this.hoveredTextureY = hoveredTextureY;
        return this;
    }

    public Button setHoveredTextureXY(final int hoveredTextureX, final int hoveredTextureY) {
        this.hoveredTextureX = hoveredTextureX;
        this.hoveredTextureY = hoveredTextureY;
        return this;
    }

    public int getUnavailableTextureX() {
        return unavailableTextureX;
    }

    public Button setUnavailableTextureX(final int unavailableTextureX) {
        this.unavailableTextureX = unavailableTextureX;
        return this;
    }

    public int getUnavailableTextureY() {
        return unavailableTextureY;
    }

    public Button setUnavailableTextureY(final int unavailableTextureY) {
        this.unavailableTextureY = unavailableTextureY;
        return this;
    }

    public Button setUnavailableTextureXY(final int unavailableTextureX, final int unavailableTextureY) {
        this.unavailableTextureX = unavailableTextureX;
        this.unavailableTextureY = unavailableTextureY;
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
