package github.kasuminova.mmce.client.renderer;

import github.kasuminova.mmce.client.model.MachineControllerModel;
import gregtech.client.renderer.IRenderSetup;
import gregtech.client.shader.postprocessing.BloomType;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.EffectRenderContext;
import gregtech.client.utils.IBloomEffect;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BloomGeoModelRenderer implements IRenderSetup, IBloomEffect {

    public static final BloomGeoModelRenderer INSTANCE = new BloomGeoModelRenderer();

    protected final Set<TileMultiblockMachineController> controllers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    protected boolean initialized = false;
    
    protected boolean postProcessing = false;

    public void registerGlobal(final TileMultiblockMachineController ctrl) {
        if (!initialized) {
            initialized = true;
            BloomEffectUtil.registerBloomRender(this, BloomType.UNREAL, this, ticket -> true);
        }
        controllers.add(ctrl);
    }

    public void unregisterGlobal(final TileMultiblockMachineController ctrl) {
        controllers.remove(ctrl);
    }

    @Override
    public void preDraw(@Nonnull final BufferBuilder bufferBuilder) {
    }

    @Override
    public void postDraw(@Nonnull final BufferBuilder bufferBuilder) {
    }

    @Override
    public void renderBloomEffect(@Nonnull final BufferBuilder bufferBuilder, @Nonnull final EffectRenderContext ctx) {
        GlStateManager.pushMatrix();

        controllers.forEach(ctrl -> renderModel(ctrl, ctx));
        ControllerModelRenderManager.INSTANCE.draw();

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.popMatrix();
        if (postProcessing) {
            ControllerModelRenderManager.INSTANCE.checkControllerState();
        }
        postProcessing = !postProcessing;
    }

    protected void renderModel(final TileMultiblockMachineController ctrl, @Nonnull final EffectRenderContext ctx) {
        MachineControllerModel currentModel = ctrl.getCurrentModel();
        if (currentModel == null) {
            return;
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(currentModel.getTextureLocation());
        GeoModelRenderTask task = MachineControllerRenderer.INSTANCE.getTask(ctrl);
        if (!task.isAvailable()) {
            return;
        }
        // BloomEffectUtil do twice render, prevent twice reinitialize, just do once.
        task.renderBloom(postProcessing);
    }

    @Override
    public boolean shouldRenderBloomEffect(@Nonnull final EffectRenderContext context) {
        return !controllers.isEmpty();
    }

}
