/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.gui;

import github.kasuminova.mmce.client.gui.GuiScreenDynamic;
import github.kasuminova.mmce.client.gui.widget.base.WidgetController;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.preivew.PreviewPanels;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: GuiScreenBlueprint
 * Created by HellFirePvP
 * Date: 09.07.2017 / 21:08
 */
public class GuiScreenBlueprint extends GuiScreenDynamic {
    public static final ResourceLocation GUI_TEXTURE =
        new ResourceLocation(ModularMachinery.MODID, "textures/gui/guiblueprint_new.png");

    public static final int X_SIZE = 184;
    public static final int Y_SIZE = 220;

    private final DynamicMachine machine;

    public GuiScreenBlueprint(DynamicMachine machine) {
        this.machine = machine;
    }

    @Override
    public void initGui() {
        this.guiLeft = (this.width - X_SIZE) / 2;
        this.guiTop = (this.height - Y_SIZE) / 2;

        this.widgetController = new WidgetController(WidgetGui.of(this, X_SIZE, Y_SIZE, guiLeft, guiTop));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int x = (this.width - X_SIZE) / 2;
        int z = (this.height - Y_SIZE) / 2;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        this.drawTexturedModalRect(x, z, 0, 0, X_SIZE, Y_SIZE);

        this.widgetController.getWidgets().clear();
        this.widgetController.addWidget(PreviewPanels.getPanel(machine, widgetController.getGui()));
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
