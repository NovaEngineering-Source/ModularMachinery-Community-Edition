package github.kasuminova.mmce.client.gui.slot;

import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.client.gui.widgets.GuiFluidTank;
import appeng.fluids.util.IAEFluidTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

public class GuiFullCapFluidTank extends GuiFluidTank {

    protected final IAEFluidTank tank;
    protected final int slot;

    protected boolean darkened = false;

    public GuiFullCapFluidTank(IAEFluidTank tank, int slot, int id, int x, int y, int w, int h) {
        super(tank, slot, id, x, y, w, h);
        this.tank = tank;
        this.slot = slot;
    }

    public GuiFullCapFluidTank(IAEFluidTank tank, int slot, int id, int x, int y, int w, int h, boolean darkened) {
        this(tank, slot, id, x, y, w, h);
        this.darkened = darkened;
    }

    @Override
    public void drawContent(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        final IAEFluidStack fs = this.getFluidStack();
        if (fs != null) {
            GlStateManager.disableBlend();
            GlStateManager.disableLighting();

            //drawRect( this.x, this.y, this.x + this.width, this.y + this.height, AEColor.GRAY.blackVariant | 0xFF000000 );

            final IAEFluidStack fluid = this.tank.getFluidInSlot(this.slot);
            if (fluid != null && fluid.getStackSize() > 0) {
                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

                float red = (fluid.getFluid().getColor() >> 16 & 255) / 255.0F;
                float green = (fluid.getFluid().getColor() >> 8 & 255) / 255.0F;
                float blue = (fluid.getFluid().getColor() & 255) / 255.0F;
                if (darkened) {
                    red = red * 0.4F;
                    green = green * 0.4F;
                    blue = blue * 0.4F;
                }
                GlStateManager.color(red, green, blue);

                TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getStill().toString());
                int scaledHeight = getHeight();
                int iconHeightRemainder = scaledHeight % 16;
                if (iconHeightRemainder > 0) {
                    this.drawTexturedModalRect(this.xPos(), this.yPos() + this.getHeight() - iconHeightRemainder, sprite, 16, iconHeightRemainder);
                }

                for (int i = 0; i < scaledHeight / 16; i++) {
                    this.drawTexturedModalRect(this.xPos(), this.yPos() + this.getHeight() - iconHeightRemainder - (i + 1) * 16, sprite, 16, 16);
                }
            }
        }
    }

}
