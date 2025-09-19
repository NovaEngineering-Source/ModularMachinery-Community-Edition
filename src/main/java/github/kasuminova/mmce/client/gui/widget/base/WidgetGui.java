package github.kasuminova.mmce.client.gui.widget.base;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

/**
 * 一个轻量化的组件库核心类，用来存储 GUI 的属性数据，被所有 {@link DynamicWidget} 使用。
 * <br/>
 * <br/>
 * A lightweight component library core class, used to store GUI properties and used by all {@link DynamicWidget}.
 */
public class WidgetGui {
    private final GuiScreen gui;
    private       int       xSize;
    private       int       ySize;
    private       int       guiLeft;
    private       int       guiTop;

    public WidgetGui(final GuiScreen gui, final int xSize, final int ySize, final int guiLeft, final int guiTop) {
        this.gui = gui;
        this.xSize = xSize;
        this.ySize = ySize;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
    }

    public static WidgetGui of(final GuiContainer container) {
        return new WidgetGui(container, container.getXSize(), container.getYSize(), container.getGuiLeft(), container.getGuiTop());
    }

    public static WidgetGui of(final GuiScreen guiScreen, final int xSize, final int ySize, final int guiLeft, final int guiTop) {
        return new WidgetGui(guiScreen, xSize, ySize, guiLeft, guiTop);
    }

    public GuiScreen getGui() {
        return gui;
    }

    public int getWidth() {
        return gui.width;
    }

    public int getHeight() {
        return gui.height;
    }

    public int getXSize() {
        return xSize;
    }

    public WidgetGui setXSize(final int xSize) {
        this.xSize = xSize;
        return this;
    }

    public int getYSize() {
        return ySize;
    }

    public WidgetGui setYSize(final int ySize) {
        this.ySize = ySize;
        return this;
    }

    public int getGuiLeft() {
        return guiLeft;
    }

    public WidgetGui setGuiLeft(final int guiLeft) {
        this.guiLeft = guiLeft;
        return this;
    }

    public int getGuiTop() {
        return guiTop;
    }

    public WidgetGui setGuiTop(final int guiTop) {
        this.guiTop = guiTop;
        return this;
    }
}
