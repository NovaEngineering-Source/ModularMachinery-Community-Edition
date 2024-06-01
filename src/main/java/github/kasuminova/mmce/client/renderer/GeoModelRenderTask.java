package github.kasuminova.mmce.client.renderer;

import github.kasuminova.mmce.client.model.MachineControllerModel;
import github.kasuminova.mmce.client.util.BufferBuilderPool;
import github.kasuminova.mmce.client.util.BufferProvider;
import github.kasuminova.mmce.common.concurrent.TaskExecutor;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import io.netty.util.internal.ThrowableUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.RecursiveAction;

public class GeoModelRenderTask extends RecursiveAction implements BufferProvider {
    protected final MachineControllerRenderer renderer;
    protected final TileMultiblockMachineController ctrl;

    protected volatile BufferBuilder buffer;
    protected volatile BufferBuilder bloomBuffer;
    protected volatile BufferBuilder transparentBuffer;
    protected volatile BufferBuilder bloomTransparentBuffer;

    public GeoModelRenderTask(final MachineControllerRenderer renderer, final TileMultiblockMachineController ctrl) {
        this.renderer = renderer;
        this.ctrl = ctrl;
    }

    @Override
    protected synchronized void compute() {
        try {
            renderer.renderAsync(ctrl, this);
        } catch (Throwable e) {
            ModularMachinery.log.warn("[MMCE-AsyncRender] Failed to render controller model!");
            ModularMachinery.log.warn(ThrowableUtil.stackTraceToString(e));
        }
    }

    public void renderAll() {
        renderDefault();
        renderBloom(true);
    }

    public void renderDefault() {
        if (buffer == null && transparentBuffer == null) {
            return;
        }
        MachineControllerModel currentModel = ctrl.getCurrentModel();
        if (currentModel == null) {
            return;
        }
        ResourceLocation texture = currentModel.getTextureLocation();

        if (buffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(RenderType.DEFAULT, texture, buffer);
        }
        if (transparentBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(RenderType.TRANSPARENT, texture, transparentBuffer);
        }
    }

    public void renderBloom(boolean postProcessing) {
        if (bloomBuffer == null && bloomTransparentBuffer == null) {
            return;
        }
        MachineControllerModel currentModel = ctrl.getCurrentModel();
        if (currentModel == null) {
            return;
        }
        ResourceLocation texture = currentModel.getTextureLocation();

        if (bloomBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(RenderType.BLOOM, texture, bloomBuffer);
        }
        if (bloomTransparentBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(RenderType.BLOOM_TRANSPARENT, texture, bloomTransparentBuffer);
        }

        if (postProcessing) {
            ControllerModelRenderManager.INSTANCE.addReinitializeCallback(this, () -> {
                reinitialize();
                MachineControllerRenderer.INSTANCE.tasks.put(ctrl, (GeoModelRenderTask) TaskExecutor.FORK_JOIN_POOL.submit(this));
            });
        }
    }

    @Override
    public void reinitialize() {
        super.reinitialize();
    }

    @Override
    public BufferBuilder getBuffer() {
        return buffer;
    }

    @Override
    public BufferBuilder getBuffer(final boolean bloom, final boolean transparent) {
        if (bloom && transparent) {
            if (bloomTransparentBuffer == null) {
                bloomTransparentBuffer = BufferBuilderPool.borrowBuffer(1024);
                bloomTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return bloomTransparentBuffer;
        }
        if (bloom) {
            if (bloomBuffer == null) {
                bloomBuffer = BufferBuilderPool.borrowBuffer(2 * 1024);
                bloomBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return bloomBuffer;
        }
        if (transparent) {
            if (transparentBuffer == null) {
                transparentBuffer = BufferBuilderPool.borrowBuffer(2 * 1024);
                transparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return transparentBuffer;
        }
        if (buffer == null) {
            buffer = BufferBuilderPool.borrowBuffer(4 * 1024);
            buffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
        }
        return buffer;
    }

    @Override
    public void begin() {
        if (buffer != null) {
            buffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
        }
        if (transparentBuffer != null) {
            transparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
        }
        if (bloomBuffer != null) {
            bloomBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
        }
        if (bloomTransparentBuffer != null) {
            bloomTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
        }
    }

    @Override
    public void finishDrawing() {
        if (buffer != null) {
            buffer.finishDrawing();
        }
        if (bloomBuffer != null) {
            bloomBuffer.finishDrawing();
        }
        if (transparentBuffer != null) {
            transparentBuffer.finishDrawing();
        }
        if (bloomTransparentBuffer != null) {
            bloomTransparentBuffer.finishDrawing();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        if (buffer == null && bloomBuffer == null && transparentBuffer == null && bloomTransparentBuffer == null) {
            super.finalize();
            return;
        }

        Minecraft.getMinecraft().addScheduledTask(this::clean);
        super.finalize();
    }

    private synchronized void clean() {
        if (buffer != null) {
            BufferBuilderPool.returnBuffer(buffer);
            buffer = null;
        }
        if (bloomBuffer != null) {
            BufferBuilderPool.returnBuffer(bloomBuffer);
            bloomBuffer = null;
        }
        if (transparentBuffer != null) {
            BufferBuilderPool.returnBuffer(transparentBuffer);
            transparentBuffer = null;
        }
        if (bloomTransparentBuffer != null) {
            BufferBuilderPool.returnBuffer(bloomTransparentBuffer);
            bloomTransparentBuffer = null;
        }
    }

}
