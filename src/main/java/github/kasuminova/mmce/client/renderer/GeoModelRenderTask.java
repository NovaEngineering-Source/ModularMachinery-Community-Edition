package github.kasuminova.mmce.client.renderer;

import github.kasuminova.mmce.client.model.MachineControllerModel;
import github.kasuminova.mmce.client.model.ModelBufferSize;
import github.kasuminova.mmce.client.util.BufferBuilderPool;
import github.kasuminova.mmce.client.util.BufferProvider;
import github.kasuminova.mmce.common.concurrent.TaskExecutor;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import io.netty.util.internal.ThrowableUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.RecursiveAction;

@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
public class GeoModelRenderTask extends RecursiveAction implements BufferProvider {
    protected final MachineControllerRenderer renderer;
    protected final TileMultiblockMachineController ctrl;

    protected volatile BufferBuilder buffer;
    protected volatile BufferBuilder bloomBuffer;
    protected volatile BufferBuilder transparentBuffer;
    protected volatile BufferBuilder bloomTransparentBuffer;

    protected int bufferSize = 0;
    protected int bloomBufferSize = 0;
    protected int transparentBufferSize = 0;
    protected int bloomTransparentBufferSize = 0;

    protected volatile BufferBuilder staticBuffer;
    protected volatile BufferBuilder staticBloomBuffer;
    protected volatile BufferBuilder staticTransparentBuffer;
    protected volatile BufferBuilder staticBloomTransparentBuffer;

    protected int staticBufferSize = 0;
    protected int staticBloomBufferSize = 0;
    protected int staticTransparentBufferSize = 0;
    protected int staticBloomTransparentBufferSize = 0;

    protected boolean available = true;

    protected MachineControllerModel currentModel = null;

    public GeoModelRenderTask(final MachineControllerRenderer renderer, final TileMultiblockMachineController ctrl) {
        this.renderer = renderer;
        this.ctrl = ctrl;
        if (Mods.GREGTECHCEU.isPresent()) {
            registerBloomRenderer();
        } else if (Mods.LUMENIZED.isPresent()) {
            registerBloomRendererLumenized();
        }
    }

    @SideOnly(Side.CLIENT)
    @net.minecraftforge.fml.common.Optional.Method(modid = "gregtech")
    public void registerBloomRenderer() {
        if (Mods.GREGTECHCEU.isPresent()) {
            BloomGeoModelRenderer.INSTANCE.registerGlobal(ctrl);
        }
    }

    @SideOnly(Side.CLIENT)
    @net.minecraftforge.fml.common.Optional.Method(modid = "lumenized")
    public void registerBloomRendererLumenized() {
        BloomGeoModelRenderer.INSTANCE.registerGlobal(ctrl);
    }

