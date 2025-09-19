package github.kasuminova.mmce.client.gui.widget.container;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * 一个轻量化的组件库核心类，用于存储一系列 {@link DynamicWidget} 并使用它自己的方式来渲染这些组件。
 * <br/>
 * <br/>
 * A lightweight component library core class, used to store a series of {@link DynamicWidget} and render them using its own way.
 */
public abstract class WidgetContainer extends DynamicWidget {
    protected static final ThreadLocal<LinkedList<Rectangle>> SCISSOR_STACK = ThreadLocal.withInitial(LinkedList::new);

    protected boolean useScissor = true;

    public static void pushScissor(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final int width, final int height) {
        final int guiLeft = gui.getGuiLeft();
        final int guiTop = gui.getGuiTop();

        int offsetX = renderPos.posX();
        int offsetY = renderPos.posY();

        if (renderSize.isLimited()) {
            LinkedList<Rectangle> scissorStack = SCISSOR_STACK.get();

            ScaledResolution res = new ScaledResolution(gui.getGui().mc);
            int scissorWidth = renderSize.isWidthLimited() ? renderSize.width() : width;
            int scissorHeight = renderSize.isHeightLimited() ? renderSize.height() : height;

            Rectangle scissorFrame = new Rectangle(
                (guiLeft + offsetX) * res.getScaleFactor(),
                // y is left bottom...
                gui.getGui().mc.displayHeight - ((guiTop + offsetY + scissorHeight) * res.getScaleFactor()),
                scissorWidth * res.getScaleFactor(),
                scissorHeight * res.getScaleFactor()
            );

            if (scissorStack.peekFirst() == null) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
            GL11.glScissor(scissorFrame.x, scissorFrame.y, scissorFrame.width, scissorFrame.height);

            scissorStack.push(scissorFrame);
        }
    }

    public static void popScissor(final RenderSize renderSize) {
        if (renderSize.isLimited()) {
            LinkedList<Rectangle> scissorStack = SCISSOR_STACK.get();
            if (scissorStack.peekFirst() != null) {
                scissorStack.pop();
            }

            Rectangle prevScissorFrame = scissorStack.peekFirst();
            if (prevScissorFrame == null) {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            } else {
                GL11.glScissor(prevScissorFrame.x, prevScissorFrame.y, prevScissorFrame.width, prevScissorFrame.height);
            }
        }
    }

    public static void enableScissor() {
        LinkedList<Rectangle> scissorStack = SCISSOR_STACK.get();
        Rectangle scissorFrame = scissorStack.peekFirst();
        if (scissorFrame != null) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(scissorFrame.x, scissorFrame.y, scissorFrame.width, scissorFrame.height);
        }
    }

    public static void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public final void preRender(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        boolean useScissor = this.useScissor;
        if (useScissor) {
            pushScissor(gui, renderSize, renderPos, getWidth(), getHeight());
        }
        try {
            preRenderInternal(gui, renderSize, renderPos, mousePos);
        } catch (Exception e) {
            SCISSOR_STACK.get().clear();
            ModularMachinery.log.error("Error when rendering dynamic widgets!", e);
            throw e;
        } finally {
            if (useScissor) {
                popScissor(renderSize);
            }
        }
    }

    @Override
    public final void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        boolean useScissor = this.useScissor;
        if (useScissor) {
            pushScissor(gui, renderSize, renderPos, getWidth(), getHeight());
        }
        try {
            renderInternal(gui, renderSize, renderPos, mousePos);
        } catch (Exception e) {
            SCISSOR_STACK.get().clear();
            ModularMachinery.log.error("Error when rendering dynamic widgets!", e);
            throw e;
        } finally {
            if (useScissor) {
                popScissor(renderSize);
            }
        }
    }

    @Override
    public final void postRender(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        boolean useScissor = this.useScissor;
        if (useScissor) {
            pushScissor(gui, renderSize, renderPos, getWidth(), getHeight());
        }
        try {
            postRenderInternal(gui, renderSize, renderPos, mousePos);
        } catch (Exception e) {
            SCISSOR_STACK.get().clear();
            ModularMachinery.log.error("Error when rendering dynamic widgets!", e);
            throw e;
        } finally {
            if (useScissor) {
                popScissor(renderSize);
            }
        }
    }

    protected abstract void preRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos);

    protected abstract void renderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos);

    protected abstract void postRenderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos);

    public abstract List<DynamicWidget> getWidgets();

    public abstract WidgetContainer addWidget(DynamicWidget widget);

    public WidgetContainer addWidgets(DynamicWidget... widgets) {
        for (final DynamicWidget widget : widgets) {
            addWidget(widget);
        }
        return this;
    }

    public abstract WidgetContainer removeWidget(DynamicWidget widget);

    // GUI EventHandlers

    @Override
    public void update(final WidgetGui gui) {
        for (DynamicWidget widget : getWidgets()) {
            if (widget.isDisabled()) {
                continue;
            }
            widget.update(gui);
        }
    }

    @Override
    public void onGUIClosed(final WidgetGui gui) {
        getWidgets().forEach(widget -> widget.onGUIClosed(gui));
    }

    @Override
    public void initWidget(final WidgetGui gui) {
        getWidgets().forEach(widget -> widget.initWidget(gui));
    }

    @Override
    public abstract boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton);

    @Override
    public abstract void onMouseClickGlobal(final MousePos mousePos, final RenderPos renderPos, final int mouseButton);

    @Override
    public abstract boolean onMouseReleased(final MousePos mousePos, final RenderPos renderPos);

    @Override
    public abstract boolean onMouseDWheel(final MousePos mousePos, final RenderPos renderPos, final int wheel);

    @Override
    public abstract boolean onKeyTyped(final char typedChar, final int keyCode);

    // Tooltips

    @Override
    public abstract List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos);

    // Events

    /**
     * GUI events will only be passed down, not up.
     *
     * @return Returns true to terminate the passing of events to the widgets.
     */
    @Override
    public abstract boolean onGuiEvent(final GuiEvent event);

    // Scissor setting

    public boolean isUseScissor() {
        return useScissor;
    }

    public WidgetContainer setUseScissor(final boolean useScissor) {
        this.useScissor = useScissor;
        return this;
    }
}