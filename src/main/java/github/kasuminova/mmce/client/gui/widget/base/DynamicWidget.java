package github.kasuminova.mmce.client.gui.widget.base;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public abstract class DynamicWidget {
    protected int width = 0;
    protected int height = 0;

    protected int absX;
    protected int absY;
    protected boolean useAbsPos = false;

    protected int marginLeft = 0;
    protected int marginRight = 0;
    protected int marginUp = 0;
    protected int marginDown = 0;

    protected boolean visible = true;
    protected boolean enabled = true;

    public void initWidget(WidgetGui gui) {
    }

    // Widget Render

    public void preRender(WidgetGui gui, RenderSize renderSize, RenderPos renderPos, MousePos mousePos) {
    }

    public abstract void render(WidgetGui gui, RenderSize renderSize, RenderPos renderPos, MousePos mousePos);

    public void postRender(WidgetGui gui, RenderSize renderSize, RenderPos renderPos, MousePos mousePos) {
    }

    // GUI EventHandlers

    public void update(WidgetGui gui) {
    }

    public void onGUIClosed(WidgetGui gui) {
    }

    public boolean onMouseClick(MousePos mousePos, RenderPos renderPos, int mouseButton) {
        return false;
    }

    public boolean onMouseClickMove(MousePos mousePos, RenderPos renderPos, int mouseButton) {
        return false;
    }

    public boolean onMouseReleased(MousePos mousePos, RenderPos renderPos) {
        return false;
    }

    public boolean onMouseDWheel(MousePos mousePos, RenderPos renderPos, int wheel) {
        return false;
    }

    public boolean onKeyTyped(char typedChar, int keyCode) {
        return false;
    }

    // Custom GUIEvent Handlers

    public boolean onGuiEvent(GuiEvent event) {
        return false;
    }

    // Tooltips

    @Deprecated
    public List<String> getHoverTooltips(final MousePos mousePos) {
        return Collections.emptyList();
    }

    public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
        return getHoverTooltips(mousePos);
    }

    // Utils

    public boolean isMouseOver(int startX, int startY, int mouseX, int mouseY) {
        if (isInvisible()) {
            return false;
        }

        int endX = startX + getWidth();
        int endY = startY + getHeight();
        return mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= endY;
    }

    public boolean isMouseOver(final MousePos mousePos) {
        return isMouseOver(mousePos.mouseX(), mousePos.mouseY());
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return isMouseOver(0, 0, mouseX, mouseY);
    }

    // Width Height

    public int getWidth() {
        return width;
    }

    public DynamicWidget setWidth(final int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public DynamicWidget setHeight(final int height) {
        this.height = height;
        return this;
    }

    public DynamicWidget setWidthHeight(final int width, final int height) {
        return setWidth(width).setHeight(height);
    }

    // Absolute position

    public int getAbsX() {
        return absX;
    }

    public DynamicWidget setAbsX(final int absX) {
        this.useAbsPos = true;
        this.absX = absX;
        return this;
    }

    public int getAbsY() {
        return absY;
    }

    public DynamicWidget setAbsY(final int absY) {
        this.useAbsPos = true;
        this.absY = absY;
        return this;
    }

    public DynamicWidget setAbsXY(final int absX, final int absY) {
        this.useAbsPos = true;
        return setAbsX(absX).setAbsY(absY);
    }

    public DynamicWidget setUseAbsPos(final boolean useAbsPos) {
        this.useAbsPos = useAbsPos;
        return this;
    }

    /**
     * Absolute coordinates cause the component to always be rendered at one coordinate
     * (but the exact position on the screen still depends on the component container).
     * Widgets rendered using absolute coordinates ignore the margin attribute.
     */
    public boolean isUseAbsPos() {
        return useAbsPos;
    }

    // Margin

    public int getMarginLeft() {
        return marginLeft;
    }

    public DynamicWidget setMarginLeft(final int marginLeft) {
        this.marginLeft = marginLeft;
        return this;
    }

    public int getMarginRight() {
        return marginRight;
    }

    public DynamicWidget setMarginRight(final int marginRight) {
        this.marginRight = marginRight;
        return this;
    }

    public int getMarginUp() {
        return marginUp;
    }

    public DynamicWidget setMarginUp(final int marginUp) {
        this.marginUp = marginUp;
        return this;
    }

    public int getMarginDown() {
        return marginDown;
    }

    public DynamicWidget setMarginDown(final int marginDown) {
        this.marginDown = marginDown;
        return this;
    }

    public DynamicWidget setMarginVertical(final int marginVertical) {
        this.marginUp = marginVertical;
        this.marginDown = marginVertical;
        return this;
    }

    public DynamicWidget setMarginHorizontal(final int marginHorizontal) {
        this.marginLeft = marginHorizontal;
        this.marginRight = marginHorizontal;
        return this;
    }

    public DynamicWidget setMargin(final int margin) {
        this.marginLeft = margin;
        this.marginRight = margin;
        this.marginUp = margin;
        this.marginDown = margin;
        return this;
    }

    public DynamicWidget setMargin(final int marginLeft, final int marginRight, final int marginUp, final int marginDown) {
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginUp = marginUp;
        this.marginDown = marginDown;
        return this;
    }

    // Enabled / Visible

    public boolean isVisible() {
        return visible;
    }

    public DynamicWidget setVisible(final boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isInvisible() {
        return !visible;
    }

    public DynamicWidget setInvisible(final boolean invisible) {
        this.visible = !invisible;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public DynamicWidget setEnabled(final boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isDisabled() {
        return !enabled;
    }

    public DynamicWidget setDisabled(final boolean disabled) {
        this.enabled = !disabled;
        return this;
    }
}
