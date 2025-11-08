package github.kasuminova.mmce.client.gui;

import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotFake;
import appeng.core.localization.GuiText;
import github.kasuminova.mmce.common.container.ContainerMEItemInputBus;
import github.kasuminova.mmce.common.network.PktMEInputBusInvAction;
import github.kasuminova.mmce.common.tile.MEItemInputBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuiMEItemInputBus extends GuiMEItemBus {
    private static final ResourceLocation TEXTURES_INPUT_BUS = new ResourceLocation(ModularMachinery.MODID, "textures/gui/meiteminputbus.png");

    private int invActionAmount = 0;

    public GuiMEItemInputBus(final MEItemInputBus te, final EntityPlayer player) {
        super(new ContainerMEItemInputBus(te, player));
        this.ySize = 204;
    }

    private static int getAddAmount() {
        int addAmount;
        // SHIFT + CTRL + ALT 1000000
        // ALT + CTRL         100000
        // ALT + SHIFT        10000
        // SHIFT + CTRL       1000
        // CTRL               100
        // SHIFT              10
        if (isShiftDown() && isControlDown() && isAltDown()) {
            addAmount = 1_000_000;
        } else if (isAltDown() && isControlDown()) {
            addAmount = 100_000;
        } else if (isAltDown() && isShiftDown()) {
            addAmount = 10_000;
        } else if (isShiftDown() && isControlDown()) {
            addAmount = 1_000;
        } else if (isControlDown()) {
            addAmount = 100;
        } else if (isShiftDown()) {
            addAmount = 10;
        } else {
            addAmount = 1;
        }
        return addAmount;
    }

    private static List<String> getAddActionInfo() {
        List<String> tooltip = new ArrayList<>();
        tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action"));
        // Quite a sight, isn't it?
        String addAmount = MiscUtils.formatDecimal(getAddAmount());
        if (isShiftDown() && isControlDown() && isAltDown()) {
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.increase",
                    "SHIFT + CTRL + ALT", addAmount));
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.decrease",
                    "SHIFT + CTRL + ALT", addAmount));
        } else if (isAltDown() && isControlDown()) {
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.increase",
                    "CTRL + ALT", addAmount));
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.decrease",
                    "CTRL + ALT", addAmount));
        } else if (isAltDown() && isShiftDown()) {
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.increase",
                    "SHIFT + ALT", addAmount));
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.decrease",
                    "SHIFT + ALT", addAmount));
        } else if (isShiftDown() && isControlDown()) {
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.increase",
                    "SHIFT + CTRL", addAmount));
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.decrease",
                    "SHIFT + CTRL", addAmount));
        } else if (isControlDown()) {
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.increase",
                    "CTRL", addAmount));
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.decrease",
                    "CTRL", addAmount));
        } else if (isShiftDown()) {
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.increase",
                    "SHIFT", addAmount));
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.decrease",
                    "SHIFT", addAmount));
        } else {
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.increase.normal"));
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.decrease.normal"));
        }
        return tooltip;
    }

    private static boolean isAltDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }

    private static boolean isControlDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }

    private static boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        final int i = Mouse.getEventDWheel();
        if (i != 0) {
            final int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
            final int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            this.onMouseWheelEvent(x, y, i / Math.abs(i));
        }
    }

    /**
     * Override AE2EL mouseWheelEvent() to prevent SHIFT = 11.
     */
    protected void mouseWheelEvent(final int x, final int y, final int wheel) {
    }

    protected void onMouseWheelEvent(final int x, final int y, final int wheel) {
        final Slot slot = this.getSlot(x, y);
        if (!(slot instanceof SlotFake)) {
            return;
        }
        final ItemStack stack = slot.getStack();
        if (stack.isEmpty()) {
            return;
        }

        int amount = wheel < 0 ? -getAddAmount() : getAddAmount();
        int stackCount = stack.getCount();

        if (amount > 0) {
            if (stackCount + amount > slot.getSlotStackLimit()) {
                return;
            }
        } else if (stackCount - amount <= 0) {
            return;
        }

        this.invActionAmount += amount;
        ClientProxy.clientScheduler.addRunnable(() -> sendInvActionToServer(slot.slotNumber), 0);
    }

    public void sendInvActionToServer(int slotNumber) {
        if (invActionAmount == 0) {
            return;
        }
        ModularMachinery.NET_CHANNEL.sendToServer(new PktMEInputBusInvAction(
                invActionAmount, slotNumber
        ));
        invActionAmount = 0;
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(I18n.format("gui.meiteminputbus.title"), 8, 8, 0x404040);
        this.fontRenderer.drawString(GuiText.Config.getLocal(), 8, 6 + 11 + 7, 0x404040);
        this.fontRenderer.drawString(GuiText.StoredItems.getLocal(), 97, 6 + 11 + 7, 0x404040);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 93, 0x404040);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_INPUT_BUS);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void renderToolTip(@Nonnull final ItemStack stack, final int x, final int y) {
        final FontRenderer font = stack.getItem().getFontRenderer(stack);
        GuiUtils.preItemToolTip(stack);

        final List<String> tooltip = this.getItemToolTip(stack);
        final Slot slot = getSlot(x, y);
        if (slot instanceof SlotFake) {
            final String formattedAmount = MiscUtils.formatDecimal((stack.getCount()));
            final String formatted = I18n.format("gui.meiteminputbus.items_marked", formattedAmount);
            tooltip.add(TextFormatting.GRAY + formatted);
            tooltip.addAll(getAddActionInfo());

            I18n.format("gui.meiteminputbus.items_marked", formattedAmount);
        } else if (slot instanceof SlotDisabled) {
            final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(stack.getCount());
            final String formatted = I18n.format("gui.meitembus.item_cached", formattedAmount);
            tooltip.add(TextFormatting.GRAY + formatted);
        }

        this.drawHoveringText(tooltip, x, y, (font == null ? fontRenderer : font));
        GuiUtils.postItemToolTip();
    }
}