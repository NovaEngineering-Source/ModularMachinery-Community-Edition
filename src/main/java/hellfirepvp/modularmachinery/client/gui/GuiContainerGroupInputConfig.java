package hellfirepvp.modularmachinery.client.gui;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.container.ContainerGroupInputConfig;
import hellfirepvp.modularmachinery.common.network.PktGroupInputConfig;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.animation.Animation;

import java.io.IOException;

public class GuiContainerGroupInputConfig extends GuiContainerBase<ContainerGroupInputConfig> {

    private GuiTextField textField;
    private GuiButton    enableIsolation;
    private GuiButton    closeIsolation;

    public GuiContainerGroupInputConfig(TileEntity owner, EntityPlayer opening) {
        super(new ContainerGroupInputConfig(owner, opening));
    }

    @Override
    protected void setWidthHeight() {

    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int offsetX = 4;
        int offsetY = 4;
        FontRenderer fr = this.fontRenderer;
        fr.drawStringWithShadow(I18n.format("gui.groupinputconfig.title"), offsetX, offsetY, 0xFFFFFF);
        offsetX += 2;
        offsetY += 20;

        fr.drawStringWithShadow(I18n.format("gui.groupinputconfig.tooltip"), offsetX, offsetY, 0xFFFFFF);
        offsetY += 25;

        fr.drawStringWithShadow(I18n.format("gui.groupinputconfig.value"), offsetX, offsetY, 0xFFFFFF);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_EMPTY_GUI);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        textField.drawTextBox();
        enableIsolation.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, Animation.getPartialTickTime());
        enableIsolation.drawButtonForegroundLayer(mouseX, mouseY);
        closeIsolation.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, Animation.getPartialTickTime());
        closeIsolation.drawButtonForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && closeIsolation.visible) {
            boolean clicked = textField.mouseClicked(mouseX, mouseY, mouseButton);
            if (clicked) {
                return;
            } else {
                try {
                    long newId = Long.parseLong(textField.getText());
                    if (newId > Integer.MAX_VALUE) {
                        newId = Integer.MAX_VALUE;
                    }
                    if (newId != this.container.getMachine().getGroupId()) {
                        this.container.getMachine().setGroupId((int) newId);
                        ModularMachinery.NET_CHANNEL.sendToServer(new PktGroupInputConfig((int) newId));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } else if (enableIsolation.visible) {
            if (textField.isFocused()) {
                textField.setFocused(false);
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) {
            return;
        }

        if (enableIsolation.visible && enableIsolation.mousePressed(mc, mouseX, mouseY)) {
            enableIsolation.visible = false;
            closeIsolation.visible = true;
            container.getMachine().setGroupInput(true);
            textField.setText(Integer.toString(container.getMachine().getGroupId()));
            ModularMachinery.NET_CHANNEL.sendToServer(new PktGroupInputConfig(true));
            return;
        }
        if (closeIsolation.visible && closeIsolation.mousePressed(mc, mouseX, mouseY)) {
            enableIsolation.visible = true;
            closeIsolation.visible = false;
            container.getMachine().setGroupInput(false);
            textField.setText(Integer.toString(container.getMachine().getGroupId()));
            ModularMachinery.NET_CHANNEL.sendToServer(new PktGroupInputConfig(false));
        }

    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!textField.isFocused()) {
            super.keyTyped(c, i);
        }

        if (Character.isDigit(c) || MiscUtils.isTextBoxKey(i)) {
            textField.textboxKeyTyped(c, i);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        textField = new GuiTextField(0, fontRenderer, this.width / 2 - 15, this.height / 2 - 35, 95, 10);
        textField.setMaxStringLength(10);

        enableIsolation = new GuiButton(1, this.width / 2 + 31, this.height / 2 - 23, 50, 20,
            I18n.format("gui.groupinputconfig.enable"));
        closeIsolation = new GuiButton(2, this.width / 2 + 31, this.height / 2 - 23, 50, 20,
            I18n.format("gui.groupinputconfig.close"));

        enableIsolation.visible = !container.getMachine().isGroupInput();
        closeIsolation.visible  =  container.getMachine().isGroupInput();
        textField.setText(Integer.toString(container.getMachine().getGroupId()));
    }
}
