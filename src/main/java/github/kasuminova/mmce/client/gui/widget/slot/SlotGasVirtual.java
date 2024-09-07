package github.kasuminova.mmce.client.gui.widget.slot;

import appeng.api.AEApi;
import com.mekeng.github.client.render.GasStackSizeRenderer;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import hellfirepvp.modularmachinery.common.base.Mods;
import mekanism.api.gas.GasStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlotGasVirtual extends SlotVirtual {
    protected static final GasStackSizeRenderer SIZE_RENDERER = new GasStackSizeRenderer();

    protected GasStack gasStack = null;

    public SlotGasVirtual(final GasStack gasStack) {
        this.gasStack = gasStack;
    }

    public SlotGasVirtual() {
    }

    public static SlotGasVirtual of() {
        return new SlotGasVirtual();
    }

    public static SlotGasVirtual of(final GasStack gasStack) {
        return new SlotGasVirtual(gasStack);
    }

    public static SlotGasVirtual ofJEI(final GasStack gasStack) {
        return Mods.JEI.isPresent() ? new SlotGasVirtualJEI(gasStack) : new SlotGasVirtual(gasStack);
    }

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        Minecraft mc = Minecraft.getMinecraft();
        int rx = renderPos.posX();
        int ry = renderPos.posY();

        slotTex.renderIfPresent(renderPos, renderSize, gui, (tex) -> {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        });

        if (gasStack != null && gasStack.amount > 0) {
            int fluidColor = gasStack.getGas().getTint();
            float red = (fluidColor >> 16 & 0xFF) / 255F;
            float green = (fluidColor >> 8 & 0xFF) / 255F;
            float blue = (fluidColor & 0xFF) / 255F;
            GlStateManager.color(red, green, blue, 1.0F);

            ResourceLocation rl = gasStack.getGas().getIcon();
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

            IGasStorageChannel channel = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
            SIZE_RENDERER.renderStackSize(mc.fontRenderer, channel.createStack(gasStack), rx, ry);
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
        if (gasStack == null || !mouseOver) {
            return;
        }
        mouseOver = false;
    }

    @Override
    public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
        if (gasStack == null) {
            return Collections.emptyList();
        }
        mouseOver = true;

        List<String> toolTips = new ArrayList<>();
        toolTips.add(gasStack.getGas().getLocalizedName());
        toolTips.add(gasStack.amount + "mB");
        toolTips.add(I18n.format("tooltip.fluidhatch.gas"));

        return toolTips;
    }

}
