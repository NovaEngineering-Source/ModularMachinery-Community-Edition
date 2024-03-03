package kport.gugu_utils.tools;

import kport.gugu_utils.handler.ClientEventHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class RenderTools {


    public static void renderBlockOutline(BlockPos pos, double partialTicks) {
        renderOutline(new Vec3d(pos), new Vec3d(pos.add(1, 1, 1)), partialTicks);
    }

    public static void renderBlockOutline(BlockPos pos, double partialTicks, boolean renderMasked) {
        renderOutline(new Vec3d(pos), new Vec3d(pos.add(1, 1, 1)), partialTicks, renderMasked);
    }

    public static void renderBlocksOutline(List<BlockPos> posList, double partialTicks) {
        renderBlocksOutline(posList, partialTicks, false);
    }

    public static void renderBlocksOutline(List<BlockPos> posList, double partialTicks, boolean renderMasked) {
        int minX, minY, minZ, maxX, maxY, maxZ;
        if (posList.isEmpty()) {
            return;
        }
        BlockPos first = posList.get(0);
        minX = maxX = first.getX();
        minY = maxY = first.getY();
        minZ = maxZ = first.getZ();
        for (BlockPos pos : posList) {
            if (minX > pos.getX())
                minX = pos.getX();
            if (maxX < pos.getX())
                maxX = pos.getX();

            if (minY > pos.getY())
                minY = pos.getY();
            if (maxY < pos.getY())
                maxY = pos.getY();

            if (minZ > pos.getZ())
                minZ = pos.getZ();
            if (maxZ < pos.getZ())
                maxZ = pos.getZ();
        }

        renderOutline(new Vec3d(minX, minY, minZ), new Vec3d(maxX + 1, maxY + 1, maxZ + 1), partialTicks, renderMasked);
    }

    public static void renderOutline(Vec3d begin, Vec3d end, double partialTicks) {
        renderOutline(begin, end, partialTicks, false);
    }

    public static void renderOutline(Vec3d begin, Vec3d end, double partialTicks, boolean renderMasked) {

        GlStateManager.pushMatrix();
        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();

        RenderToolsInternal.glColor(Color.HSBtoRGB((float) getTimeGradient(1, 200, partialTicks), 0.7f, 1.0f));


        RenderToolsInternal.translateGlToWorldCord(partialTicks);

        //遮住的部分再来一个细一点的线
        if (renderMasked) {
            GlStateManager.disableDepth();
            GlStateManager.glLineWidth(2);
            RenderToolsInternal.renderOutline(begin, end);
            GlStateManager.enableDepth();
        }
        GlStateManager.glLineWidth(5);
        RenderToolsInternal.renderOutline(begin, end);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glPopAttrib();
        GlStateManager.popMatrix();

    }


    public static double getTimeGradient(double min, double max, double period, double partialTicks) {
        double length = max - min;
        double proportion = (ClientEventHandler.elapsedTicks + partialTicks) % period / period;
        return min + length * proportion;
    }

    public static double getTimeGradient(double max, double period, double partialTicks) {
        return getTimeGradient(0, max, period, partialTicks);
    }


}
