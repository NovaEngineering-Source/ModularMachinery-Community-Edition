package github.kasuminova.mmce.client.gui.widget.container;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderFunction;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class Row extends WidgetContainer {
    protected final List<DynamicWidget> widgets = new ArrayList<>();

    protected boolean upAligned = true;
    protected boolean downAligned = false;

    @Override
    protected void preRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        doRender(gui, renderSize, renderPos, mousePos, DynamicWidget::preRender);
    }

    @Override
    protected void renderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        doRender(gui, renderSize, renderPos, mousePos, DynamicWidget::render);
    }

    @Override
    protected void postRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        doRender(gui, renderSize, renderPos, mousePos, DynamicWidget::postRender);
    }

    protected void doRender(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos,
                            final RenderFunction renderFunction) {
        int x = 0;

        int height = getHeight();

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, height, x);
            if (widgetRenderPos == null) {
                continue;
            }
            if (widget.isVisible()) {
                RenderPos absRenderPos = widgetRenderPos.add(renderPos);
                renderFunction.doRender(widget, gui, new RenderSize(widget.getWidth(), widget.getHeight()).smaller(renderSize), absRenderPos, mousePos.relativeTo(widgetRenderPos));
            }
            x += widget.getMarginLeft() + widget.getWidth() + widget.getMarginRight();
        }
    }

    @Override
    public List<DynamicWidget> getWidgets() {
        return widgets;
    }

    @Override
    public Row addWidget(final DynamicWidget widget) {
        widgets.add(widget);
        return this;
    }

    @Override
    public Row removeWidget(final DynamicWidget widget) {
        widgets.remove(widget);
        return this;
    }

    // GUI EventHandlers

    @Override
    public boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        int x = 0;

        int height = getHeight();

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, height, x);
            if (widgetRenderPos == null) {
                continue;
            }
            int offsetX = widgetRenderPos.posX();
            int offsetY = widgetRenderPos.posY();

            MousePos relativeMousePos = mousePos.relativeTo(widgetRenderPos);
            if (widget.isMouseOver(relativeMousePos)) {
                RenderPos absRenderPos = widgetRenderPos.add(renderPos);
                if (widget.onMouseClick(mousePos.relativeTo(widgetRenderPos), absRenderPos, mouseButton)) {
                    return true;
                }
            }
            x += widget.getMarginLeft() + widget.getWidth() + widget.getMarginRight();
        }

        return false;
    }

    @Override
    public void onMouseClickGlobal(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        int x = 0;

        int height = getHeight();

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, height, x);
            if (widgetRenderPos == null) {
                continue;
            }
            int offsetX = widgetRenderPos.posX();
            int offsetY = widgetRenderPos.posY();

            MousePos relativeMousePos = mousePos.relativeTo(widgetRenderPos);
            RenderPos absRenderPos = widgetRenderPos.add(renderPos);
            widget.onMouseClickGlobal(mousePos.relativeTo(widgetRenderPos), absRenderPos, mouseButton);
            x += widget.getMarginLeft() + widget.getWidth() + widget.getMarginRight();
        }
    }

    @Override
    public boolean onMouseClickMove(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        int x = 0;

        int height = getHeight();

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, height, x);
            if (widgetRenderPos == null) {
                continue;
            }
            RenderPos absRenderPos = widgetRenderPos.add(renderPos);
            if (widget.onMouseClickMove(mousePos.relativeTo(widgetRenderPos), absRenderPos, mouseButton)) {
                return true;
            }
            x += widget.getMarginLeft() + widget.getWidth() + widget.getMarginRight();
        }
        return false;
    }

    @Override
    public boolean onMouseReleased(final MousePos mousePos, final RenderPos renderPos) {
        int x = 0;

        int height = getHeight();

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, height, x);
            if (widgetRenderPos == null) {
                continue;
            }
            RenderPos absRenderPos = widgetRenderPos.add(renderPos);
            if (widget.onMouseReleased(mousePos.relativeTo(widgetRenderPos), absRenderPos)) {
                return true;
            }
            x += widget.getMarginLeft() + widget.getWidth() + widget.getMarginRight();
        }
        return false;
    }

    @Override
    public boolean onMouseDWheel(final MousePos mousePos, final RenderPos renderPos, final int wheel) {
        int x = 0;

        int height = getHeight();

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, height, x);
            if (widgetRenderPos == null) {
                continue;
            }
            RenderPos absRenderPos = widgetRenderPos.add(renderPos);
            if (widget.onMouseDWheel(mousePos.relativeTo(widgetRenderPos), absRenderPos, wheel)) {
                return true;
            }
            x += widget.getMarginLeft() + widget.getWidth() + widget.getMarginRight();
        }
        return false;
    }

    @Override
    public boolean onKeyTyped(final char typedChar, final int keyCode) {
        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            if (widget.onKeyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    // Tooltips

    @Override
    public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
        int x = 0;

        int height = getHeight();

        List<String> tooltips = null;

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, height, x);
            if (widgetRenderPos == null) {
                continue;
            }
            int offsetX = widgetRenderPos.posX();
            int offsetY = widgetRenderPos.posY();

            MousePos relativeMousePos = mousePos.relativeTo(widgetRenderPos);
            if (widget.isMouseOver(relativeMousePos)) {
                List<String> hoverTooltips = widget.getHoverTooltips(widgetGui, relativeMousePos);
                if (!hoverTooltips.isEmpty()) {
                    tooltips = hoverTooltips;
                    break;
                }
            }

            x += widget.getMarginLeft() + widget.getWidth() + widget.getMarginRight();
        }

        return tooltips != null ? tooltips : Collections.emptyList();
    }

    // CustomEventHandlers

    @Override
    public boolean onGuiEvent(final GuiEvent event) {
        for (final DynamicWidget widget : widgets) {
//            if (widget.isDisabled()) {
//                continue;
//            }
            if (widget.onGuiEvent(event)) {
                return true;
            }
        }
        return false;
    }

    // Utils

    public RenderPos getWidgetRenderOffset(DynamicWidget widget, int height, int x) {
        int xOffset;
        int yOffset;

        if (widget.isUseAbsPos()) {
            xOffset = widget.getAbsX();
            yOffset = widget.getAbsY();
        } else if (isCenterAligned()) {
            xOffset = x + widget.getMarginLeft();
            yOffset = (height - (widget.getMarginUp() + widget.getHeight() + widget.getMarginDown())) / 2;
        } else if (upAligned) {
            xOffset = x + widget.getMarginLeft();
            yOffset = widget.getMarginUp();
        } else if (downAligned) {
            xOffset = x + widget.getMarginLeft();
            yOffset = height - (widget.getHeight() + widget.getMarginDown());
        } else {
            // Where does it align?
            return null;
        }

        return new RenderPos(xOffset, yOffset);
    }

    // X/Y Size

    @Override
    public int getWidth() {
        int width = 0;
        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            int widgetWidth = widget.getMarginLeft() + widget.getWidth() + widget.getMarginRight();
            if (widget.isUseAbsPos()) {
                width = Math.max(width, widgetWidth);
                continue;
            }
            width += widgetWidth;
        }
        return width;
    }

    @Override
    public DynamicWidget setWidth(final int width) {
        // It's dynamic, so ignore it.
        return this;
    }

    @Override
    public int getHeight() {
        int maxY = 0;
        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            int height = 0;
            if (widget.isUseAbsPos()) {
                height += widget.getAbsY();
            }
            height += widget.getMarginUp() + widget.getHeight() + widget.getMarginDown();
            if (height > maxY) {
                maxY = height;
            }
        }
        return maxY;
    }

    @Override
    public DynamicWidget setHeight(final int height) {
        // It's dynamic, so ignore it.
        return this;
    }

    @Override
    public DynamicWidget setWidthHeight(final int width, final int height) {
        // It's dynamic, so ignore it.
        return this;
    }

    // Align

    public boolean isUpAligned() {
        return upAligned;
    }

    public Row setUpAligned(final boolean upAligned) {
        this.downAligned = !upAligned;
        this.upAligned = upAligned;
        return this;
    }

    public boolean isDownAligned() {
        return downAligned;
    }

    public Row setDownAligned(final boolean downAligned) {
        this.upAligned = !downAligned;
        this.downAligned = downAligned;
        return this;
    }

    public Row setCenterAligned(final boolean centerAligned) {
        if (centerAligned) {
            this.upAligned = true;
            this.downAligned = true;
            return this;
        }
        // Default setting is up aligned.
        this.upAligned = true;
        this.downAligned = false;
        return this;
    }

    public boolean isCenterAligned() {
        return this.upAligned && this.downAligned;
    }
}
