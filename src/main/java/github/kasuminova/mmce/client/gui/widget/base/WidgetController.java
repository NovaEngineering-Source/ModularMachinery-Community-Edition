package github.kasuminova.mmce.client.gui.widget.base;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.container.WidgetContainer;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class WidgetController {

    public static final ThreadLocal<RenderPos> TRANSLATE_STATE = ThreadLocal.withInitial(() -> new RenderPos(0, 0));

    protected final WidgetGui gui;
    protected final List<WidgetContainer> containers = new ArrayList<>();

    private boolean initialized = false;

    public WidgetController(final WidgetGui gui) {
        this.gui = gui;
    }

    public void addWidgetContainer(final WidgetContainer widgetContainer) {
        containers.add(widgetContainer);
    }

    public void render(final MousePos mousePos, final boolean translatePos) {
        WidgetGui gui = this.gui;

        final int guiLeft = gui.getGuiLeft();
        final int guiTop = gui.getGuiTop();
        RenderPos offset = new RenderPos(guiLeft, guiTop);

        GlStateManager.pushMatrix();
        if (translatePos) {
            GlStateManager.translate(guiLeft, guiTop, 0F);
            TRANSLATE_STATE.set(TRANSLATE_STATE.get().add(offset));
        }

        for (final WidgetContainer container : containers) {
            RenderPos renderPos = new RenderPos(guiLeft + container.getAbsX(), guiTop + container.getAbsY());
            RenderPos relativeRenderPos = renderPos.subtract(offset);
            MousePos relativeMousePos = mousePos.relativeTo(renderPos);
            RenderSize renderSize = new RenderSize(container.getWidth(), container.getHeight());
            container.preRender(gui, renderSize, relativeRenderPos, relativeMousePos);
        }
        for (final WidgetContainer container : containers) {
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

        for (final WidgetContainer container : containers) {
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
            containers.forEach(container -> container.initWidget(gui));
        }
        this.initialized = true;
    }

    public void update() {
        WidgetGui gui = this.gui;
        containers.forEach(container -> container.update(gui));
    }

    public void onGUIClosed() {
        WidgetGui gui = this.gui;
        containers.forEach(container -> container.onGUIClosed(gui));
    }

    public void postGuiEvent(GuiEvent event) {
        for (final WidgetContainer container : containers) {
            if (container.onGuiEvent(event)) {
                break;
            }
        }
    }

    public boolean onMouseClicked(final MousePos mousePos, final int mouseButton) {
        WidgetGui gui = this.gui;

        final int x = gui.getGuiLeft();
        final int y = gui.getGuiTop();

        for (final WidgetContainer container : containers) {
            RenderPos renderPos = new RenderPos(x + container.getAbsX(), y + container.getAbsY());
            RenderPos relativeRenderPos = renderPos.subtract(new RenderPos(x, y));
            MousePos relativeMousePos = mousePos.relativeTo(renderPos);

            if (container.isMouseOver(relativeMousePos)) {
                if (container.onMouseClick(relativeMousePos, relativeRenderPos, mouseButton)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean onMouseClickMove(final MousePos mousePos, final int mouseButton) {
        WidgetGui gui = this.gui;

        final int x = gui.getGuiLeft();
        final int y = gui.getGuiTop();

        for (final WidgetContainer container : containers) {
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

        for (final WidgetContainer container : containers) {
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

        for (final WidgetContainer container : containers) {
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
        for (final WidgetContainer container : containers) {
            if (container.onKeyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getHoverTooltips(final MousePos mousePos) {
        WidgetGui gui = this.gui;

        final int x = gui.getGuiLeft();
        final int y = gui.getGuiTop();

        List<String> tooltips = null;
        for (final WidgetContainer container : containers) {
            RenderPos renderPos = new RenderPos(x + container.getAbsX(), y + container.getAbsY());
            MousePos relativeMousePos = mousePos.relativeTo(renderPos);

            if (container.isMouseOver(relativeMousePos)) {
                List<String> hoverTooltips = container.getHoverTooltips(relativeMousePos);
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

    public List<WidgetContainer> getContainers() {
        return containers;
    }

}
