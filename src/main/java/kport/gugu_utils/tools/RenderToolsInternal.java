package kport.gugu_utils.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderToolsInternal {
    private static void line(BufferBuilder buffer, double aX, double aY, double aZ, double bX, double bY, double bZ) {
        buffer.pos(aX, aY, aZ).endVertex();
        buffer.pos(bX, bY, bZ).endVertex();
    }

    private static void lineX(BufferBuilder buffer, double aX, double bX, double Y, double Z) {
        buffer.pos(aX, Y, Z).endVertex();
        buffer.pos(bX, Y, Z).endVertex();
    }

    private static void lineY(BufferBuilder buffer, double aY, double bY, double X, double Z) {
        buffer.pos(X, aY, Z).endVertex();
        buffer.pos(X, bY, Z).endVertex();
    }

    private static void lineZ(BufferBuilder buffer, double aZ, double bZ, double X, double Y) {
        buffer.pos(X, Y, aZ).endVertex();
        buffer.pos(X, Y, bZ).endVertex();
    }


    public static void glTranslate(Vec3d vec) {
        GL11.glTranslated(vec.x, vec.y, vec.z);
    }

    public static void glTranslate(Vec3i vec) {
        GL11.glTranslated(vec.getX(), vec.getY(), vec.getZ());
    }

    public static void glColor(Color colorRGB) {
        GL11.glColor4ub((byte) colorRGB.getRed(), (byte) colorRGB.getGreen(), (byte) colorRGB.getBlue(), (byte) colorRGB.getAlpha());
    }

    public static void glColor(int color) {
        Color colorRGB = new Color(color);
        GL11.glColor4ub((byte) colorRGB.getRed(), (byte) colorRGB.getGreen(), (byte) colorRGB.getBlue(), (byte) colorRGB.getAlpha());
    }


    public static void translateGlToWorldCord(double partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();

        EntityPlayerSP p = mc.player;
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * partialTicks;
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * partialTicks;
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partialTicks;

        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);
    }

    public static Vec3d getPlayerRenderPos(double partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();

        EntityPlayerSP p = mc.player;
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * partialTicks;
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * partialTicks;
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partialTicks;

        return new Vec3d(doubleX, doubleY, doubleZ);
    }


    public static void renderOutline(Vec3d begin, Vec3d end) {
        renderOutline(begin.x, begin.y, begin.z, end.x, end.y, end.z);
    }

    public static void renderOutline(Vec3d begin, Vec3d end, double offset) {
//        renderOutline(begin.x, begin.y, begin.z, end.x, end.y, end.z);
        renderOutline(
                Math.min(begin.x, end.x) - offset,
                Math.min(begin.y, end.y) - offset,
                Math.min(begin.z, end.z) - offset,
                Math.max(begin.x, end.x) + offset,
                Math.max(begin.y, end.y) + offset,
                Math.max(begin.z, end.z) + offset);
    }

    public static void renderOutline(AxisAlignedBB aabb) {
        renderOutline(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    //画出方块边框
    public static void renderOutline(double aX, double aY, double aZ, double bX, double bY, double bZ) {

        //初始化渲染模式
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        //一个正方体有12条边
        lineX(buf, aX, bX, aY, aZ);
        lineX(buf, aX, bX, bY, bZ);
        lineX(buf, aX, bX, aY, bZ);
        lineX(buf, aX, bX, bY, aZ);

        lineY(buf, aY, bY, aX, aZ);
        lineY(buf, aY, bY, bX, bZ);
        lineY(buf, aY, bY, aX, bZ);
        lineY(buf, aY, bY, bX, aZ);

        lineZ(buf, aZ, bZ, aX, aY);
        lineZ(buf, aZ, bZ, bX, bY);
        lineZ(buf, aZ, bZ, aX, bY);
        lineZ(buf, aZ, bZ, bX, aY);

        tessellator.draw();
    }
}
