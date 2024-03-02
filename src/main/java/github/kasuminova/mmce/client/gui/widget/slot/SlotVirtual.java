package github.kasuminova.mmce.client.gui.widget.slot;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.WidgetContainer;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.integration.ingredient.IngredientItemStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class SlotVirtual extends DynamicWidget {

    protected ResourceLocation slotTexLocation = null;
    protected int slotTexX = 0;
    protected int slotTexY = 0;

    protected ItemStack stackInSlot = ItemStack.EMPTY;
    protected Function<ItemStack, List<String>> tooltipFunction = null;

    protected boolean mouseOver = false;

    public SlotVirtual() {
        setWidthHeight(18, 18);
    }

    public SlotVirtual(final ItemStack stackInSlot) {
        this();
        this.stackInSlot = stackInSlot;
    }

    public static SlotVirtualSelectable ofSelectable() {
        return new SlotVirtualSelectable();
    }

    public static SlotVirtualSelectable ofSelectable(final ItemStack stackInSlot) {
        return new SlotVirtualSelectable(stackInSlot);
    }

    public static SlotVirtual ofJEI() {
        return Mods.JEI.isPresent() ? new SlotVirtualJEI() : new SlotVirtual();
    }

    public static SlotVirtual ofJEI(final ItemStack stackInSlot) {
        return Mods.JEI.isPresent() ? new SlotVirtualJEI(stackInSlot) : new SlotVirtual(stackInSlot);
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

    protected void drawHoverOverlay(final MousePos mousePos, final int rx, final int ry) {
        if (isMouseOver(mousePos)) {
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            GuiScreen.drawRect(rx, ry, rx + 16, ry + 16, new Color(255, 255, 255, 150).getRGB());
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
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
        WidgetContainer.disableScissor();
        GuiScreen g = gui.getGui();
        GuiUtils.preItemToolTip(stackInSlot);

        List<String> toolTip = g.getItemToolTip(stackInSlot);
        if (tooltipFunction != null) {
            toolTip.addAll(tooltipFunction.apply(stackInSlot));
        }
        g.drawHoveringText(toolTip, mousePos.mouseX() + renderPos.posX(), mousePos.mouseY() + renderPos.posY());

        GuiUtils.postItemToolTip();
        WidgetContainer.enableScissor();
    }

    @Override
    public List<String> getHoverTooltips(final MousePos mousePos) {
        mouseOver = true;
        return Collections.emptyList();
    }

    // Tooltip function

    public SlotVirtual setTooltipFunction(final Function<ItemStack, List<String>> tooltipFunction) {
        this.tooltipFunction = tooltipFunction;
        return this;
    }

    public Function<ItemStack, List<String>> getTooltipFunction() {
        return tooltipFunction;
    }

    // Texture Location.

    public SlotVirtual setSlotTexLocation(final ResourceLocation slotTexLocation) {
        this.slotTexLocation = slotTexLocation;
        return this;
    }

    public SlotVirtual setSlotTexX(final int slotTexX) {
        this.slotTexX = slotTexX;
        return this;
    }

    public SlotVirtual setSlotTexY(final int slotTexY) {
        this.slotTexY = slotTexY;
        return this;
    }

    public ItemStack getStackInSlot() {
        return stackInSlot;
    }

    public SlotVirtual setStackInSlot(final ItemStack stackInSlot) {
        this.stackInSlot = stackInSlot;
        return this;
    }
}