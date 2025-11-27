package github.kasuminova.mmce.client.gui;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
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
    private static final ResourceLocation TEXTURES_OUTPUT_BUS = new ResourceLocation("appliedenergistics2", "textures/guis/skychest.png");

    private CustomStackSizeButton stackSizeBtn;
    private final MEItemOutputBus outputBus;

    public GuiMEItemOutputBus(final MEItemOutputBus te, final EntityPlayer player) {
        super(new ContainerMEItemOutputBus(te, player));
        this.outputBus = te;
        this.ySize = 195;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.stackSizeBtn = new CustomStackSizeButton(
                this.guiLeft - 18,
                this.guiTop + 8
        );
        this.buttonList.add(this.stackSizeBtn);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        if (btn == this.stackSizeBtn) {
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

    private static class CustomStackSizeButton extends GuiImgButton {
        public CustomStackSizeButton(int x, int y) {
            super(x, y, Settings.ACTIONS, ActionItems.WRENCH);
        }

        @Override
        public String getMessage() {
            return "Configure Stack Size";
        }
    }
}