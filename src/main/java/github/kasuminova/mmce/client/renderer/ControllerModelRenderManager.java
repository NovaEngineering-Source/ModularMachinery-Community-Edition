package github.kasuminova.mmce.client.renderer;

import github.kasuminova.mmce.client.util.ReusableVBOUploader;
import github.kasuminova.mmce.common.util.concurrent.Action;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ControllerModelRenderManager {

    public static final ControllerModelRenderManager INSTANCE = new ControllerModelRenderManager();

    private static final ReusableVBOUploader VBO_UPLOADER = new ReusableVBOUploader();

    // RenderType -> TextureLocation -> Light -> Buffers.
    private final Map<RenderType, Map<ResourceLocation, Int2ObjectMap<List<BufferBuilder>>>> buffers              = new EnumMap<>(RenderType.class);
    private final Map<Object, Action>                                                        reinitializeCallback = new Reference2ObjectOpenHashMap<>();

    private final Set<TileMultiblockMachineController> toRender = new ReferenceOpenHashSet<>();
    private final Set<TileMultiblockMachineController> alive    = new ReferenceOpenHashSet<>();

    public void addBuffer(int light, RenderType type, ResourceLocation textureGroup, BufferBuilder buffer) {
        buffers.computeIfAbsent(type, t -> new Object2ObjectOpenHashMap<>())
               .computeIfAbsent(textureGroup, t -> new Int2ObjectOpenHashMap<>())
               .computeIfAbsent(light, t -> new ObjectArrayList<>())
               .add(buffer);
    }

    public void addReinitializeCallback(Object key, Action action) {
        reinitializeCallback.put(key, action);
    }

    public void reinitialize() {
        reinitializeCallback.values().forEach(Action::doAction);
        reinitializeCallback.clear();
    }

    public void addToRender(TileMultiblockMachineController ctrl) {
        toRender.add(ctrl);
        notifyAlive(ctrl);
    }

    public void notifyAlive(TileMultiblockMachineController ctrl) {
        alive.add(ctrl);
    }

    public void checkControllerState() {
        Set<TileMultiblockMachineController> toRemove = new ReferenceOpenHashSet<>(toRender);
        toRemove.removeAll(alive);
        for (final TileMultiblockMachineController ctrl : toRemove) {
            GeoModelRenderTask removed = MachineControllerRenderer.INSTANCE.tasks.remove(ctrl);
            if (removed != null) {
                try {
                    //noinspection FinalizeCalledExplicitly
                    removed.finalize();
                } catch (Throwable ignored) {
                }
            }
            toRender.remove(ctrl);
            if (MachineControllerRenderer.shouldUseBloom()) {
                BloomGeoModelRenderer.INSTANCE.unregisterGlobal(ctrl);
            }
        }
        alive.clear();
    }

    /**
     * TODO: Prevents precision overflow due to oversize coordinates.
     */
    public void draw() {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);

        buffers.forEach((type, textureGroups) -> {
            type.preDraw();
            textureGroups.forEach((textureLoc, lightBuffers) -> {
                Minecraft.getMinecraft().renderEngine.bindTexture(textureLoc);
                lightBuffers.forEach((light, buffers) -> {
                    if (light != -1) {
                        int lx = light % 65536;
                        int ly = light / 65536;
                        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lx, ly);
                        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                    }
                    VBO_UPLOADER.drawMultiple(buffers);
                });
            });
            type.postDraw();
        });
        buffers.clear();

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.popMatrix();
        reinitialize();
    }

}
