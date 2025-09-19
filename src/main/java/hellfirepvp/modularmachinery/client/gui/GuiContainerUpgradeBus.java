package hellfirepvp.modularmachinery.client.gui;

import github.kasuminova.mmce.common.upgrade.MachineUpgrade;
import github.kasuminova.mmce.common.upgrade.UpgradeType;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.gui.widget.GuiScrollbar;
import hellfirepvp.modularmachinery.common.container.ContainerUpgradeBus;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.TileUpgradeBus;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GuiContainerUpgradeBus extends GuiContainerBase<ContainerUpgradeBus> {
    public static final  ResourceLocation TEXTURES_UPGRADE_BUS = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guiupgradebus.png");
    public static final  double           FONT_SCALE           = 0.72;
    public static final  int              SLOT_START_X         = 8;
    public static final  int              SLOT_START_Y         = 17;
    public static final  int              MAX_DESC_LINES       = 15;
    private static final int              SCROLLBAR_TOP        = 17;
    private static final int              SCROLLBAR_LEFT       = 156;
    private static final int              SCROLLBAR_HEIGHT     = 106;
    private static final int              TEXT_DRAW_OFFSET_X   = 92;
    private static final int              TEXT_DRAW_OFFSET_Y   = 23;
    private final        GuiScrollbar     scrollbar            = new GuiScrollbar();
    private final        TileUpgradeBus   upgradeBus;

    public GuiContainerUpgradeBus(final TileUpgradeBus upgradeBus, final EntityPlayer player) {
        super(new ContainerUpgradeBus(upgradeBus, player));
        this.upgradeBus = upgradeBus;
        this.ySize = 213;
    }

    private static void collectBoundedMachineDescriptions(final List<String> desc,
                                                          final Map<BlockPos, DynamicMachine> boundedMachine,
                                                          final Map<UpgradeType, List<MachineUpgrade>> founded) {
        if (boundedMachine.isEmpty()) {
            desc.add(I18n.format("gui.upgradebus.bounded.empty"));
            return;
        } else {
            desc.add(I18n.format("gui.upgradebus.bounded", boundedMachine.size()));
        }

        boundedMachine.forEach((pos, machine) -> {
            desc.add(String.format("%s (%s)", machine.getLocalizedName(), MiscUtils.posToString(pos)));
            founded.forEach((type, upgrades) -> {
                for (final MachineUpgrade upgrade : upgrades) {
                    if (type.isCompatible(machine)) {
                        return;
                    }

                    desc.add("   " + I18n.format(
                        "gui.upgradebus.incompatible", upgrade.getType().getLocalizedName()));
                }
            });
        });
        desc.add("");
    }

    private static void collectUpgradeDescriptions(final TileUpgradeBus.UpgradeBusProvider component, final List<String> desc, final Map<UpgradeType, List<MachineUpgrade>> founded) {
        founded.values().forEach(upgrades -> upgrades.forEach(upgrade -> {
            upgrade.readNBT(component.getUpgradeCustomData(upgrade));

            int stackSize = upgrade.getStackSize();
            desc.add(stackSize + "x " + upgrade.getType().getLocalizedName());

            List<String> busDesc = upgrade.getBusGUIDescriptions();
            if (busDesc.isEmpty()) {
                return;
            }

            desc.addAll(busDesc);
            desc.add("");
        }));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;

        TileUpgradeBus.UpgradeBusProvider component = upgradeBus.provideComponent();
        FontRenderer fr = this.fontRenderer;
        fr.drawStringWithShadow(I18n.format("gui.upgradebus.title"), 7, 5, 0xFFFFFF);

        GlStateManager.scale(FONT_SCALE, FONT_SCALE, FONT_SCALE);

        List<String> description = new ArrayList<>();
        Map<BlockPos, DynamicMachine> boundedMachine = component.getBoundedMachine();
        Map<UpgradeType, List<MachineUpgrade>> upgrades = component.getUpgrades(null);

        collectBoundedMachineDescriptions(description, boundedMachine, upgrades);
        collectUpgradeDescriptions(component, description, upgrades);

        //noinspection SimplifyStreamApiCallChains
        List<String> wrappedDesc = description.stream()
                                              .flatMap(s -> fr.listFormattedStringToWidth(s, (int) (89 * (1 / FONT_SCALE))).stream())
                                              .collect(Collectors.toList());
        updateScrollbar(x, y, Math.max(0, wrappedDesc.size() - MAX_DESC_LINES));

        int offsetY = TEXT_DRAW_OFFSET_Y;
        for (int i = scrollbar.getCurrentScroll(); i < Math.min(wrappedDesc.size(), MAX_DESC_LINES + scrollbar.getCurrentScroll()); i++) {
            fr.drawStringWithShadow(wrappedDesc.get(i), TEXT_DRAW_OFFSET_X, offsetY, 0xFFFFFF);
            offsetY += 10;
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        final int i = Mouse.getEventDWheel();
        if (i != 0) {
            scrollbar.wheel(i);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        scrollbar.click(mouseX, mouseY);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        scrollbar.click(mouseX, mouseY);
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void updateScrollbar(int displayX, int displayY, int range) {
        scrollbar.setLeft(SCROLLBAR_LEFT + displayX).setTop(SCROLLBAR_TOP + displayY).setHeight(SCROLLBAR_HEIGHT)
                 .setRange(0, range, 1);
    }

    @Override
    protected void setWidthHeight() {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_UPGRADE_BUS);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        int x = SLOT_START_X;
        int y = SLOT_START_Y;
        for (int index = 0; index < upgradeBus.getInventory().getSlots(); index++) {
            this.drawTexturedModalRect(i + x - 1, j + y - 1, 7, 130, 18, 18);
            x += 18;

            if ((index + 1) % 3 == 0) {
                x = SLOT_START_X;
                y += 18;
            }
        }
        scrollbar.draw(this, mc);
    }
}
