package github.kasuminova.mmce.client.renderer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;

public enum RenderType {

    DEFAULT(),

    TRANSPARENT() {
        @Override
        public void preDraw() {
            GlStateManager.depthMask(false);
        }

        @Override
        public void postDraw() {
            GlStateManager.depthMask(true);
        }
    },

    BLOOM() {
        @Override
        public void preDraw() {
            lastBrightnessX = OpenGlHelper.lastBrightnessX;
            lastBrightnessY = OpenGlHelper.lastBrightnessY;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        }

        @Override
        public void postDraw() {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        }
    },

    BLOOM_TRANSPARENT() {
        @Override
        public void preDraw() {
            BLOOM.preDraw();
            TRANSPARENT.preDraw();
        }

        @Override
        public void postDraw() {
            TRANSPARENT.postDraw();
            BLOOM.postDraw();
        }
    },
    ;

    protected static float lastBrightnessX = 0.0F;
    protected static float lastBrightnessY = 0.0F;

    public void preDraw() {
    }

    public void postDraw() {
    }

}
