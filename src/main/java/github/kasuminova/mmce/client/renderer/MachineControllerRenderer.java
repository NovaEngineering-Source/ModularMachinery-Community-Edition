package github.kasuminova.mmce.client.renderer;

import github.kasuminova.mmce.client.model.MachineControllerModel;
import github.kasuminova.mmce.client.util.BufferProvider;
import github.kasuminova.mmce.client.util.MatrixStack;
import github.kasuminova.mmce.common.concurrent.TaskExecutor;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.geo.render.built.*;

import javax.annotation.Nonnull;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MachineControllerRenderer extends TileEntitySpecialRenderer<TileMultiblockMachineController> {

    public static final MachineControllerRenderer INSTANCE = new MachineControllerRenderer();

    public static final VertexFormat VERTEX_FORMAT = DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL;

    protected static final ThreadLocal<MatrixStack> MATRIX_STACK = ThreadLocal.withInitial(MatrixStack::new);
    protected final Map<TileMultiblockMachineController, GeoModelRenderTask> tasks = new WeakHashMap<>();

    static {
        if (Mods.GECKOLIB.isPresent()) {
            AnimationController.addModelFetcher((IAnimatable object) -> {
                if (object instanceof TileMultiblockMachineController ctrl) {
                    MachineControllerModel currentModel = ctrl.getCurrentModel();
                    if (currentModel != null) {
                        return (IAnimatableModel) currentModel;
                    }
                }
                return null;
            });
        }
    }

    protected MachineControllerRenderer() {
    }

    protected static void rotateBlock(EnumFacing facing) {
        switch (facing) {
            case SOUTH -> GlStateManager.rotate(180, 0, 1, 0);
            case WEST -> GlStateManager.rotate(90, 0, 1, 0);
            /* There is no need to rotate by 0 */
            case NORTH -> {
            }
            case EAST -> GlStateManager.rotate(270, 0, 1, 0);
            case UP -> GlStateManager.rotate(90, 1, 0, 0);
            case DOWN -> GlStateManager.rotate(90, -1, 0, 0);
        }
    }

    protected static void rotateBlockMatrix(EnumFacing facing) {
        switch (facing) {
            // 180
            case SOUTH -> MATRIX_STACK.get().rotateY(3.141592653589793f);
            // 90
            case WEST -> MATRIX_STACK.get().rotateY(1.5707963267948966f);
            /* There is no need to rotate by 0 */
            case NORTH -> {}
            // 270
            case EAST -> MATRIX_STACK.get().rotateY(4.71238898038469f);
        }
    }

    @Override
    public void render(TileMultiblockMachineController te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        this.render(te, x, y, z, partialTicks, destroyStage);
    }

    @Optional.Method(modid = "geckolib3")
    public void render(TileMultiblockMachineController tile, double x, double y, double z, float partialTicks, int destroyStage) {
        MachineControllerModel modelProvider = tile.getCurrentModel();
        if (modelProvider == null) {
            return;
        }

//        int light = tile.getWorld().getCombinedLight(tile.getPos(), 0);
//        int lx = light % 65536;
//        int ly = light / 65536;
//        TODO: shall we use world light?
//        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lx, ly);
//        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
//
//        Minecraft.getMinecraft().renderEngine.bindTexture(modelProvider.getTextureLocation());
        render(modelProvider, tile, x, y, z, partialTicks);
    }

    @Optional.Method(modid = "geckolib3")
    public void render(final MachineControllerModel modelProvider,
                       final TileMultiblockMachineController tile,
                       double x, double y, double z,
                       final float partialTicks)
    {
//        GlStateManager.disableCull();
//        GlStateManager.enableRescaleNormal();
//        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//        GlStateManager.alphaFunc(516, 0.1F);
//        GlStateManager.enableBlend();
//        GlStateManager.depthMask(true);
//        GlStateManager.shadeModel(7425);
//
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);

        renderWithBuffer(tile);

//        GlStateManager.popMatrix();
//        GlStateManager.resetColor();
//        GlStateManager.disableRescaleNormal();
//        GlStateManager.enableCull();
    }

    @Optional.Method(modid = "geckolib3")
    private void renderWithBuffer(final TileMultiblockMachineController animatable) {
        GeoModelRenderTask task = getTask(animatable);

        if (Mods.GREGTECHCEU.isPresent() || Mods.LUMENIZED.isPresent()) {
            task.renderDefault();
        } else {
            task.renderAll();
        }
    }

//    @Optional.Method(modid = "geckolib3")
//    private void renderWithDefault(final MachineControllerModel modelProvider,
//                                   final TileMultiblockMachineController ctrl)
//    {
//        GeoModel model = modelProvider.getModel();
//        modelProvider.setLivingAnimations(ctrl, ctrl.hashCode());
//        
//        BufferBuilder builder = Tessellator.getInstance().getBuffer();
//        builder.begin(GL11.GL_QUADS, VERTEX_FORMAT);
//
//        // Render all top level bones
//        for (GeoBone group : model.topLevelBones) {
//            renderRecursively(builder, group, 1F, 1F, 1F, 1F);
//        }
//
//        Tessellator.getInstance().draw();
//    }

    @Optional.Method(modid = "geckolib3")
    public void renderAsync(TileMultiblockMachineController tile,
                            BufferProvider bufferProvider)
    {
        MachineControllerModel modelProvider = tile.getCurrentModel();
        if (modelProvider == null) {
            return;
        }
        MachineControllerModel renderInst = modelProvider.getRenderInstance();
        GeoModel model = renderInst.getModel();
        synchronized (model) {
            renderInst.setLivingAnimations(tile, tile.hashCode());
            bufferProvider.begin();

            MatrixStack matrixStack = MATRIX_STACK.get();
            BlockPos pos = tile.getPos();
            matrixStack.push();
            matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
            matrixStack.translate(0, 0.01f, 0);
            matrixStack.translate(0.5f, 0, 0.5f);
            rotateBlockMatrix(tile.getControllerRotation());

            // Render all top level bones
            for (GeoBone group : model.topLevelBones) {
                renderRecursively(bufferProvider, group, 1F, 1F, 1F, 1F, false, false);
            }

            matrixStack.pop();

            bufferProvider.finishDrawing();
            renderInst.returnRenderInst();
        }
    }

