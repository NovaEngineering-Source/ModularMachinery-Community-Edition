package github.kasuminova.mmce.client.gui;

import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.core.localization.GuiText;
import appeng.fluids.client.gui.widgets.GuiFluidTank;
import appeng.fluids.util.IAEFluidTank;
import github.kasuminova.mmce.common.container.ContainerMEFluidOutputBus;
import github.kasuminova.mmce.common.tile.MEFluidOutputBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.List;

public class GuiMEFluidOutputBus extends GuiUpgradeable {
    private static final ResourceLocation TEXTURES_OUTPUT_BUS = new ResourceLocation(ModularMachinery.MODID, "textures/gui/mefluidoutputbus.png");

    private final MEFluidOutputBus bus;

    public GuiMEFluidOutputBus(final MEFluidOutputBus te, final EntityPlayer player) {
        super(new ContainerMEFluidOutputBus(te, player));
        this.bus = te;
        this.ySize = 204;
    }

    @Override
    public void initGui() {
        super.initGui();

        final IAEFluidTank fluidTank = this.bus.getTanks();

        for (int i = 0; i < MEFluidOutputBus.TANK_SLOT_AMOUNT; i++) {
            final GuiFluidTank guiTank = new GuiFluidTank(fluidTank, i, i,
                    8 + 18 * i, 26, 16, 68);

            // AE2 Unofficial Extended Life Check
            if (Mods.AE2EL.isPresent()) {
                this.guiSlots.add(guiTank);
            } else {
                // Default AE2
                ObfuscationReflectionHelper.setPrivateValue(GuiCustomSlot.class, guiTank, getGuiLeft(), "x");
                ObfuscationReflectionHelper.setPrivateValue(GuiCustomSlot.class, guiTank, getGuiTop(), "y");
                List<Object> buttonList = (List) this.buttonList;
                buttonList.add(guiTank);
            }
        }
    }

    @Override
    protected void addButtons() {
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(I18n.format("gui.mefluidoutputbus.title"), 8, 8, 0x404040);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 104, 0x404040);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.mc.getTextureManager().bindTexture(TEXTURES_OUTPUT_BUS);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }
}
