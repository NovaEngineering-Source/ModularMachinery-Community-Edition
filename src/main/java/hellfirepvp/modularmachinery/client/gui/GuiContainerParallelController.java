package hellfirepvp.modularmachinery.client.gui;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.container.ContainerParallelController;
import hellfirepvp.modularmachinery.common.network.PktParallelControllerUpdate;
import hellfirepvp.modularmachinery.common.tiles.TileParallelController;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.model.animation.Animation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiContainerParallelController extends GuiContainerBase<ContainerParallelController> {
    private final TileParallelController parallelCtrlInterface;
    private final List<GuiButton>        buttons = new ArrayList<>();
    private       GuiButton              increment_1;
    private       GuiButton              increment_10;
    private       GuiButton              increment_100;
    private       GuiButton              decrement_1;
    private       GuiButton              decrement_10;
    private       GuiButton              decrement_100;
    private       GuiTextField           textField;

    public GuiContainerParallelController(TileParallelController te, EntityPlayer opening) {
        super(new ContainerParallelController(te, opening));
        this.parallelCtrlInterface = te;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int offsetX = 4;
        int offsetY = 4;
        FontRenderer fr = this.fontRenderer;
        fr.drawStringWithShadow(I18n.format("gui.parallelcontroller.title"), offsetX, offsetY, 0xFFFFFF);
        offsetX += 2;
        offsetY += 12;

        TileParallelController.ParallelControllerProvider provider = parallelCtrlInterface.provideComponent();

        fr.drawStringWithShadow(I18n.format("gui.parallelcontroller.max_value", provider.getMaxParallelism()), offsetX, offsetY, 0xFFFFFF);
        offsetY += 33;
        fr.drawStringWithShadow(I18n.format("gui.parallelcontroller.current_value", provider.getParallelism()), offsetX, offsetY, 0xFFFFFF);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    @Override
    protected void setWidthHeight() {

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_EMPTY_GUI);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        textField.drawTextBox();
        for (GuiButton button : buttons) {
            button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, Animation.getPartialTickTime());
            button.drawButtonForegroundLayer(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) {
            return;
        }

        boolean clicked = textField.mouseClicked(mouseX, mouseY, mouseButton);
        if (clicked) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        SoundHandler soundHandler = mc.getSoundHandler();

        TileParallelController.ParallelControllerProvider provider = parallelCtrlInterface.provideComponent();
        int parallelism = provider.getParallelism();
        int maxParallelism = provider.getMaxParallelism();
        int maxCanIncrement = maxParallelism - parallelism;

        if (increment_1.mousePressed(mc, mouseX, mouseY)) {
            if (maxCanIncrement >= 1) {
                ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktParallelControllerUpdate(parallelism + 1)
                );
            }
            increment_1.playPressSound(soundHandler);
            return;
        }
        if (increment_10.mousePressed(mc, mouseX, mouseY)) {
            if (maxCanIncrement >= 10) {
                ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktParallelControllerUpdate(parallelism + 10)
                );
            } else {
                ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktParallelControllerUpdate(maxParallelism)
                );
            }
            increment_10.playPressSound(soundHandler);
            return;
        }
        if (increment_100.mousePressed(mc, mouseX, mouseY)) {
            if (maxCanIncrement >= 100) {
                ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktParallelControllerUpdate(parallelism + 100)
                );
            } else {
                ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktParallelControllerUpdate(maxParallelism)
                );
            }
            increment_100.playPressSound(soundHandler);
            return;
        }
        int maxCanDecrement = Math.max(0, parallelism - 1);
        if (decrement_1.mousePressed(mc, mouseX, mouseY)) {
            ModularMachinery.NET_CHANNEL.sendToServer(
                new PktParallelControllerUpdate(Math.max(0, parallelism - 1))
            );
            decrement_1.playPressSound(soundHandler);
            return;
        }
        if (decrement_10.mousePressed(mc, mouseX, mouseY)) {
            if (maxCanDecrement >= 10) {
                ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktParallelControllerUpdate(parallelism - 10)
                );
            } else {
                ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktParallelControllerUpdate(0)
                );
            }
            decrement_10.playPressSound(soundHandler);
            return;
        }
        if (decrement_100.mousePressed(mc, mouseX, mouseY)) {
            if (maxCanDecrement >= 100) {
                ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktParallelControllerUpdate(parallelism - 100)
                );
            } else {
                ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktParallelControllerUpdate(0)
                );
            }
            decrement_100.playPressSound(soundHandler);
        }
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!textField.isFocused()) {
            super.keyTyped(c, i);
        }

        if (i == Keyboard.KEY_RETURN) {
            TileParallelController.ParallelControllerProvider provider = parallelCtrlInterface.provideComponent();

            try {
                int newParallelism = Integer.parseInt(textField.getText());
                if (newParallelism >= 0 && newParallelism <= provider.getMaxParallelism()) {
                    ModularMachinery.NET_CHANNEL.sendToServer(new PktParallelControllerUpdate(newParallelism));
                }
                textField.setText("");
            } catch (NumberFormatException ignored) {
            }
            return;
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

        decrement_1 = new GuiButton(1, this.width / 2 - 81, this.height / 2 - 57, 30, 20,
            "-1");
        buttons.add(decrement_1);
        decrement_10 = new GuiButton(2, this.width / 2 - 16, this.height / 2 - 57, 30, 20,
            "-10");
        buttons.add(decrement_10);
        decrement_100 = new GuiButton(3, this.width / 2 + 51, this.height / 2 - 57, 30, 20,
            "-100");
        buttons.add(decrement_100);

        increment_1 = new GuiButton(4, this.width / 2 - 81, this.height / 2 - 23, 30, 20,
            "+1");
        buttons.add(increment_1);
        increment_10 = new GuiButton(5, this.width / 2 - 16, this.height / 2 - 23, 30, 20,
            "+10");
        buttons.add(increment_10);
        increment_100 = new GuiButton(6, this.width / 2 + 51, this.height / 2 - 23, 30, 20,
            "+100");
        buttons.add(increment_100);
    }
}
