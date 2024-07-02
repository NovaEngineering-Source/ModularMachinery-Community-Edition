package github.kasuminova.mmce.client.gui.widget.base;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.container.WidgetContainer;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 一个轻量化的组件库核心类，用来控制所有的组件，可以被附加到任意 GUI 上（只要你能调用它的所有方法）。
 * <br/>
 * <br/>
 * A lightweight component library core class, used to control all the components,
 * can be attached to any GUI (as long as you can call it's all methods).
 */
public class WidgetController {

    public static final ThreadLocal<RenderPos> TRANSLATE_STATE = ThreadLocal.withInitial(() -> new RenderPos(0, 0));

    protected final WidgetGui gui;
    protected final List<DynamicWidget> widgets = new ArrayList<>();

    protected List<String> tooltipCache = new ArrayList<>();

    private boolean initialized = false;

    public WidgetController(final WidgetGui gui) {
        this.gui = gui;
    }

    @Deprecated
    public void addWidgetContainer(final WidgetContainer widgetContainer) {
        widgets.add(widgetContainer);
    }

    public void addWidget(final DynamicWidget widget) {
        widgets.add(widget);
    }

    public void render(final MousePos mousePos, final boolean translatePos) {
        tooltipCache = getHoverTooltipsInternal(mousePos);

        WidgetGui gui = this.gui;

        final int guiLeft = gui.getGuiLeft();
        final int guiTop = gui.getGuiTop();
        RenderPos offset = new RenderPos(guiLeft, guiTop);

        GlStateManager.pushMatrix();
        if (translatePos) {
            GlStateManager.translate(guiLeft, guiTop, 0F);
            TRANSLATE_STATE.set(TRANSLATE_STATE.get().add(offset));
        }

        for (final DynamicWidget container : widgets) {
            RenderPos renderPos = new RenderPos(guiLeft + container.getAbsX(), guiTop + container.getAbsY());
            RenderPos relativeRenderPos = renderPos.subtract(offset);
            MousePos relativeMousePos = mousePos.relativeTo(renderPos);
            RenderSize renderSize = new RenderSize(container.getWidth(), container.getHeight());
            container.preRender(gui, renderSize, relativeRenderPos, relativeMousePos);
        }
        for (final DynamicWidget container : widgets) {
            RenderPos renderPos = new RenderPos(guiLeft + container.getAbsX(), guiTop + container.getAbsY());
            RenderPos relativeRenderPos = renderPos.subtract(offset);
            MousePos relativeMousePos = mousePos.relativeTo(renderPos);
            RenderSize renderSize = new RenderSize(container.getWidth(), container.getHeight());
            container.render(gui, renderSize, relativeRenderPos, relativeMousePos);
        }

        if (translatePos) {
            TRANSLATE_STATE.set(TRANSLATE_STATE.get().subtract(offset));
        }
        GlStateManager.popMatrix();
    }

    public void postRender(final MousePos mousePos, final boolean translatePos) {
        WidgetGui gui = this.gui;

        final int guiLeft = gui.getGuiLeft();
        final int guiTop = gui.getGuiTop();
        RenderPos offset = new RenderPos(guiLeft, guiTop);

        GlStateManager.pushMatrix();
        if (translatePos) {
            GlStateManager.translate(guiLeft, guiTop, 0F);
            TRANSLATE_STATE.set(TRANSLATE_STATE.get().add(offset));
        }

        for (final DynamicWidget container : widgets) {
            RenderPos renderPos = new RenderPos(guiLeft + container.getAbsX(), guiTop + container.getAbsY());
            RenderPos relativeRenderPos = renderPos.subtract(new RenderPos(guiLeft, guiTop));
            MousePos relativeMousePos = mousePos.relativeTo(renderPos);
            RenderSize renderSize = new RenderSize(container.getWidth(), container.getHeight());
            container.postRender(gui, renderSize, relativeRenderPos, relativeMousePos);
        }

        if (translatePos) {
            TRANSLATE_STATE.set(TRANSLATE_STATE.get().subtract(offset));
        }
        GlStateManager.popMatrix();
    }

    public void renderTooltip(final MousePos mousePos) {
        final int guiLeft = gui.getGuiLeft();
        final int guiTop = gui.getGuiTop();

        List<String> hoverTooltips = getHoverTooltips(mousePos);
        if (!hoverTooltips.isEmpty()) {
            MousePos relativeMousePos = mousePos.relativeTo(new RenderPos(guiLeft, guiTop));
            gui.getGui().drawHoveringText(hoverTooltips, relativeMousePos.mouseX(), relativeMousePos.mouseY());
        }
    }

    public void init() {
        if (!initialized) {
            WidgetGui gui = this.gui;
            widgets.forEach(container -> container.initWidget(gui));
        }
        this.initialized = true;
    }

    public void update() {
        WidgetGui gui = this.gui;
        widgets.forEach(container -> container.update(gui));
    }

    public void onGUIClosed() {
        WidgetGui gui = this.gui;
        widgets.forEach(container -> container.onGUIClosed(gui));
    }

    public void postGuiEvent(GuiEvent event) {
        for (final DynamicWidget container : widgets) {
            if (container.onGuiEvent(event)) {
                break;
            }
        }
    }

    public boolean onMouseClicked(final MousePos mousePos, final int mouseButton) {
        WidgetGui gui = this.gui;

        final int x = gui.getGuiLeft();
        final int y = gui.getGuiTop();

        boolean mouseClickEventCancelled = false;

        for (final DynamicWidget container : widgets) {
            RenderPos renderPos = new RenderPos(x + container.getAbsX(), y + container.getAbsY());
            RenderPos relativeRenderPos = renderPos.subtract(new RenderPos(x, y));
            MousePos relativeMousePos = mousePos.relativeTo(renderPos);

            container.onMouseClickGlobal(relativeMousePos, relativeRenderPos, mouseButton);
            if (!mouseClickEventCancelled && container.isMouseOver(relativeMousePos)) {
                if (container.onMouseClick(relativeMousePos, relativeRenderPos, mouseButton)) {
                    mouseClickEventCancelled = true;
                }
            }
        }

        return mouseClickEventCancelled;
    }

    public boolean onMouseClickMove(final MousePos mousePos, final int mouseButton) {
        WidgetGui gui = this.gui;

        final int x = gui.getGuiLeft();
        final int y = gui.getGuiTop();

        for (final DynamicWidget container : widgets) {
            RenderPos renderPos = new RenderPos(x + container.getAbsX(), y + container.getAbsY());
            RenderPos relativeRenderPos = renderPos.subtract(new RenderPos(x, y));
            MousePos relativeMousePos = mousePos.relativeTo(renderPos);

            if (container.onMouseClickMove(relativeMousePos, relativeRenderPos, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    public boolean onMouseReleased(final MousePos mousePos) {
        WidgetGui gui = this.gui;

        final int x = gui.getGuiLeft();
        final int y = gui.getGuiTop();

        for (final DynamicWidget container : widgets) {
            RenderPos renderPos = new RenderPos(x + container.getAbsX(), y + container.getAbsY());
            RenderPos relativeRenderPos = renderPos.subtract(new RenderPos(x, y));
            MousePos relativeMousePos = mousePos.relativeTo(renderPos);

            if (container.onMouseReleased(relativeMousePos, relativeRenderPos)) {
                return true;
            }
        }
        return false;
    }

    public boolean onMouseInput(final MousePos mousePos) {
        final int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return false;
        }
        WidgetGui gui = this.gui;

        final int x = gui.getGuiLeft();
        final int y = gui.getGuiTop();

        for (final DynamicWidget container : widgets) {
            RenderPos renderPos = new RenderPos(x + container.getAbsX(), y + container.getAbsY());
            RenderPos relativeRenderPos = renderPos.subtract(new RenderPos(x, y));
            MousePos relativeMousePos = mousePos.relativeTo(renderPos);

            if (container.onMouseDWheel(relativeMousePos, relativeRenderPos, wheel)) {
                return true;
            }
        }
        return false;
    }

    public boolean onKeyTyped(final char typedChar, final int keyCode) {
        for (final DynamicWidget container : widgets) {
            if (container.onKeyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getHoverTooltips(final MousePos mousePos) {
        return tooltipCache;
    }

    @Nonnull
    private List<String> getHoverTooltipsInternal(final MousePos mousePos) {
        WidgetGui gui = this.gui;

        final int x = gui.getGuiLeft();
        final int y = gui.getGuiTop();

        List<String> tooltips = null;
        for (final DynamicWidget container : widgets) {
            RenderPos renderPos = new RenderPos(x + container.getAbsX(), y + container.getAbsY());
            MousePos relativeMousePos = mousePos.relativeTo(renderPos);

            if (container.isMouseOver(relativeMousePos)) {
                List<String> hoverTooltips = container.getHoverTooltips(this.gui, relativeMousePos);
                if (!hoverTooltips.isEmpty()) {
                    tooltips = hoverTooltips;
                    break;
                }
            }
        }

        return tooltips != null ? tooltips : Collections.emptyList();
    }

    public WidgetGui getGui() {
        return gui;
    }

    public List<DynamicWidget> getWidgets() {
        return widgets;
    }

    @Deprecated
    public List<WidgetContainer> getContainers() {
        return widgets.stream()
                .filter(WidgetContainer.class::isInstance)
                .map(WidgetContainer.class::cast)
                .collect(Collectors.toList());
    }

}
