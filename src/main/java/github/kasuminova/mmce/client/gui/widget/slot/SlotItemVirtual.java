package github.kasuminova.mmce.client.gui.widget.slot;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.integration.ingredient.IngredientItemStackRenderer;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class SlotItemVirtual extends SlotVirtual {
    protected ItemStack stackInSlot = ItemStack.EMPTY;

    protected Function<ItemStack, List<String>> tooltipFunction = null;

    public SlotItemVirtual() {
    }

    public SlotItemVirtual(final ItemStack stackInSlot) {
        this();
        this.stackInSlot = stackInSlot;
    }

    public static SlotItemVirtualSelectable ofSelectable() {
        return new SlotItemVirtualSelectable();
    }

    public static SlotItemVirtualSelectable ofSelectable(final ItemStack stackInSlot) {
        return new SlotItemVirtualSelectable(stackInSlot);
    }

    public static SlotItemVirtual of() {
        return new SlotItemVirtual();
    }

    public static SlotItemVirtual of(final ItemStack stackInSlot) {
        return new SlotItemVirtual(stackInSlot);
    }

    public static SlotItemVirtual ofJEI() {
        return Mods.JEI.isPresent() ? new SlotItemVirtualJEI() : new SlotItemVirtual();
    }

    public static SlotItemVirtual ofJEI(final ItemStack stackInSlot) {
        return Mods.JEI.isPresent() ? new SlotItemVirtualJEI(stackInSlot) : new SlotItemVirtual(stackInSlot);
    }

    @Override
    public void preRender(final WidgetGui widgetGui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {

    }

    @Override
    public void render(final WidgetGui widgetGui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        GuiScreen gui = widgetGui.getGui();
        Minecraft mc = gui.mc;
        int rx = renderPos.posX();
        int ry = renderPos.posY();

        if (slotTexLocation != null) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            mc.getTextureManager().bindTexture(slotTexLocation);
            gui.drawTexturedModalRect(rx, ry, slotTexX, slotTexY, getWidth(), getHeight());
        }

        if (stackInSlot.isEmpty()) {
            return;
        }

        rx += 1;
        ry += 1;
        try {
            IngredientItemStackRenderer.DEFAULT_INSTANCE.render(mc, rx, ry, stackInSlot);
        } catch (Exception e) {
            ModularMachinery.log.warn("Failed to render virtual slot item!", e);
        }
        drawHoverOverlay(mousePos, rx, ry);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    /**
     * Renders tooltips that take advantage of the ScrollingColumn feature to determine
     * if the mouse is actually over the widget, preventing the judgment from going outside
     * the container.
     */
    @Override
    public void postRender(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (stackInSlot.isEmpty() || !mouseOver) {
            return;
        }
        mouseOver = false;
    }

    @Override
    public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
        if (stackInSlot.isEmpty()) {
            return Collections.emptyList();
        }
        mouseOver = true;

        GuiScreen g = widgetGui.getGui();
        GuiUtils.preItemToolTip(stackInSlot);

        List<String> toolTip = g.getItemToolTip(stackInSlot);
        if (tooltipFunction != null) {
            toolTip.addAll(tooltipFunction.apply(stackInSlot));
        }
        if (stackInSlot.getCount() >= 1000) {
            toolTip.add(MiscUtils.formatDecimal(stackInSlot.getCount()));
        }

        return toolTip;
    }

    public ItemStack getStackInSlot() {
        return stackInSlot;
    }

    public SlotItemVirtual setStackInSlot(final ItemStack stackInSlot) {
        this.stackInSlot = stackInSlot;
        return this;
    }

    // Tooltip function

    public SlotItemVirtual setTooltipFunction(final Function<ItemStack, List<String>> tooltipFunction) {
        this.tooltipFunction = tooltipFunction;
        return this;
    }

    public Function<ItemStack, List<String>> getTooltipFunction() {
        return tooltipFunction;
    }

}