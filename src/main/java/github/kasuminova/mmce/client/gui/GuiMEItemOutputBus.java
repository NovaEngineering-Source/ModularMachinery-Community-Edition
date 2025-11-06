package github.kasuminova.mmce.client.gui;

import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;
import github.kasuminova.mmce.common.container.ContainerMEItemOutputBus;
import github.kasuminova.mmce.common.network.PktSwitchGuiMEOutputBus;
import github.kasuminova.mmce.common.tile.MEItemOutputBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiMEItemOutputBus extends GuiMEItemBus {
    // For convenience, the Sky Chest resource was used :P
    private static final ResourceLocation TEXTURES_OUTPUT_BUS = new ResourceLocation("appliedenergistics2", "textures/guis/skychest.png");

    private GuiTabButton stackSizeBtn;
    private final MEItemOutputBus outputBus;

    public GuiMEItemOutputBus(final MEItemOutputBus te, final EntityPlayer player) {
        super(new ContainerMEItemOutputBus(te, player));
        this.outputBus = te;
        this.ySize = 195;
    }

    @Override
    public void initGui() {
        super.initGui();

        // Add wrench button for stack size configuration
        // Icon ID: 2 + 4 * 16 = 66 (wrench icon in AE2's states.png texture)
        this.buttonList.add(this.stackSizeBtn = new GuiTabButton(
                this.guiLeft + 154,
                this.guiTop,
                2 + 4 * 16,
                "Stack Size",
                this.itemRender
        ));
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        if (btn == this.stackSizeBtn) {
            // Switch to stack size configuration GUI
            ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktSwitchGuiMEOutputBus(this.outputBus.getPos(), 1)
            );
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("gui.meitemoutputbus.title"), 8, 8, 0x404040);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 2, 0x404040);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_OUTPUT_BUS);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }

}