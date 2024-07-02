package github.kasuminova.mmce.client.gui.widget.base;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;

import java.util.Collections;
import java.util.List;

/**
 * 一个轻量化的组件库核心类，扩展这个类来渲染你想要的内容。
 * <br/>
 * 内置基础的大小属性，以及 4 个边界属性，可见性控制和可用性控制。
 * <br/>
 * <br/>
 * A lightweight component library core class, extending this class to render the content you want.
 * <br/>
 * Built-in basic size attributes, and 4 boundary attributes, visibility control and availability control.
 */
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

    /**
     * 加载组件，通常情况下一般只会在构造函数内构建一些内容，但是某些情况需要在组件完成组装时再进行加载时此方法会非常有用。
     * <br/>
     * Build widgets, usually only construct some contents in the constructor method,
     * but there are some situations where you need to load them after the component is assembled, and this method will be very useful.
     *
     * @param gui 组件 GUI
     *            <br/>
     *            Widget gui.
     */
    public void initWidget(WidgetGui gui) {
    }

    // Widget Render

    /**
     * 渲染组件的第一个阶段，可以在此阶段完成一些更新操作，但是最好不要在渲染期间修改组件的属性。
     * <br/>
     * The first phase of rendering, you can complete some update operations here, but it is better not to modify the attributes of the component during rendering.
     *
     * @param gui        组件 GUI
     *                   <br/>
     *                   Widget gui
     * @param renderSize 组件的实际渲染大小
     *                   <br/>
     *                   Actual rendering size
     * @param renderPos  组件的真正渲染位置
     *                   <br/>
     *                   Real rendering position
     * @param mousePos   当前鼠标位置
     *                   <br/>
     *                   Current mouse position
     */
    public void preRender(WidgetGui gui, RenderSize renderSize, RenderPos renderPos, MousePos mousePos) {
    }

    /**
     * 渲染组件的第二个阶段，主要内容都在此处渲染。
     * <br/>
     * The second phase of rendering, the main content is rendered here.
     *
     * @param gui        组件 GUI
     *                   <br/>
     *                   Widget gui
     * @param renderSize 组件的实际渲染大小
     *                   <br/>
     *                   Actual rendering size
     * @param renderPos  组件的真正渲染位置
     *                   <br/>
     *                   Real rendering position
     * @param mousePos   当前鼠标位置
     *                   <br/>
     *                   Current mouse position
     */
    public abstract void render(WidgetGui gui, RenderSize renderSize, RenderPos renderPos, MousePos mousePos);

    /**
     * 渲染组件的最终阶段，通常情况下一般用于渲染覆盖内容。
     * <br/>
     * The final phase of rendering, usually used to render overlay content.
     *
     * @param gui        组件 GUI
     *                   <br/>
     *                   Widget gui
     * @param renderSize 组件的实际渲染大小
     *                   <br/>
     *                   Actual rendering size
     * @param renderPos  组件的真正渲染位置
     *                   <br/>
     *                   Real rendering position
     * @param mousePos   当前鼠标位置
     *                   <br/>
     *                   Current mouse position
     */
    public void postRender(WidgetGui gui, RenderSize renderSize, RenderPos renderPos, MousePos mousePos) {
    }

    // GUI EventHandlers

    /**
     * GUI 更新时触发，每 Tick 都会触发一次（1s = 20ticks）。
     * <br/>
     * Triggered when the GUI is updated every tick (1s = 20ticks).
     *
     * @param gui 组件 GUI
     *            <br/>
     *            Widget gui.
     */
    public void update(WidgetGui gui) {
    }

    /**
     * GUI 关闭时触发。
     * <br/>
     * Triggered when the GUI is closed.
     *
     * @param gui 组件 GUI
     *            <br/>
     *            Widget gui.
     */
    public void onGUIClosed(WidgetGui gui) {
    }

    /**
     * 鼠标点击时触发，只有鼠标是在组件范围内才会触发。
     * <br/>
     * Triggered when the mouse is clicked, only when the mouse is within the scope of the component.
     *
     * @param mousePos    鼠标位置
     *                    <br/>
     *                    Mouse position
     * @param renderPos   渲染位置
     *                    <br/>
     *                    Render position
     * @param mouseButton 按下的鼠标按钮
     *                    <br/>
     *                    Mouse button pressed
     * @return 返回 {@code true} 时将会取消继续传递事件。
     * <br/>
     * Return {@code true} to cancel the event.
     */
    public boolean onMouseClick(MousePos mousePos, RenderPos renderPos, int mouseButton) {
        return false;
    }

    /**
     * 鼠标按下时触发，不判定鼠标是否在组件范围内，会在 {@link #onMouseClick(MousePos, RenderPos, int)} 之前调用。
     * <br/>
     * Triggered on mouse clicked, doesn't determine if the mouse is within the scope of the component,
     * and will be called before {@link #onMouseClick(MousePos, RenderPos, int)}.
     *
     * @param mousePos    鼠标位置
     *                    <br/>
     *                    Mouse position
     * @param renderPos   渲染位置
     *                    <br/>
     *                    Render position
     * @param mouseButton 按下的鼠标按钮
     *                    <br/>
     *                    Mouse button pressed
     */
    public void onMouseClickGlobal(MousePos mousePos, RenderPos renderPos, int mouseButton) {
    }

    /**
     * 鼠标按下并拖动的时候触发，不判定鼠标是否在组件范围内，会在 {@link #onMouseClick(MousePos, RenderPos, int)} 之后，@{@link #onMouseReleased(MousePos, RenderPos)} 之前调用。
     * <br/>
     * Triggered when the mouse is clicked and dragged, doesn't determine if the mouse is within the scope of the component,
     * and will be called after {@link #onMouseClick(MousePos, RenderPos, int)} and before {@link #onMouseReleased(MousePos, RenderPos)}.
     *
     * @param mousePos    鼠标位置
     *                    <br/>
     *                    Mouse position
     * @param renderPos   渲染位置
     *                    <br/>
     *                    Render position
     * @param mouseButton 按下的鼠标按钮
     *                    <br/>
     *                    Mouse button pressed
     * @return 返回 {@code true} 时将会取消继续传递事件。
     * <br/>
     * Return {@code true} to cancel the event.
     */
    public boolean onMouseClickMove(MousePos mousePos, RenderPos renderPos, int mouseButton) {
        return false;
    }

    /**
     * 鼠标释放时触发，不判定鼠标是否在组件范围内，会在 {@link #onMouseClick(MousePos, RenderPos, int)} 之后调用。
     * <br/>
     * Triggered when the mouse is released, doesn't determine if the mouse is within the scope of the component,
     * will be called after {@link #onMouseClick(MousePos, RenderPos, int)}.
     *
     * @param mousePos  鼠标位置
     *                  <br/>
     *                  Mouse position
     * @param renderPos 渲染位置
     *                  <br/>
     *                  Render position
     * @return 返回 {@code true} 时将会取消继续传递事件。
     * <br/>
     * Return {@code true} to cancel the event.
     */
    public boolean onMouseReleased(MousePos mousePos, RenderPos renderPos) {
        return false;
    }

    /**
     * 鼠标滚动时触发，不判定鼠标是否在组件范围内，会在 {@link #onMouseClick(MousePos, RenderPos, int)} 之后调用。
     * <br/>
     * Triggered when the mouse is scrolled, doesn't determine if the mouse is within the scope of the component,
     * will be called after {@link #onMouseClick(MousePos, RenderPos, int)}.
     *
     * @param mousePos  鼠标位置
     *                  <br/>
     *                  Mouse position
     * @param renderPos 渲染位置
     *                  <br/>
     *                  Render position
     * @param wheel     滚动值
     *                  <br/>
     *                  Scroll value
     * @return 返回 {@code true} 时将会取消继续传递事件。
     * <br/>
     * Return {@code true} to cancel the event.
     */
    public boolean onMouseDWheel(MousePos mousePos, RenderPos renderPos, int wheel) {
        return false;
    }

    /**
     * 键盘按键按下时触发，不判定鼠标是否在组件范围内，会在 {@link #onMouseClick(MousePos, RenderPos, int)} 之后调用。
     * <br/>
     * Triggered when the keyboard key is pressed, doesn't determine if the mouse is within the scope of the component,
     * will be called after {@link #onMouseClick(MousePos, RenderPos, int)}.
     *
     * @param typedChar 按下的按键字符
     *                  <br/>
     *                  Typed character
     * @param keyCode   按下的按键码
     *                  <br/>
     *                  Key code
     */
    public boolean onKeyTyped(char typedChar, int keyCode) {
        return false;
    }

    // Custom GUIEvent Handlers

    /**
     * 传递 GuiEvent 事件。
     * <br/>
     * Post GuiEvent event.
     *
     * @param event 事件
     *              <br/>
     *              The event
     * @return 返回 {@code true} 时将会取消继续传递事件。
     * <br/>
     * Return {@code true} to cancel the event.
     */
    public boolean onGuiEvent(GuiEvent event) {
        return false;
    }

    // Tooltips

    /**
     * Use {@link #getHoverTooltips(WidgetGui, MousePos)}
     */
    @Deprecated
    public List<String> getHoverTooltips(final MousePos mousePos) {
        return Collections.emptyList();
    }

    /**
     * 获取鼠标悬浮时的提示信息，只有鼠标在组件范围内时才会触发。
     * <br/>
     * Get the tooltips when the mouse is hovered, will be called only when the mouse is within the scope of the component.
     *
     * @param widgetGui 组件 GUI
     *                  <br/>
     *                  Widget GUI
     * @param mousePos  鼠标位置
     *                  <br/>
     *                  Mouse position
     */
    public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
        return getHoverTooltips(mousePos);
    }

    // Utils

    public boolean isMouseOver(final MousePos mousePos) {
        return isMouseOver(mousePos.mouseX(), mousePos.mouseY());
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return isMouseOver(0, 0, mouseX, mouseY);
    }

    public boolean isMouseOver(int startX, int startY, int mouseX, int mouseY) {
        if (isInvisible()) {
            return false;
        }

        int endX = startX + getWidth();
        int endY = startY + getHeight();
        return mouseX >= startX && mouseX < endX && mouseY >= startY && mouseY < endY;
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
     * 绝对坐标会使组件始终在一个坐标上渲染（但在屏幕上的确切位置仍取决于组件容器），使用绝对坐标渲染的组件会忽略 margin 属性。
     * <br/>
     * Absolute coordinates cause the component to always be rendered at one coordinate (but the exact position
     * on the screen still depends on the component container). Widgets rendered using absolute coordinates 
     * ignore the margin attribute.
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

    /**
     * 获取当前组件是否可见，不会影响事件的推送。
     * <br/>
     * Get whether the component is visible, will not affect the event push.
     *
     * @return 组件是否可见
     * <br/>
     * Whether the component is visible.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * 设置当前组件是否可见。
     * <br/>
     * Set whether the component is visible, will not affect the event push.
     *
     * @param visible 组件是否可见
     *                <br/>
     *                Whether the component is visible.
     * @return 当前组件
     * <br/>
     * Current component.
     */
    public DynamicWidget setVisible(final boolean visible) {
        this.visible = visible;
        return this;
    }

    /**
     * {@link #isVisible()} 的反向方法调用。
     * <br/>
     * {@link #isVisible()} reverse method call.
     */
    public boolean isInvisible() {
        return !visible;
    }

    /**
     * {@link #setVisible(boolean)} 的反向方法调用。
     * <br/>
     * {@link #setVisible(boolean)} reverse method call.
     */
    public DynamicWidget setInvisible(final boolean invisible) {
        this.visible = !invisible;
        return this;
    }

    /**
     * 当前组件是否可用，会影响布局的判定以及渲染和事件推送。
     * <br/>
     * Returns the component is enabled, will affect the layout and event push.
     *
     * @return Returns whether the component is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置当前组件是否可用。
     * <br/>
     * Set whether the component is enabled, will affect the layout and event push.
     *
     * @param enabled 组件是否可用
     *                <br/>
     *                Whether the component is enabled.
     * @return 当前组件
     * <br/>
     * Current component.
     */
    public DynamicWidget setEnabled(final boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * {@link #isEnabled()} 的反向方法调用。
     * <br/>
     * {@link #isEnabled()} reverse method call.
     */
    public boolean isDisabled() {
        return !enabled;
    }

    /**
     * {@link #setEnabled(boolean)} 的反向方法调用。
     * <br/>
     * {@link #setEnabled(boolean)} reverse method call.
     */
    public DynamicWidget setDisabled(final boolean disabled) {
        this.enabled = !disabled;
        return this;
    }

}
