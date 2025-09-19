package github.kasuminova.mmce.client.gui;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.widget.base.WidgetController;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public abstract class GuiScreenDynamic extends GuiScreen {

    protected WidgetController widgetController;
    protected int              guiLeft;
    protected int              guiTop;

    @Override
    public void updateScreen() {
        super.updateScreen();
        widgetController.update();
    }

    @Override
    public abstract void initGui();

    @Override
    public void onGuiClosed() {
        widgetController.onGUIClosed();
        super.onGuiClosed();
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        widgetController.render(new MousePos(mouseX, mouseY), true);
        widgetController.postRender(new MousePos(mouseX, mouseY), true);
        renderHoveredToolTip(mouseX, mouseY, false);
    }

    protected void renderHoveredToolTip(final int mouseX, final int mouseY, final boolean translateMousePos) {
        MousePos mousePos = new MousePos(mouseX, mouseY);
        List<String> hoverTooltips = widgetController.getHoverTooltips(mousePos);
        if (hoverTooltips.isEmpty()) {
            return;
        }
        if (translateMousePos) {
            mousePos = mousePos.relativeTo(new RenderPos(guiLeft, guiTop));
        }
        this.drawHoveringText(hoverTooltips, mousePos.mouseX(), mousePos.mouseY(), fontRenderer);
    }

    @Override
    public void handleMouseInput() throws IOException {
        final int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        final int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        if (widgetController.onMouseInput(new MousePos(mouseX, mouseY))) {
            return;
        }
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        if (widgetController.onMouseClicked(new MousePos(mouseX, mouseY), mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(final int mouseX, final int mouseY, final int mouseButton, final long timeSinceLastClick) {
        if (widgetController.onMouseClickMove(new MousePos(mouseX, mouseY), mouseButton)) {
            return;
        }
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        if (widgetController.onMouseReleased(new MousePos(mouseX, mouseY))) {
            return;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
        if (widgetController.onKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawHoveringText(@Nonnull final List<String> textLines, final int x, final int y) {
        super.drawHoveringText(textLines, x, y);
    }
}
