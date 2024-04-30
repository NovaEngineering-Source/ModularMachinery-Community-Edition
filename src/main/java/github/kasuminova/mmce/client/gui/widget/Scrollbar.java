package github.kasuminova.mmce.client.gui.widget;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Scrollbar extends DynamicWidget {
    public static final int DEFAULT_SCROLL_WIDTH = 6;
    public static final int DEFAULT_SCROLL_HEIGHT = 27;

    public static final int DEFAULT_TEXTURE_X = 0;
    public static final int DEFAULT_TEXTURE_Y = 22;

    public static final int DEFAULT_TEXTURE_OFFSET_X = 6;
    public static final int DEFAULT_TEXTURE_OFFSET_Y = 0;

    public static final int DEFAULT_SCROLL_UNIT = 1;

    protected final Button4State scroll = new Button4State();

    protected int maxScroll = 0;
    protected int minScroll = 0;
    protected int currentScroll = 0;

    protected int scrollUnit = DEFAULT_SCROLL_UNIT;

    protected boolean mouseDown = false;

    protected boolean mouseWheelCheckPos = true;

    protected Consumer<Scrollbar> onValueChanged = null;

    public Scrollbar() {
        this.width = scroll.getWidth();
        this.height = scroll.getHeight() * 2;
    }

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        int height = renderSize.isHeightLimited() ? renderSize.height() : this.height;
        int scrollHeight = scroll.getHeight();
        int scrollWidth = scroll.getWidth();

        if (this.getRange() == 0) {
            scroll.render(gui, new RenderSize(scrollWidth, scrollHeight), renderPos, mousePos);
        } else {
            int offsetY = (this.currentScroll - this.minScroll) * (height - scrollHeight) / this.getRange();
            scroll.render(gui,
                    new RenderSize(scrollWidth, scrollHeight),
                    renderPos.add(new RenderPos(0, offsetY)),
                    mousePos.relativeTo(new RenderPos(0, offsetY)));
        }
    }

    @Override
    public boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        if (this.getRange() == 0) {
            return false;
        }

        int scrollHeight = scroll.getHeight();
        int offsetY = getRange() == 0 ? 0 : (this.currentScroll - this.minScroll) * (height - scrollHeight) / this.getRange();
        RenderPos offset = new RenderPos(0, offsetY);
        MousePos scrollMousePos = mousePos.relativeTo(offset);
        if (scroll.isMouseOver(scrollMousePos) && scroll.onMouseClick(mousePos, renderPos.subtract(offset), mouseButton)) {
            return mouseDown = true;
        }

        return false;
    }

    @Override
    public boolean onMouseClickMove(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        if (mouseDown) {
            handleMouseDragMove(mousePos);
        }
        return super.onMouseClickMove(mousePos, renderPos, mouseButton);
    }

    protected void handleMouseDragMove(final MousePos mousePos) {
        float clickedPercent = Math.min(Math.max((float) mousePos.mouseY() / this.height, 0), 1F);
        int scroll = Math.round((float) getRange() * clickedPercent);
        setCurrentScroll(scroll + this.minScroll);
    }

    @Override
    public boolean onMouseReleased(final MousePos mousePos, final RenderPos renderPos) {
        int scrollHeight = scroll.getHeight();
        int offsetY = getRange() == 0 ? 0 : (this.currentScroll - this.minScroll) * (height - scrollHeight) / this.getRange();
        RenderPos offset = new RenderPos(0, offsetY);
        MousePos scrollMousePos = mousePos.relativeTo(offset);
        scroll.onMouseReleased(mousePos, renderPos.subtract(offset));

        mouseDown = false;
        return false;
    }

    @Override
    public boolean onMouseDWheel(final MousePos mousePos, final RenderPos renderPos, final int dWheel) {
        if (mouseWheelCheckPos) {
            if (!isMouseOver(mousePos)) {
                return false;
            }
        }
        int wheel = Math.max(Math.min(-dWheel, 1), -1);
        setCurrentScroll(this.currentScroll + (wheel * this.scrollUnit));
        return true;
    }

    @Override
    public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
        int scrollHeight = scroll.getHeight();
        int offsetY = getRange() == 0 ? 0 : (this.currentScroll - this.minScroll) * (height - scrollHeight) / this.getRange();
        RenderPos offset = new RenderPos(0, offsetY);
        MousePos scrollMousePos = mousePos.relativeTo(offset);
        if (scroll.isMouseOver(scrollMousePos)) {
            return scroll.getHoverTooltips(widgetGui, mousePos);
        }

        return super.getHoverTooltips(widgetGui, mousePos);
    }

    // Scroll button

    public Button4State getScroll() {
        return scroll;
    }

    // Scroll Range

    public int getMaxScroll() {
        return maxScroll;
    }

    public int getMinScroll() {
        return minScroll;
    }

    public int getCurrentScroll() {
        return currentScroll;
    }

    public Scrollbar setCurrentScroll(final int currentScroll) {
        this.currentScroll = Math.max(Math.min(currentScroll, this.maxScroll), this.minScroll);
        if (onValueChanged != null) {
            onValueChanged.accept(this);
        }
        return this;
    }

    public int getRange() {
        return this.maxScroll - this.minScroll;
    }

    public Scrollbar setRange(final int min, final int max) {
        this.minScroll = min;
        this.maxScroll = max;

        if (this.minScroll > this.maxScroll) {
            this.maxScroll = this.minScroll;
        }
        this.scroll.setAvailable(getRange() != 0);

        setCurrentScroll(this.currentScroll);
        return this;
    }

    // Scroll Unit

    public int getScrollUnit() {
        return scrollUnit;
    }

    public Scrollbar setScrollUnit(final int scrollUnit) {
        this.scrollUnit = Math.max(scrollUnit, 1);
        return this;
    }

    // Handlers

    public Consumer<Scrollbar> getOnValueChanged() {
        return onValueChanged;
    }

    public Scrollbar setOnValueChanged(final Consumer<Scrollbar> onValueChanged) {
        this.onValueChanged = onValueChanged;
        return this;
    }

    // Handle mouse wheel

    public boolean isMouseWheelCheckPos() {
        return mouseWheelCheckPos;
    }

    public Scrollbar setMouseWheelCheckPos(final boolean mouseWheelCheckPos) {
        this.mouseWheelCheckPos = mouseWheelCheckPos;
        return this;
    }
}
