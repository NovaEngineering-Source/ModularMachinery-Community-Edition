package github.kasuminova.mmce.client.gui;

import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.core.localization.GuiText;
import com.mekeng.github.client.slots.SlotGasTank;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import github.kasuminova.mmce.common.container.ContainerMEGasOutputBus;
import github.kasuminova.mmce.common.tile.MEGasOutputBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiMEGasOutputBus extends GuiUpgradeable {
    private static final ResourceLocation TEXTURES_OUTPUT_BUS = new ResourceLocation(ModularMachinery.MODID, "textures/gui/mefluidoutputbus.png");

    private final MEGasOutputBus bus;

    public GuiMEGasOutputBus(final MEGasOutputBus te, final EntityPlayer player) {
        super(new ContainerMEGasOutputBus(te, player));
        this.bus = te;
        this.ySize = 204;
    }

    @Override
    public void initGui() {
        super.initGui();

        final GasInventory fluidTank = this.bus.getTanks();

        for (int i = 0; i < MEGasOutputBus.TANK_SLOT_AMOUNT; i++) {
            this.guiSlots.add(new SlotGasTank(fluidTank, i, i, 8 + 18 * i, 26, 16, 68));
        }
    }

    @Override
    protected void addButtons() {
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(I18n.format("gui.megasoutputbus.title"), 8, 8, 0x404040);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 104, 0x404040);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.mc.getTextureManager().bindTexture(TEXTURES_OUTPUT_BUS);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }
}
