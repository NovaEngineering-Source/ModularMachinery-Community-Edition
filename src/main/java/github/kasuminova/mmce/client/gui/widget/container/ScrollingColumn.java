package github.kasuminova.mmce.client.gui.widget.container;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderFunction;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.Scrollbar;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class ScrollingColumn extends Column {
    protected final Scrollbar scrollbar = new Scrollbar();

    @Override
    protected void doRender(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos, final RenderFunction renderFunction) {
        int width = renderSize.width() - (scrollbar.getMarginLeft() + scrollbar.getWidth() + scrollbar.getMarginRight());
        int height = renderSize.height();

        int y = getTotalHeight() > height ? -scrollbar.getCurrentScroll() : 0;

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, width, y);
            if (widgetRenderPos == null) {
                continue;
            }
            int offsetY = widgetRenderPos.posY();
            if (offsetY + widget.getHeight() >= 0) {
                RenderPos absRenderPos = widgetRenderPos.add(renderPos);
                renderFunction.doRender(widget, gui, new RenderSize(widget.getWidth(), widget.getHeight()).smaller(renderSize), absRenderPos, mousePos.relativeTo(widgetRenderPos));
            }

            y += widget.getMarginUp() + widget.getHeight() + widget.getMarginDown();
            if (renderSize.isHeightLimited() && y >= renderSize.height()) {
                break;
            }
        }

        if (scrollbar.isDisabled()) {
            return;
        }
        RenderPos scrollbarRenderPos = new RenderPos(
            width + (scrollbar.getMarginLeft()),
            scrollbar.getMarginUp());
        renderFunction.doRender(scrollbar, gui,
            new RenderSize(scrollbar.getWidth(), scrollbar.getHeight()).smaller(renderSize),
            renderPos.add(scrollbarRenderPos),
            mousePos.relativeTo(scrollbarRenderPos)
        );
    }

    @Override
    public void initWidget(final WidgetGui gui) {
        super.initWidget(gui);
        scrollbar.setMargin(0, 1, 1, 1);
        scrollbar.setMouseWheelCheckPos(false);
    }

    @Override
    public ScrollingColumn addWidget(final DynamicWidget widget) {
        super.addWidget(widget);
        return this;
    }

    @Override
    public ScrollingColumn removeWidget(final DynamicWidget widget) {
        super.removeWidget(widget);
        return this;
    }

    // GUI EventHandlers

    @Override
    public void update(final WidgetGui gui) {
        super.update(gui);
        scrollbar.setRange(0, Math.max(getTotalHeight() - height, 0));
        scrollbar.setScrollUnit(scrollbar.getRange() / 10);
        scrollbar.update(gui);
    }

    @Override
    public boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        int width = this.width;
        int height = this.height;

        int y = getTotalHeight() > height ? -scrollbar.getCurrentScroll() : 0;

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, width, y);
            if (widgetRenderPos == null) {
                continue;
            }
            int offsetX = widgetRenderPos.posX();
            int offsetY = widgetRenderPos.posY();

            if (offsetY + widget.getHeight() >= 0) {
                MousePos relativeMousePos = mousePos.relativeTo(widgetRenderPos);
                if (widget.isMouseOver(relativeMousePos)) {
                    RenderPos absRenderPos = widgetRenderPos.add(renderPos);
                    if (widget.onMouseClick(relativeMousePos, absRenderPos, mouseButton)) {
                        return true;
                    }
                }
            }
            y += widget.getMarginUp() + widget.getHeight() + widget.getMarginDown();
        }

        if (scrollbar.isDisabled()) {
            return false;
        }
        RenderPos scrollbarRenderPos = new RenderPos(
            width - (scrollbar.getWidth() + scrollbar.getMarginRight()),
            height - (scrollbar.getHeight() + scrollbar.getMarginDown()));
        MousePos scrollbarRelativeMousePos = mousePos.relativeTo(scrollbarRenderPos);
        if (scrollbar.isMouseOver(scrollbarRelativeMousePos)) {
            return scrollbar.onMouseClick(scrollbarRelativeMousePos, renderPos.add(scrollbarRenderPos), mouseButton);
        }

        return false;
    }

    @Override
    public void onMouseClickGlobal(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        int width = this.width;
        int height = this.height;

        int y = getTotalHeight() > height ? -scrollbar.getCurrentScroll() : 0;

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, width, y);
            if (widgetRenderPos == null) {
                continue;
            }
            int offsetX = widgetRenderPos.posX();
            int offsetY = widgetRenderPos.posY();

            if (offsetY + widget.getHeight() >= 0) {
                MousePos relativeMousePos = mousePos.relativeTo(widgetRenderPos);
                RenderPos absRenderPos = widgetRenderPos.add(renderPos);
                widget.onMouseClickGlobal(relativeMousePos, absRenderPos, mouseButton);
            }
            y += widget.getMarginUp() + widget.getHeight() + widget.getMarginDown();
        }
    }

    @Override
    public boolean onMouseClickMove(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        int width = this.width;
        int height = this.height;

        int y = getTotalHeight() > height ? -scrollbar.getCurrentScroll() : 0;

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, width, y);
            if (widgetRenderPos == null) {
                continue;
            }
            RenderPos absRenderPos = widgetRenderPos.add(renderPos);
            if (widget.onMouseClickMove(mousePos.relativeTo(widgetRenderPos), absRenderPos, mouseButton)) {
                return true;
            }
            y += widget.getMarginUp() + widget.getHeight() + widget.getMarginDown();
        }

        if (scrollbar.isDisabled()) {
            return false;
        }
        RenderPos scrollbarRenderPos = new RenderPos(
            width - (scrollbar.getWidth() + scrollbar.getMarginRight()),
            height - (scrollbar.getHeight() + scrollbar.getMarginDown()));
        return scrollbar.onMouseClickMove(mousePos.relativeTo(scrollbarRenderPos), renderPos.add(scrollbarRenderPos), mouseButton);
    }

    @Override
    public boolean onMouseReleased(final MousePos mousePos, final RenderPos renderPos) {
        int width = this.width;
        int height = this.height;

        int y = getTotalHeight() > height ? -scrollbar.getCurrentScroll() : 0;

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, width, y);
            if (widgetRenderPos == null) {
                continue;
            }
            RenderPos absRenderPos = widgetRenderPos.add(renderPos);
            if (widget.onMouseReleased(mousePos.relativeTo(widgetRenderPos), absRenderPos)) {
                return true;
            }
            y += widget.getMarginUp() + widget.getHeight() + widget.getMarginDown();
        }

        if (scrollbar.isDisabled()) {
            return false;
        }
        RenderPos scrollbarRenderPos = new RenderPos(
            width - (scrollbar.getWidth() + scrollbar.getMarginRight()),
            height - (scrollbar.getHeight() + scrollbar.getMarginDown()));
        return scrollbar.onMouseReleased(mousePos.relativeTo(scrollbarRenderPos), renderPos.add(scrollbarRenderPos));
    }

    @Override
    public boolean onMouseDWheel(final MousePos mousePos, final RenderPos renderPos, final int wheel) {
        int width = this.width;
        int height = this.height;

        int y = getTotalHeight() > height ? -scrollbar.getCurrentScroll() : 0;

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, width, y);
            if (widgetRenderPos == null) {
                continue;
            }
            RenderPos absRenderPos = widgetRenderPos.add(renderPos);
            if (widget.onMouseDWheel(mousePos.relativeTo(widgetRenderPos), absRenderPos, wheel)) {
                return true;
            }
            y += widget.getMarginUp() + widget.getHeight() + widget.getMarginDown();
        }

        if (scrollbar.isDisabled()) {
            return false;
        }
        if (isMouseOver(mousePos)) {
            RenderPos scrollbarRenderPos = new RenderPos(
                width - (scrollbar.getWidth() + scrollbar.getMarginRight()),
                height - (scrollbar.getHeight() + scrollbar.getMarginDown()));
            MousePos scrollbarRelativeMousePos = mousePos.relativeTo(scrollbarRenderPos);
            return scrollbar.onMouseDWheel(scrollbarRelativeMousePos, renderPos.add(scrollbarRenderPos), wheel);
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
        int width = this.width;
        int height = this.height;

        int y = getTotalHeight() > height ? -scrollbar.getCurrentScroll() : 0;

        List<String> tooltips = null;

        for (final DynamicWidget widget : widgets) {
            if (widget.isDisabled()) {
                continue;
            }
            RenderPos widgetRenderPos = getWidgetRenderOffset(widget, width, y);
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

            y += widget.getMarginUp() + widget.getHeight() + widget.getMarginDown();
        }

        if (tooltips != null) {
            return tooltips;
        }

        if (scrollbar.isDisabled()) {
            return Collections.emptyList();
        }

        RenderPos scrollbarRenderPos = new RenderPos(
            width - (scrollbar.getWidth() + scrollbar.getMarginRight()),
            height - (scrollbar.getHeight() + scrollbar.getMarginDown()));
        MousePos scrollbarMousePos = mousePos.relativeTo(scrollbarRenderPos);
        if (scrollbar.isMouseOver(scrollbarMousePos)) {
            List<String> hoverTooltips = scrollbar.getHoverTooltips(widgetGui, scrollbarMousePos);
            if (!hoverTooltips.isEmpty()) {
                tooltips = hoverTooltips;
            }
        }

        return tooltips != null ? tooltips : Collections.emptyList();
    }

    // CustomEventHandlers

    @Override
    public boolean onGuiEvent(final GuiEvent event) {
        if (scrollbar.onGuiEvent(event)) {
            return true;
        }
        return super.onGuiEvent(event);
    }

    // X/Y Size

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public ScrollingColumn setWidth(final int width) {
        this.width = width;
        return this;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public ScrollingColumn setHeight(final int height) {
        this.height = height;
        scrollbar.setHeight(height - 2);
        return this;
    }

    @Override
    public ScrollingColumn setWidthHeight(final int width, final int height) {
        return setWidth(width).setHeight(height);
    }

    public int getTotalHeight() {
        int total = 0;
        for (final DynamicWidget widget : widgets) {
            if (widget.isUseAbsPos()) {
                continue;
            }
            total += widget.getMarginUp() + widget.getHeight() + widget.getMarginDown();
        }
        return total;
    }

}
