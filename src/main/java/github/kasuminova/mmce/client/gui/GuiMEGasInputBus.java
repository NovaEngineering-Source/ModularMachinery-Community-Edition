package github.kasuminova.mmce.client.gui;

import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.core.localization.GuiText;
import com.mekeng.github.client.slots.SlotGas;
import com.mekeng.github.client.slots.SlotGasTank;
import com.mekeng.github.common.me.inventory.IGasInventory;
import github.kasuminova.mmce.common.container.ContainerMEGasInputBus;
import github.kasuminova.mmce.common.tile.MEGasInputBus;
import github.kasuminova.mmce.common.tile.base.MEFluidBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiMEGasInputBus extends GuiUpgradeable {
    private static final ResourceLocation TEXTURES_INPUT_BUS = new ResourceLocation(ModularMachinery.MODID, "textures/gui/mefluidinputbus.png");

    private final MEGasInputBus bus;

    public GuiMEGasInputBus(final MEGasInputBus te, final EntityPlayer player) {
        super(new ContainerMEGasInputBus(te, player));
        this.bus = te;
        this.ySize = 231;
    }

    @Override
    public void initGui() {
        super.initGui();

        final IGasInventory configFluids = bus.getConfig();
        final IGasInventory fluidTank = bus.getTanks();

        for (int i = 0; i < MEFluidBus.TANK_SLOT_AMOUNT; i++) {
            this.guiSlots.add(new SlotGasTank(fluidTank, i, MEFluidBus.TANK_SLOT_AMOUNT + i, 8 + 18 * i, 53, 16, 68));
            this.guiSlots.add(new SlotGas(configFluids, i, i, 8 + 18 * i, 35));
        }
    }

    @Override
    protected void addButtons() {
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("gui.megasinputbus.title"), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.Config.getLocal(), 8, 6 + 11 + 7, 4210752);
        this.fontRenderer.drawString(I18n.format("tooltip.mekeng.stored_gas"), 8, 6 + 112 + 7, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(TEXTURES_INPUT_BUS);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }
}
