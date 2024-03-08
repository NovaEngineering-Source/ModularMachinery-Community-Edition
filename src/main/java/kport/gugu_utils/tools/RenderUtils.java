package kport.gugu_utils.tools;

import hellfirepvp.modularmachinery.ModularMachinery;
import kport.gugu_utils.handler.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;

public final class RenderUtils {

    public static final ResourceLocation RECIPES_UI = new ResourceLocation(ModularMachinery.MODID, "textures/gui/recipes_ui.png");

    public static void drawQuantity(int x, int y, String qty, FontRenderer fontRenderer) {

        boolean large = fontRenderer.getUnicodeFlag();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);

        if (!large) {
            GlStateManager.scale(0.5f, 0.5f, 1);
        }

        GlStateManager.disableLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableDepth();

        fontRenderer.drawStringWithShadow(qty, (large ? 16 : 30) - fontRenderer.getStringWidth(qty), large ? 8 : 22, 16777215);

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static boolean inBounds(int x, int y, int w, int h, int ox, int oy) {
        return ox >= x && ox <= x + w && oy >= y && oy <= y + h;
    }

    private static void setGLColorFromInt(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        GlStateManager.color(red, green, blue, 1.0F);
    }

    private static void drawFluidTexture(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
        double uMin = textureSprite.getMinU();
        double uMax = textureSprite.getMaxU();
        double vMin = textureSprite.getMinV();
        double vMax = textureSprite.getMaxV();
        uMax = uMax - (maskRight / 16.0 * (uMax - uMin));
        vMax = vMax - (maskTop / 16.0 * (vMax - vMin));

        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder vertexBuffer = tessellator.getBuffer();
        vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexBuffer.pos(xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
        vertexBuffer.pos(xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
        vertexBuffer.pos(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
        vertexBuffer.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    public static void renderEmberBarVertical(int x, int y, float alpha, float percentage) {
        Minecraft mc = Minecraft.getMinecraft();
        double particalTicks = mc.getRenderPartialTicks();
        GlStateManager.color(1F, 1F, 1F, alpha);
        mc.renderEngine.bindTexture(RECIPES_UI);
        drawTexturedModalRect(x, y, 0, 8, 0, 5, 102);

        int voffset = ((int) ((ClientEventHandler.elapsedTicks + particalTicks) * 3)) % 87;

        drawTexturedModalRect(x + 1, y + 1, 0, 13, 0, 3, 100);

        int emberPercentage = (int) (100 * percentage);
        if (percentage > 0 && emberPercentage == 0) {
            emberPercentage = 1;
        }
        emberPercentage = Math.min(100, emberPercentage);
        int offset = 100 - emberPercentage;
        drawTexturedModalRect(x + 1, y + 1 + offset, 0, 16 + voffset, offset, 3, emberPercentage);
        GL11.glColor4ub((byte) 255, (byte) 255, (byte) 255, (byte) 255);
    }

    public static void renderManaBarVertical(int x, int y, int color, float alpha, int mana, int maxMana) {
        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.color(1F, 1F, 1F, alpha);
        mc.renderEngine.bindTexture(RECIPES_UI);
        drawTexturedModalRect(x, y, 0, 0, 0, 5, 102);

        int manaPercentage = Math.max(0, (int) ((double) mana / (double) maxMana * 100));

        if (manaPercentage == 0 && mana > 0)
            manaPercentage = 1;

        drawTexturedModalRect(x + 1, y + 1, 0, 5, 0, 3, 100);

        Color color_ = new Color(color);
        GL11.glColor4ub((byte) color_.getRed(), (byte) color_.getGreen(), (byte) color_.getBlue(), (byte) (255F * alpha));

        manaPercentage = Math.min(100, manaPercentage);
        int offset = 100 - manaPercentage;
        drawTexturedModalRect(x + 1, y + 1 + offset, 0, 5, offset, 3, Math.min(100, manaPercentage));
        GL11.glColor4ub((byte) 255, (byte) 255, (byte) 255, (byte) 255);
    }

    public static void renderItem(Minecraft mc, ItemStack item, int x, int y, @Nullable String count) {

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepth();
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(item, x, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(getFontRenderer(mc, item), item, x + 4, y, count);
        GlStateManager.disableBlend();
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
    }

    public static FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
        FontRenderer fontRenderer = ingredient.getItem().getFontRenderer(ingredient);
        if (fontRenderer == null) {
            fontRenderer = minecraft.fontRenderer;
        }
        return fontRenderer;

    }

    public static void drawTexturedModalRect(int x, int y, float z, int u, int v, int w, int h) {
        drawTexturedModalRect(x, y, z, u, v, w, h, 1 / 256F, 1 / 256F);
    }

    public static void drawTexturedModalRect(int x, int y, float z, int u, int v, int w, int h, float f, float f1) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vb = tessellator.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(x, y + h, z).tex(u * f, (v + h) * f1).endVertex();
        vb.pos(x + w, y + h, z).tex((u + w) * f, (v + h) * f1).endVertex();
        vb.pos(x + w, y, z).tex((u + w) * f, v * f1).endVertex();
        vb.pos(x, y, z).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    /**
     * Draws a texture rectangle using the texture currently bound to the TextureManager
     */
    public static void drawTexturedModalRect(int xCoord, int yCoord, float zLevel, TextureAtlasSprite textureSprite, int widthIn, int heightIn) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(xCoord, yCoord + heightIn, zLevel).tex(textureSprite.getMinU(), textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos(xCoord + widthIn, yCoord + heightIn, zLevel).tex(textureSprite.getMaxU(), textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos(xCoord + widthIn, yCoord, zLevel).tex(textureSprite.getMaxU(), textureSprite.getMinV()).endVertex();
        bufferbuilder.pos(xCoord, yCoord, zLevel).tex(textureSprite.getMinU(), textureSprite.getMinV()).endVertex();
        tessellator.draw();
    }

    /**
     * Draws a textured rectangle at z = 0. Args: x, y, u, v, width, height, textureWidth, textureHeight
     */
    public static void drawModalRectWithCustomSizedTexture(int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0.0D).tex(u * f, (v + (float) height) * f1).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex((u + (float) width) * f, (v + (float) height) * f1).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex((u + (float) width) * f, v * f1).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public static void drawRect(int offsetX, int offsetY, float zLevel, int width, int height) {
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder vb = tes.getBuffer();
        vb.begin(7, DefaultVertexFormats.POSITION_TEX);
        vb.pos(offsetX, offsetY + height, zLevel).tex(0, 1).endVertex();
        vb.pos(offsetX + width, offsetY + height, zLevel).tex(1, 1).endVertex();
        vb.pos(offsetX + width, offsetY, zLevel).tex(1, 0).endVertex();
        vb.pos(offsetX, offsetY, zLevel).tex(0, 0).endVertex();
        tes.draw();
    }

    public static void drawRect(int offsetX, int offsetY, float zLevel, int width, int height, double u, double v, double uLength, double vLength) {
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder vb = tes.getBuffer();
        vb.begin(7, DefaultVertexFormats.POSITION_TEX);
        vb.pos(offsetX, offsetY + height, zLevel).tex(u, v + vLength).endVertex();
        vb.pos(offsetX + width, offsetY + height, zLevel).tex(u + uLength, v + vLength).endVertex();
        vb.pos(offsetX + width, offsetY, zLevel).tex(u + uLength, v).endVertex();
        vb.pos(offsetX, offsetY, zLevel).tex(u, v).endVertex();
        tes.draw();
    }

    public static class FluidRenderer {
        private static final int TEX_WIDTH = 16;
        private static final int TEX_HEIGHT = 16;
        private static final int MIN_FLUID_HEIGHT = 1;

        private final int capacityMb;
        private final int width;
        private final int height;

        public FluidRenderer(int capacityMb, int width, int height) {
            this.capacityMb = capacityMb;
            this.width = width;
            this.height = height;
        }

        public void draw(Minecraft minecraft, int xPosition, int yPosition, FluidStack fluidStack) {
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.disableLighting();

            drawFluid(minecraft, xPosition, yPosition, fluidStack);

            GlStateManager.color(1, 1, 1, 1);

            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
        }

        private void drawFluid(Minecraft minecraft, int xPosition, int yPosition, FluidStack fluidStack) {
            if (fluidStack == null) {
                return;
            }

            Fluid fluid = fluidStack.getFluid();

            if (fluid == null) {
                return;
            }

            TextureMap textureMapBlocks = minecraft.getTextureMapBlocks();
            ResourceLocation fluidStill = fluid.getStill();
            TextureAtlasSprite fluidStillSprite = null;

            if (fluidStill != null) {
                fluidStillSprite = textureMapBlocks.getTextureExtry(fluidStill.toString());
            }

            if (fluidStillSprite == null) {
                fluidStillSprite = textureMapBlocks.getMissingSprite();
            }

            int fluidColor = fluid.getColor(fluidStack);

            int scaledAmount = height;

            if (capacityMb != -1) {
                scaledAmount = (fluidStack.amount * height) / capacityMb;

                if (fluidStack.amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
                    scaledAmount = MIN_FLUID_HEIGHT;
                }

                if (scaledAmount > height) {
                    scaledAmount = height;
                }
            }

            minecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            setGLColorFromInt(fluidColor);

            int xTileCount = width / TEX_WIDTH;
            int xRemainder = width - (xTileCount * TEX_WIDTH);
            int yTileCount = scaledAmount / TEX_HEIGHT;
            int yRemainder = scaledAmount - (yTileCount * TEX_HEIGHT);

            int yStart = yPosition + height;

            for (int xTile = 0; xTile <= xTileCount; xTile++) {
                for (int yTile = 0; yTile <= yTileCount; yTile++) {
                    int width = (xTile == xTileCount) ? xRemainder : TEX_WIDTH;
                    int height = (yTile == yTileCount) ? yRemainder : TEX_HEIGHT;
                    int x = xPosition + (xTile * TEX_WIDTH);
                    int y = yStart - ((yTile + 1) * TEX_HEIGHT);

                    if (width > 0 && height > 0) {
                        int maskTop = TEX_HEIGHT - height;
                        int maskRight = TEX_WIDTH - width;

                        drawFluidTexture(x, y, fluidStillSprite, maskTop, maskRight, 100);
                    }
                }
            }
        }
    }


}
