package github.kasuminova.mmce.client.gui;

import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.core.localization.GuiText;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import appeng.fluids.client.gui.widgets.GuiFluidTank;
import appeng.fluids.util.IAEFluidTank;
import github.kasuminova.mmce.common.container.ContainerMEFluidInputBus;
import github.kasuminova.mmce.common.tile.MEFluidInputBus;
import github.kasuminova.mmce.common.tile.base.MEFluidBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.List;

public class GuiMEFluidInputBus extends GuiUpgradeable {
    private static final ResourceLocation TEXTURES_INPUT_BUS = new ResourceLocation(ModularMachinery.MODID, "textures/gui/mefluidinputbus.png");

    private final MEFluidInputBus bus;

    public GuiMEFluidInputBus(final MEFluidInputBus te, final EntityPlayer player) {
        super(new ContainerMEFluidInputBus(te, player));
        this.bus = te;
        this.ySize = 231;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void initGui() {
        super.initGui();

        final IAEFluidTank configFluids = bus.getConfig();
        final IAEFluidTank fluidTank = bus.getTanks();

        for (int i = 0; i < MEFluidBus.TANK_SLOT_AMOUNT; i++) {
            final GuiFluidTank guiTank = new GuiFluidTank(fluidTank, i, MEFluidBus.TANK_SLOT_AMOUNT + i,
                8 + 18 * i, 53, 16, 68);

            // AE2 Unofficial Extended Life Check
            if (Mods.AE2EL.isPresent()) {
                this.guiSlots.add(guiTank);
            } else {
                // Default AE2
                int x = ObfuscationReflectionHelper.getPrivateValue(GuiCustomSlot.class, guiTank, "x");
                int y = ObfuscationReflectionHelper.getPrivateValue(GuiCustomSlot.class, guiTank, "y");
                ObfuscationReflectionHelper.setPrivateValue(GuiCustomSlot.class, guiTank, getGuiLeft() + x, "x");
                ObfuscationReflectionHelper.setPrivateValue(GuiCustomSlot.class, guiTank, getGuiTop() + y, "y");
                List<Object> buttonList = (List) this.buttonList;
                buttonList.add(guiTank);
            }

            this.guiSlots.add(new GuiFluidSlot(configFluids, i, i, 8 + 18 * i, 35));
        }
    }

    @Override
    protected void addButtons() {
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("gui.mefluidinputbus.title"), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.Config.getLocal(), 8, 6 + 11 + 7, 4210752);
        this.fontRenderer.drawString(GuiText.StoredFluids.getLocal(), 8, 6 + 112 + 7, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(TEXTURES_INPUT_BUS);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }
}
