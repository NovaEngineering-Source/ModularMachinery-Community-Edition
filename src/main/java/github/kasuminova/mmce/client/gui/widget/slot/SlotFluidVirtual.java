package github.kasuminova.mmce.client.gui.widget.slot;

import appeng.api.AEApi;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.fluids.client.render.FluidStackSizeRenderer;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.ReadableNumberConverter;
import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import hellfirepvp.modularmachinery.common.base.Mods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlotFluidVirtual extends SlotVirtual {
    protected static final FluidStackSizeRenderer SIZE_RENDERER = new FluidStackSizeRenderer();

    protected static final String[] NUMBER_FORMATS = {"#.000", "#.00", "#.0", "#"};
    protected static final IWideReadableNumberConverter WIDE_CONVERTER = ReadableNumberConverter.INSTANCE;

    protected FluidStack fluidStack = null;

    public SlotFluidVirtual(final FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }

    public SlotFluidVirtual() {
    }

    public static SlotFluidVirtual of() {
        return new SlotFluidVirtual();
    }

    public static SlotFluidVirtual of(final FluidStack fluidStack) {
        return new SlotFluidVirtual(fluidStack);
    }

    public static SlotFluidVirtual ofJEI(final FluidStack fluidStack) {
        return Mods.JEI.isPresent() ? new SlotFluidVirtualJEI(fluidStack) : new SlotFluidVirtual(fluidStack);
    }

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        Minecraft mc = Minecraft.getMinecraft();
        int rx = renderPos.posX();
        int ry = renderPos.posY();

        if (slotTexLocation != null) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            mc.getTextureManager().bindTexture(slotTexLocation);
            gui.getGui().drawTexturedModalRect(rx, ry, slotTexX, slotTexY, getWidth(), getHeight());
        }

        if (fluidStack != null && fluidStack.amount > 0) {
            int fluidColor = fluidStack.getFluid().getColor(fluidStack);
            float red = (fluidColor >> 16 & 0xFF) / 255F;
            float green = (fluidColor >> 8 & 0xFF) / 255F;
            float blue = (fluidColor & 0xFF) / 255F;
            GlStateManager.color(red, green, blue, 1.0F);

            ResourceLocation rl = fluidStack.getFluid().getStill(fluidStack);
            TextureAtlasSprite tas = mc.getTextureMapBlocks().getTextureExtry(rl.toString());
            if (tas == null) {
                tas = mc.getTextureMapBlocks().getMissingSprite();
            }

            int width = getWidth() - 2;
            int height = getHeight() - 2;
            rx += 1;
            ry += 1;

            mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            gui.getGui().drawTexturedModalRect(rx, ry, tas, width, height);

            IFluidStorageChannel channel = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
            SIZE_RENDERER.renderStackSize(mc.fontRenderer, channel.createStack(fluidStack), rx, ry);
            drawHoverOverlay(mousePos, rx, ry);

            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    /**
     * Renders tooltips that take advantage of the ScrollingColumn feature to determine
     * if the mouse is actually over the widget, preventing the judgment from going outside
     * the container.
     */
    @Override
    public void postRender(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        if (fluidStack == null || !mouseOver) {
            return;
        }
        mouseOver = false;
    }

    @Override
    public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
        if (fluidStack == null) {
            return Collections.emptyList();
        }
        mouseOver = true;

        List<String> toolTips = new ArrayList<>();
        toolTips.add(fluidStack.getLocalizedName());
        toolTips.add(fluidStack.amount + "mB" );

        return toolTips;
    }

}
