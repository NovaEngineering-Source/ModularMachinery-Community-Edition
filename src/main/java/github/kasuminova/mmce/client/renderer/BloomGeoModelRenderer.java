package github.kasuminova.mmce.client.renderer;

import github.kasuminova.mmce.client.model.MachineControllerModel;
import gregtech.client.renderer.IRenderSetup;
import gregtech.client.shader.postprocessing.BloomType;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.EffectRenderContext;
import gregtech.client.utils.IBloomEffect;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
//        this.lastBrightnessX = OpenGlHelper.lastBrightnessX;
//        this.lastBrightnessY = OpenGlHelper.lastBrightnessY;
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    @Override
    public void postDraw(@Nonnull final BufferBuilder bufferBuilder) {
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, this.lastBrightnessX, this.lastBrightnessY);
    }

    @Override
    public void renderBloomEffect(@Nonnull final BufferBuilder bufferBuilder, @Nonnull final EffectRenderContext ctx) {
        GlStateManager.pushMatrix();

        List<TileMultiblockMachineController> toRemove = new ArrayList<>();
        WorldClient mcWorld = Minecraft.getMinecraft().world;
        controllers.forEach(ctrl -> {
            World world = ctrl.getWorld();
            //noinspection ConstantValue
            if (ctrl.isInvalid() || world == null || world != mcWorld || mcWorld.getTileEntity(ctrl.getPos()) != ctrl) {
                toRemove.add(ctrl);
                return;
            }
            renderModel(ctrl, ctx);
        });
        ControllerModelRenderManager.INSTANCE.draw();
        toRemove.forEach(controllers::remove);

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.popMatrix();
        postProcessing = !postProcessing;
    }

    protected void renderModel(final TileMultiblockMachineController ctrl, @Nonnull final EffectRenderContext ctx) {
        MachineControllerModel currentModel = ctrl.getCurrentModel();
        if (currentModel == null) {
            return;
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(currentModel.getTextureLocation());
        // BloomEffectUtil do twice render, prevent twice reinitialize, just do once.
        MachineControllerRenderer.INSTANCE.getTask(ctrl).renderBloom(postProcessing);
    }

    @Override
    public boolean shouldRenderBloomEffect(@Nonnull final EffectRenderContext context) {
        return !controllers.isEmpty();
    }

}