//    @Optional.Method(modid = "geckolib3")
//    public void renderRecursively(BufferBuilder buffer, GeoBone bone,
//                                  float red, float green, float blue, float alpha)
//    {
//        boolean emissive = bone.name.equals("emissive");
//        float lastBrightnessX = 0;
//        float lastBrightnessY = 0;
//        if (emissive) {
//            Tessellator.getInstance().draw();
//            lastBrightnessX = OpenGlHelper.lastBrightnessX;
//            lastBrightnessY = OpenGlHelper.lastBrightnessY;
//            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
//            buffer.begin(GL11.GL_QUADS, VERTEX_FORMAT);
//        }
//
//        MatrixStack matrixStack = MATRIX_STACK.get();
//        matrixStack.push();
//
//        matrixStack.translate(bone);
//        matrixStack.moveToPivot(bone);
//        matrixStack.rotate(bone);
//        matrixStack.scale(bone);
//        matrixStack.moveBackFromPivot(bone);
//
//        if (!bone.isHidden()) {
//            for (GeoCube cube : bone.childCubes) {
//                matrixStack.push();
//                renderCube(buffer, cube, red, green, blue, alpha);
//                matrixStack.pop();
//            }
//        }
//        if (!bone.childBonesAreHiddenToo()) {
//            for (GeoBone childBone : bone.childBones) {
//                renderRecursively(buffer, childBone, red, green, blue, alpha);
//            }
//        }
//
//        matrixStack.pop();
//        if (emissive) {
//            Tessellator.getInstance().draw();
//            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
//            buffer.begin(GL11.GL_QUADS, VERTEX_FORMAT);
//        }
//    }

    @Optional.Method(modid = "geckolib3")
    public void renderRecursively(BufferProvider bufferProvider,
                                  GeoBone bone,
                                  float red, float green, float blue, float alpha,
                                  boolean bloom, boolean transparent)
    {
        bloom |= bone.name.startsWith("emissive") || bone.name.startsWith("bloom");
        transparent |= bone.name.startsWith("transparent") || bone.name.startsWith("emissive_transparent") || bone.name.startsWith("bloom_transparent");

        MatrixStack matrixStack = MATRIX_STACK.get();
        matrixStack.push();

        matrixStack.translate(bone);
        matrixStack.moveToPivot(bone);
        matrixStack.rotate(bone);
        matrixStack.scale(bone);
        matrixStack.moveBackFromPivot(bone);

        if (!bone.isHidden()) {
            for (GeoCube cube : bone.childCubes) {
                matrixStack.push();
                renderCube(bufferProvider.getBuffer(bloom, transparent), cube, red, green, blue, alpha);
                matrixStack.pop();
            }
        }
        if (!bone.childBonesAreHiddenToo()) {
            for (GeoBone childBone : bone.childBones) {
                renderRecursively(bufferProvider, childBone, red, green, blue, alpha, bloom, transparent);
            }
        }

        matrixStack.pop();
    }

    @Optional.Method(modid = "geckolib3")
    public void renderCube(final BufferBuilder builder,
                           final GeoCube cube,
                           final float red, final float green, final float blue, final float alpha)
    {
        MatrixStack matrixStack = MATRIX_STACK.get();
        matrixStack.moveToPivot(cube);
        matrixStack.rotate(cube);
        matrixStack.moveBackFromPivot(cube);

        for (GeoQuad quad : cube.quads) {
            if (quad == null) {
                continue;
            }

            Vector3f normal = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());

            matrixStack.getNormalMatrix().transform(normal);

            if ((cube.size.y == 0 || cube.size.z == 0) && normal.getX() < 0) {
                normal.x *= -1;
            }
            if ((cube.size.x == 0 || cube.size.z == 0) && normal.getY() < 0) {
                normal.y *= -1;
            }
            if ((cube.size.x == 0 || cube.size.y == 0) && normal.getZ() < 0) {
                normal.z *= -1;
            }

            for (GeoVertex vertex : quad.vertices) {
                Vector4f vector4f = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(),
                        1.0F);

                matrixStack.getModelMatrix().transform(vector4f);

                builder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ())
                        .tex(vertex.textureU, vertex.textureV)
                        .color(red, green, blue, alpha)
                        .normal(normal.getX(), normal.getY(), normal.getZ())
                        .endVertex();
            }
        }
    }

    @Nonnull
    public GeoModelRenderTask getTask(final TileMultiblockMachineController animatable) {
        GeoModelRenderTask task = tasks.get(animatable);
        if (task == null) {
            task = new GeoModelRenderTask(this, animatable);
            tasks.put(animatable, (GeoModelRenderTask) TaskExecutor.FORK_JOIN_POOL.submit(task));
        }
        if (!task.isDone()) {
            long current = System.currentTimeMillis();
            task.join();
            long after = System.currentTimeMillis();
            if (after - current > 50) {
                ModularMachinery.log.warn("[MMCE-AsyncRender] GeoModelRenderTask took too long to complete! ({}ms)", after - current);
            }
        }
        return task;
    }

    @Override
    public boolean isGlobalRenderer(final TileMultiblockMachineController te) {
        return true;
    }

}
