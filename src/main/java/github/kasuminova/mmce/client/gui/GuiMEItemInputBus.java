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

    public GuiMEItemInputBus(final MEItemInputBus te, final EntityPlayer player) {
        super(new ContainerMEItemInputBus(te, player));
        this.ySize = 204;
    }

    private static List<String> getAddActionInfo() {
        List<String> tooltip = new ArrayList<>();
        tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action"));
        // Quite a sight, isn't it?
        // It was truly a beautiful sight...

        if (isShiftDown() && isControlDown()) {
            String keyCombination = "SHIFT + CTRL";
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.multiply",
                    keyCombination));
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.divide",
                    keyCombination));
        } else {
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.increase.normal"));
            tooltip.add(TextFormatting.GRAY + I18n.format("gui.meiteminputbus.inv_action.decrease.normal"));
        }

        return tooltip;
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

        int stackCount = stack.getCount();
        int countToSend = getUpdatedCount(isScrollingUp(wheel), stackCount);

        if (countToSend > 0) {
            if (countToSend > slot.getSlotStackLimit()) {
                return;
            }
        }

        ClientProxy.clientScheduler.addRunnable(() -> sendInvActionToServer(slot.slotNumber, countToSend), 0);
    }

    private boolean isScrollingUp(int wheel) {
        return wheel >= 0;
    }

    private int getUpdatedCount(boolean isScrollingUp, int currentAmount) {
        if (isShiftDown() && isControlDown()) {
            if (isScrollingUp) {
                // Overflow protection
                if (currentAmount <= Integer.MAX_VALUE / 2) {
                    return 2 * currentAmount;
                }
                return Integer.MAX_VALUE;
            } else {
                return Math.max(1, currentAmount / 2);
            }
        } else {
            if (isScrollingUp) {
                // Overflow protection
                if (currentAmount < Integer.MAX_VALUE) {
                    return 1 + currentAmount;
                }
                return Integer.MAX_VALUE;
            } else {
                return Math.max(1, currentAmount - 1);
            }
        }
    }

    public void sendInvActionToServer(int slotNumber, int amountToSend) {
        if (amountToSend == 0) {
            return;
        }
        ModularMachinery.NET_CHANNEL.sendToServer(new PktMEInputBusInvAction(
            amountToSend, slotNumber
        ));
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
