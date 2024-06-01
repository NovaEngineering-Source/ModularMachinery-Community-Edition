package github.kasuminova.mmce.client.gui.widget;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.stream.IntStream;

public class ButtonElements<E> extends Button4State {

    private final List<ElementInfo> elements = new ObjectArrayList<>();

    private int currentElementIndex = 0;

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        super.render(gui, renderSize, renderPos, mousePos);
        if (!isVisible() || textureLocation == null) {
            return;
        }

        ElementInfo current = getCurrentElement();
        if (current == null) {
            return;
        }

        gui.getGui().mc.getTextureManager().bindTexture(current.textureLocation());
        gui.getGui().drawTexturedModalRect(renderPos.posX(), renderPos.posY(), current.texX(), current.texY(), width, height);
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

    public ButtonElements<E> addElement(final E element, final int textureX, final int textureY, final ResourceLocation textureLocation) {
        elements.add(new ElementInfo(element, textureX, textureY, textureLocation));
        return this;
    }

    public ButtonElements<E> setCurrentSelection(final E element) {
        currentElementIndex = IntStream.range(0, elements.size())
                .filter(i -> elements.get(i).element() == element)
                .findFirst()
                .orElse(currentElementIndex);
        return this;
    }

    public final class ElementInfo {
        private final E element;
        private final int texX;
        private final int texY;
        private final ResourceLocation textureLocation;

        public ElementInfo(E element, int texX, int texY, ResourceLocation textureLocation) {
            this.element = element;
            this.texX = texX;
            this.texY = texY;
            this.textureLocation = textureLocation;
        }

        public E element() {
            return element;
        }

        public int texX() {
            return texX;
        }

        public int texY() {
            return texY;
        }

        public ResourceLocation textureLocation() {
            return textureLocation;
        }
    }
}
