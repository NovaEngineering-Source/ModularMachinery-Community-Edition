package hellfirepvp.modularmachinery.client.gui;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.container.ContainerSmartInterface;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.network.PktSmartInterfaceUpdate;
import hellfirepvp.modularmachinery.common.tiles.TileSmartInterface;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceData;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceType;
import io.netty.util.internal.ThrowableUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.Animation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.IllegalFormatException;

public class GuiContainerSmartInterface extends GuiContainerBase<ContainerSmartInterface> {
    private final TileSmartInterface smartInterface;
    private       GuiTextField       textField;
    private       GuiButton          prev;
    private       GuiButton          next;
    private       int                showing = 0;

    public GuiContainerSmartInterface(TileSmartInterface owner, EntityPlayer opening) {
        super(new ContainerSmartInterface(owner, opening));
        this.smartInterface = owner;
    }

    @Override
    protected void setWidthHeight() {
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        TileSmartInterface.SmartInterfaceProvider component = smartInterface.provideComponent();

        int offsetX = 4;
        int offsetY = 4;
        FontRenderer fr = this.fontRenderer;
        fr.drawStringWithShadow(I18n.format("gui.smartinterface.title", component.getBoundSize(), showing + 1), offsetX, offsetY, 0xFFFFFF);
        offsetX += 3;
        offsetY += 12;

        SmartInterfaceData data = component.getMachineData(showing);
        if (component.getBoundSize() <= 0 || data == null) {
            fr.drawStringWithShadow(I18n.format("gui.smartinterface.notfound"), offsetX, offsetY, 0xFFFFFF);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
            return;
        }
        DynamicMachine machine = MachineRegistry.getRegistry().getMachine(data.getParent());
        if (machine == null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
            return;
        }
        SmartInterfaceType type = machine.getSmartInterfaceType(data.getType());
        if (type == null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
            return;
        }

        BlockPos pos = data.getPos();
        fr.drawStringWithShadow(String.format("%s (%s)", machine.getLocalizedName(), MiscUtils.posToString(pos)),
            offsetX, offsetY, 0xFFFFFF);

        offsetY += 10;
        fr.drawStringWithShadow(I18n.format(type.getHeaderInfo()), offsetX, offsetY, 0xFFFFFF);

        offsetY += 10;
        String valueInfo;
        try {
            valueInfo = type.getValueInfo().isEmpty()
                ? I18n.format("gui.smartinterface.value", data.getValue())
                : String.format(type.getValueInfo(), data.getValue());
        } catch (IllegalFormatException ex) {
            ModularMachinery.log.warn(ThrowableUtil.stackTraceToString(ex));
            valueInfo = I18n.format("gui.smartinterface.value", data.getValue());
        }
        fr.drawStringWithShadow(valueInfo, offsetX, offsetY, 0xFFFFFF);

        offsetY += 10;
        fr.drawStringWithShadow(I18n.format(type.getFooterInfo()), offsetX, offsetY, 0xFFFFFF);

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

        if (smartInterface.provideComponent().getBoundSize() <= 0) {
            return;
        }

        textField.drawTextBox();
        prev.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, Animation.getPartialTickTime());
        prev.drawButtonForegroundLayer(mouseX, mouseY);
        next.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, Animation.getPartialTickTime());
        next.drawButtonForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        textField.updateCursorCounter();
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

        TileSmartInterface.SmartInterfaceProvider component = smartInterface.provideComponent().getContainerProvider();
        Minecraft minecraft = Minecraft.getMinecraft();
        SoundHandler soundHandler = minecraft.getSoundHandler();

        int boundSize = component.getBoundSize();
        if (prev.mousePressed(minecraft, mouseX, mouseY)) {
            if (showing >= 1) {
                showing--;
            }
            if (showing > boundSize) {
                showing = boundSize - 1;
            }
            prev.playPressSound(soundHandler);
            return;
        }
        if (next.mousePressed(minecraft, mouseX, mouseY)) {
            if (boundSize > showing + 1) {
                showing++;
            } else {
                showing = boundSize - 1;
            }
            next.playPressSound(soundHandler);
        }
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!textField.isFocused()) {
            super.keyTyped(c, i);
        }

        if (i == Keyboard.KEY_RETURN) {
            TileSmartInterface.SmartInterfaceProvider provider = smartInterface.provideComponent().getContainerProvider();
            SmartInterfaceData data = provider.getMachineData(showing);
            if (data == null) {
                return;
            }

            try {
                SmartInterfaceData newData = new SmartInterfaceData(data.getPos(), data.getParent(), data.getType(), Float.parseFloat(textField.getText()));
                ModularMachinery.NET_CHANNEL.sendToServer(new PktSmartInterfaceUpdate(newData));
                textField.setText("");
            } catch (NumberFormatException ignored) {
            }
            return;
        }

        if (Character.isDigit(c) || c == '.' || c == 'E' || MiscUtils.isTextBoxKey(i)) {
            textField.textboxKeyTyped(c, i);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        textField = new GuiTextField(0, fontRenderer, this.width / 2 + 10, this.height / 2 - 48, 70, 10);
        textField.setMaxStringLength(16);
        prev = new GuiButton(1, this.width / 2 - 81, this.height / 2 - 25, 40, 20,
            I18n.format("gui.smartinterface.prev"));
        next = new GuiButton(2, this.width / 2 + 41, this.height / 2 - 25, 40, 20,
            I18n.format("gui.smartinterface.next"));
    }
}
