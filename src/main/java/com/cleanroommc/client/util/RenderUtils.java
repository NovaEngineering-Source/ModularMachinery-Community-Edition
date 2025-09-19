package com.cleanroommc.client.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class RenderUtils {

    public static void renderBlockOverLay(BlockPos pos, float r, float g, float b, float a, float scale) {
        if (pos == null) {
            return;
        }
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.translate((pos.getX() + 0.5), (pos.getY() + 0.5), (pos.getZ() + 0.5));
        GlStateManager.scale(scale, scale, scale);

        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.disableTexture2D();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        RenderUtils.renderCubeFace(buffer, -0.5f, -0.5f, -0.5f, 0.5, 0.5, 0.5, r, g, b, a);
        tessellator.draw();

        GlStateManager.scale(1 / scale, 1 / scale, 1 / scale);
        GlStateManager.translate(-(pos.getX() + 0.5), -(pos.getY() + 0.5), -(pos.getZ() + 0.5));
        GlStateManager.enableTexture2D();

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void renderCubeFace(BufferBuilder buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
    }
}
