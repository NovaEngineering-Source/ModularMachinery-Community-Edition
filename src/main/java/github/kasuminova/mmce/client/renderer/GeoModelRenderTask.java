package github.kasuminova.mmce.client.renderer;

import github.kasuminova.mmce.client.util.BufferBuilderPool;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.WorldVertexBufferUploader;

import java.util.concurrent.RecursiveAction;

public class GeoModelRenderTask extends RecursiveAction {
    private final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();
    
    private final MachineControllerRenderer renderer;
    private final TileMultiblockMachineController ctrl;
    private final float partialTicks;
    private volatile BufferBuilder buffer;
    private volatile BufferBuilder emissiveBuffer;

    public GeoModelRenderTask(final MachineControllerRenderer renderer, final TileMultiblockMachineController ctrl, final float partialTicks) {
        this.renderer = renderer;
        this.ctrl = ctrl;
        this.partialTicks = partialTicks;
        this.buffer = BufferBuilderPool.borrowBuffer(1024);
        this.emissiveBuffer = BufferBuilderPool.borrowBuffer(128);
    }

    @Override
    protected void compute() {
        if (buffer == null) {
            buffer = BufferBuilderPool.borrowBuffer(1024);
        }
        if (emissiveBuffer == null) {
            emissiveBuffer = BufferBuilderPool.borrowBuffer(128);
        }
        renderer.renderAsync(ctrl, buffer, emissiveBuffer, partialTicks);
    }

    public synchronized void draw() {
        // Render default
        vboUploader.draw(buffer);
        // Render emissive
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        vboUploader.draw(emissiveBuffer);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected synchronized void finalize() throws Throwable {
        try {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (buffer != null) {
                    BufferBuilderPool.returnBuffer(buffer);
                    buffer = null;
                }
                if (emissiveBuffer != null) {
                    BufferBuilderPool.returnBuffer(emissiveBuffer);
                    emissiveBuffer = null;
                }
            });
        } finally {
            super.finalize();
        }
    }

}
