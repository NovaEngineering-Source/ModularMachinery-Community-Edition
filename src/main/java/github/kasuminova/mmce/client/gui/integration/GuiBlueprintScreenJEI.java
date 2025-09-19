package github.kasuminova.mmce.client.gui.integration;

import github.kasuminova.mmce.client.gui.GuiScreenDynamic;
import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.widget.base.WidgetController;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.client.Minecraft;

import java.io.IOException;

/**
 * Rendering special content in a highly encapsulated JEI is horrible.
 */
public class GuiBlueprintScreenJEI extends GuiScreenDynamic {

    @Override
    public void initGui() {
    }

    /**
     * Prevent pushing Forge events, we don't need these extra events,
     * it's just a virtual GUI container.
     */
    @Override
    public void setWorldAndResolution(final Minecraft mc, final int width, final int height) {
        this.mc = mc;
        this.itemRender = mc.getRenderItem();
        this.fontRenderer = mc.fontRenderer;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        widgetController.render(new MousePos(mouseX, mouseY), false);
        widgetController.postRender(new MousePos(mouseX, mouseY), false);
        renderHoveredToolTip(mouseX, mouseY, true);
    }

    @Override
    public void handleMouseInput() {
        try {
            super.handleMouseInput();
        } catch (IOException e) {
            ModularMachinery.log.error(e);
        }
    }

    public void setGuiLeft(final int guiLeft) {
        this.guiLeft = guiLeft;
    }

    public void setGuiTop(final int guiTop) {
        this.guiTop = guiTop;
    }

    public WidgetController getWidgetController() {
        return widgetController;
    }

    public void setWidgetController(final WidgetController widgetController) {
        this.widgetController = widgetController;
    }
}
