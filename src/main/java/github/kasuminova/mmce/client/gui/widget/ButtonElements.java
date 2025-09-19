package github.kasuminova.mmce.client.gui.widget;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.stream.IntStream;

public class ButtonElements<E> extends Button4State {

    private final List<ElementInfo> elements = new ObjectArrayList<>();

    private int currentElementIndex = 0;

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        super.render(gui, renderSize, renderPos, mousePos);
        if (!isVisible()) {
            return;
        }

        ElementInfo current = getCurrentElement();
        if (current == null) {
            return;
        }

        current.texture().render(textureLocation, renderPos, renderSize, gui);
    }

    @Override
    public boolean onMouseReleased(final MousePos mousePos, final RenderPos renderPos) {
        if (isVisible() && isMouseOver(mousePos) && mouseDown) {
            mouseDown = false;
            findNextElement();
            if (onClickedListener != null) {
                onClickedListener.accept(this);
            }
            return true;
        }
        mouseDown = false;
        return false;
    }

    public void findNextElement() {
        if (currentElementIndex + 1 >= elements.size()) {
            currentElementIndex = 0;
        } else {
            currentElementIndex++;
        }
    }

    public ElementInfo getCurrentElement() {
        if (elements.isEmpty()) {
            return null;
        }
        if (currentElementIndex >= elements.size()) {
            currentElementIndex = 0;
        }
        return elements.get(currentElementIndex);
    }

    public E getCurrentSelection() {
        ElementInfo current = getCurrentElement();
        return current == null ? null : current.element();
    }

    public ButtonElements<E> setCurrentSelection(final E element) {
        currentElementIndex = IntStream.range(0, elements.size())
                                       .filter(i -> elements.get(i).element() == element)
                                       .findFirst()
                                       .orElse(currentElementIndex);
        return this;
    }

    public ButtonElements<E> addElement(final E element, final TextureProperties texture) {
        elements.add(new ElementInfo(element, texture));
        return this;
    }

    public final class ElementInfo {

        private final E                 element;
        private final TextureProperties texture;

        public ElementInfo(E element, final TextureProperties texture) {
            this.element = element;
            this.texture = texture;
        }

        public E element() {
            return element;
        }

        public TextureProperties texture() {
            return texture;
        }

    }

}
