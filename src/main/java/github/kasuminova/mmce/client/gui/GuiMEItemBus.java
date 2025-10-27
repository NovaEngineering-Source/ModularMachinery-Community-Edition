package github.kasuminova.mmce.client.gui;

import appeng.client.gui.AEBaseGui;
import appeng.client.render.StackSizeRenderer;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotDisabled;
import appeng.util.item.AEItemStack;
import github.kasuminova.mmce.client.gui.slot.Size1Slot;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public abstract class GuiMEItemBus extends AEBaseGui {

    protected final StackSizeRenderer stackSizeRenderer = Mods.AE2EL.isPresent() ? null : new StackSizeRenderer();

    public GuiMEItemBus(final Container container) {
        super(container);
    }

    @Override
    public void drawSlot(final Slot s) {
        if (Mods.AE2EL.isPresent() || s.slotNumber < 36 || !(s instanceof AppEngSlot)) {
            super.drawSlot(s);
            return;
        }

        // Only for vanilla ae2.
        try {
            this.zLevel = 100.0F;
            this.itemRender.zLevel = 100.0F;

            if (!this.isPowered()) {
                drawRect(s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66111111);
            }

            this.zLevel = 0.0F;
            this.itemRender.zLevel = 0.0F;

            // Annoying but easier than trying to splice into render item
            super.drawSlot(new Size1Slot((AppEngSlot) s));

            this.stackSizeRenderer.renderStackSize(this.fontRenderer, AEItemStack.fromItemStack(s.getStack()), s.xPos, s.yPos);
        } catch (final Exception err) {
            ModularMachinery.log.warn("Prevented crash while drawing slot.", err);
        }
    }

    @Override
    protected void renderToolTip(@Nonnull final ItemStack stack, final int x, final int y) {
        final FontRenderer font = stack.getItem().getFontRenderer(stack);
        GuiUtils.preItemToolTip(stack);

        final List<String> tooltip = this.getItemToolTip(stack);
        final Slot slot = getSlot(x, y);
        if (slot instanceof SlotDisabled) {
            final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(stack.getCount());
            final String formatted = I18n.format("gui.meitembus.item_cached", formattedAmount);
            tooltip.add(TextFormatting.GRAY + formatted);
        }

        this.drawHoveringText(tooltip, x, y, (font == null ? fontRenderer : font));
        GuiUtils.postItemToolTip();
    }
}