    @Override
    protected synchronized void compute() {
        try {
            calculateBufferSize();
            renderer.render(ctrl, this, false);
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
        if (buffer == null && transparentBuffer == null && staticBuffer == null && staticTransparentBuffer == null) {
            return;
        }
        MachineControllerModel currentModel = ctrl.getCurrentModel();
        if (currentModel == null) {
            return;
        }
        ResourceLocation texture = currentModel.getTextureLocation();

        int combinedLight = ctrl.getWorld().getCombinedLight(ctrl.getPos(), 0);

        if (buffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(combinedLight, RenderType.DEFAULT, texture, buffer);
        }
        if (transparentBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(combinedLight, RenderType.TRANSPARENT, texture, transparentBuffer);
        }
        if (staticBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(combinedLight, RenderType.DEFAULT, texture, staticBuffer);
        }
        if (staticTransparentBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(combinedLight, RenderType.TRANSPARENT, texture, staticTransparentBuffer);
        }
    }

    public void renderBloom(boolean postProcessing) {
        if (bloomBuffer == null && bloomTransparentBuffer == null && staticBloomBuffer == null && staticBloomTransparentBuffer == null) {
            return;
        }
        MachineControllerModel currentModel = ctrl.getCurrentModel();
        if (currentModel == null) {
            return;
        }
        ResourceLocation texture = currentModel.getTextureLocation();

        if (bloomBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(-1, RenderType.BLOOM, texture, bloomBuffer);
        }
        if (bloomTransparentBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(-1, RenderType.BLOOM_TRANSPARENT, texture, bloomTransparentBuffer);
        }
        if (staticBloomBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(-1, RenderType.BLOOM, texture, staticBloomBuffer);
        }
        if (staticBloomTransparentBuffer != null) {
            ControllerModelRenderManager.INSTANCE.addBuffer(-1, RenderType.BLOOM_TRANSPARENT, texture, staticBloomTransparentBuffer);
        }

        if (postProcessing) {
            ControllerModelRenderManager.INSTANCE.addReinitializeCallback(this, () -> {
                reinitialize();
                ControllerModelRenderManager.INSTANCE.addToRender(ctrl);
                MachineControllerRenderer.INSTANCE.tasks.put(ctrl, (GeoModelRenderTask) TaskExecutor.FORK_JOIN_POOL.submit(this));
            });
        }
    }

    private void calculateBufferSize() {
        MachineControllerModel currentModel = ctrl.getCurrentModel();
        if (currentModel != null) {
            ModelBufferSize modelBufferSize = currentModel.getBufferSize();
            bufferSize = modelBufferSize.getBufferSize();
            bloomBufferSize = modelBufferSize.getBloomBufferSize();
            transparentBufferSize = modelBufferSize.getTransparentBufferSize();
            bloomTransparentBufferSize = modelBufferSize.getBloomTransparentBufferSize();
            staticBufferSize = modelBufferSize.getStaticBufferSize();
            staticBloomBufferSize = modelBufferSize.getStaticBloomBufferSize();
            staticTransparentBufferSize = modelBufferSize.getStaticTransparentBufferSize();
            staticBloomTransparentBufferSize = modelBufferSize.getStaticBloomTransparentBufferSize();
        } else {
            bufferSize = 0;
            bloomBufferSize = 0;
            transparentBufferSize = 0;
            bloomTransparentBufferSize = 0;
            staticBufferSize = 0;
            staticBloomBufferSize = 0;
            staticTransparentBufferSize = 0;
            staticBloomTransparentBufferSize = 0;
        }
    }

    @Override
    public void reinitialize() {
        clean();
        available = false;
        super.reinitialize();
    }

    @Override
    public BufferBuilder getBuffer() {
        return buffer;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(final boolean available) {
        this.available = available;
    }

    public boolean shouldReRenderStatic() {
        if (currentModel != ctrl.getCurrentModel()) {
            currentModel = ctrl.getCurrentModel();
            return true;
        }
        return false;
    }

    @Override
    public BufferBuilder getBuffer(final boolean bloom, final boolean transparent, final boolean isStatic) {
        // BLOOM + TRANSPARENT
        if (bloom && transparent) {
            // Static render
            if (isStatic) {
                // Dynamic render
                if (staticBloomTransparentBuffer == null) {
                    staticBloomTransparentBuffer = BufferBuilderPool.borrowBuffer(Math.max(staticBloomTransparentBufferSize, 1024));
                    staticBloomTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
                }
                return staticBloomTransparentBuffer;
            }
            // Dynamic render
            if (bloomTransparentBuffer == null) {
                bloomTransparentBuffer = BufferBuilderPool.borrowBuffer(Math.max(bloomTransparentBufferSize, 1024));
                bloomTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return bloomTransparentBuffer;
        }
        // BLOOM
        if (bloom) {
            // Static render
            if (isStatic) {
                if (staticBloomBuffer == null) {
                    staticBloomBuffer = BufferBuilderPool.borrowBuffer(Math.max(staticBloomBufferSize, 2048));
                    staticBloomBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
                }
                return staticBloomBuffer;
            }
            // Dynamic render
            if (bloomBuffer == null) {
                bloomBuffer = BufferBuilderPool.borrowBuffer(Math.max(bloomBufferSize, 2048));
                bloomBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return bloomBuffer;
        }
        // TRANSPARENT
        if (transparent) {
            // Static render
            if (isStatic) {
                if (staticTransparentBuffer == null) {
                    staticTransparentBuffer = BufferBuilderPool.borrowBuffer(Math.max(staticTransparentBufferSize, 2048));
                    staticTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
                }
                return staticTransparentBuffer;
            }
            // Dynamic render
            if (transparentBuffer == null) {
                transparentBuffer = BufferBuilderPool.borrowBuffer(Math.max(transparentBufferSize, 2048));
                transparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return transparentBuffer;
        }
        // Static render
        if (isStatic) {
            if (staticBuffer == null) {
                staticBuffer = BufferBuilderPool.borrowBuffer(Math.max(staticBufferSize, 4096));
                staticBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return staticBuffer;
        }
        // Dynamic render
        if (buffer == null) {
            buffer = BufferBuilderPool.borrowBuffer(Math.max(bufferSize, 4096));
            buffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
        }
        return buffer;
    }

    @Override
    public void begin(final boolean isStatic) {
        if (isStatic) {
            if (staticBuffer != null) {
                staticBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            if (staticBloomBuffer != null) {
                staticBloomBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            if (staticTransparentBuffer != null) {
                staticTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            if (staticBloomTransparentBuffer != null) {
                staticBloomTransparentBuffer.begin(GL11.GL_QUADS, MachineControllerRenderer.VERTEX_FORMAT);
            }
            return;
        }

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
    public void finishDrawing(final boolean isStatic) {
        if (isStatic) {
            if (staticBuffer != null) {
                staticBuffer.finishDrawing();
            }
            if (staticBloomBuffer != null) {
                staticBloomBuffer.finishDrawing();
            }
            if (staticTransparentBuffer != null) {
                staticTransparentBuffer.finishDrawing();
            }
            if (staticBloomTransparentBuffer != null) {
                staticBloomTransparentBuffer.finishDrawing();
            }
            return;
        }

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
        if (buffer == null && bloomBuffer == null && transparentBuffer == null && bloomTransparentBuffer == null &&
            staticBuffer == null && staticBloomBuffer == null && staticTransparentBuffer == null && staticBloomTransparentBuffer == null) {
            super.finalize();
            return;
        }

        Minecraft.getMinecraft().addScheduledTask(() -> {
            clean();
            cleanStatic();
        });
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

    private synchronized void cleanStatic() {
        if (staticBuffer != null) {
            BufferBuilderPool.returnBuffer(staticBuffer);
            staticBuffer = null;
        }
        if (staticBloomBuffer != null) {
            BufferBuilderPool.returnBuffer(staticBloomBuffer);
            staticBloomBuffer = null;
        }
        if (staticTransparentBuffer != null) {
            BufferBuilderPool.returnBuffer(staticTransparentBuffer);
            staticTransparentBuffer = null;
        }
        if (staticBloomTransparentBuffer != null) {
            BufferBuilderPool.returnBuffer(staticBloomTransparentBuffer);
            staticBloomTransparentBuffer = null;
        }
    }

}
