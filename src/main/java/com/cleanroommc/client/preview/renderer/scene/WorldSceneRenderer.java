package com.cleanroommc.client.preview.renderer.scene;

import com.cleanroommc.client.util.*;
import com.cleanroommc.client.util.world.LRDummyWorld;
import github.kasuminova.mmce.client.util.BufferBuilderPool;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.cleanroommc.client.util.Vector3.X;

/**
 * Abstract class, and extend a lot of features compared with the original one.
 *
 * @Author: KilaBash
 * @Date: 2021/08/23
 */
@SuppressWarnings("ALL")
@SideOnly(Side.CLIENT)
public abstract class WorldSceneRenderer {
    protected static final FloatBuffer MODELVIEW_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final FloatBuffer PROJECTION_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final IntBuffer VIEWPORT_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
    protected static final FloatBuffer PIXEL_DEPTH_BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final FloatBuffer OBJECT_POS_BUFFER = ByteBuffer.allocateDirect(3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

    protected static final AtomicInteger THREAD_ID = new AtomicInteger(0);

    protected static final Object2IntMap<BlockRenderLayer> LAYER_PROGRESS_UNITS = new Object2IntOpenHashMap<>();
    protected static final int TOTAL_PROGRESS_UNIT;

    protected volatile Map<BlockRenderLayer, BufferBuilder> layerBufferBuilders = new EnumMap<>(BlockRenderLayer.class);

    static {
        int totalProgressUnit = 0;
        LAYER_PROGRESS_UNITS.defaultReturnValue(1);
        for (final BlockRenderLayer layer : BlockRenderLayer.values()) {
            int progressUnit = 1;
            switch (layer) {
                case SOLID -> progressUnit = 4;
                case CUTOUT_MIPPED -> progressUnit = 1;
                case CUTOUT -> progressUnit = 3;
                case TRANSLUCENT -> progressUnit = 2;
            }
            LAYER_PROGRESS_UNITS.put(layer, progressUnit);
            totalProgressUnit += progressUnit;
        }
        TOTAL_PROGRESS_UNIT = totalProgressUnit;
    }

    enum CacheState {
        UNUSED,
        NEED,
        COMPILING,
        COMPILED
    }

    private final LRDummyWorld dummyWorld;
    private final LRMap<Collection<BlockPos>, ISceneRenderHook> renderedBlocksMap;
    private final LRVertexBuffer vertexBuffers = new LRVertexBuffer();

    protected Set<BlockPos> tileEntities = new HashSet<>();
    protected boolean useCache;
    protected AtomicReference<CacheState> cacheState;
    protected int maxProgress;
    protected final AtomicInteger progress = new AtomicInteger();
    protected Thread thread;
    protected EntityCamera viewEntity;

    private Consumer<WorldSceneRenderer> beforeRender;
    private Consumer<WorldSceneRenderer> afterRender;
    private Consumer<RayTraceResult> onLookingAt;
    protected int clearColor;
    private RayTraceResult lastTraceResult;
    private Vector3f eyePos = new Vector3f(0, 0, 10f);
    private Vector3f lookAt = new Vector3f(0, 0, 0);
    private Vector3f worldUp = new Vector3f(0, 1, 0);

    public WorldSceneRenderer(LRDummyWorld world) {
        this.dummyWorld = world;
        renderedBlocksMap = new LRMap<>(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
        cacheState = new AtomicReference<>(CacheState.UNUSED);
    }

    public WorldSceneRenderer useCacheBuffer(boolean useCache) {
        if (this.useCache == useCache || !Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            return this;
        }
        deleteCacheBuffer();
        if (useCache && OpenGlHelper.useVbo()) {
            setVertexBuffers(new VertexBuffer[BlockRenderLayer.values().length]);
            stopCompileCache();
            this.useCache = true;
        } else {
            this.useCache = false;
        }
        cacheState.set(this.useCache ? CacheState.NEED : CacheState.UNUSED);
        return this;
    }

    public WorldSceneRenderer deleteCacheBuffer() {
        if (useCache) {
            Thread thread = this.thread;
            stopCompileCache();

            VertexBuffer[] bufferRef = this.vertexBuffers.getBuffer();
            VertexBuffer[] anotherBufferRef = this.vertexBuffers.getAnotherBuffer();
            this.vertexBuffers.setLeft(null);
            this.vertexBuffers.setRight(null);

            Map<BlockRenderLayer, BufferBuilder> layerBufferBuilders = this.layerBufferBuilders;
            this.layerBufferBuilders = new EnumMap<>(BlockRenderLayer.class);

            CompletableFuture.runAsync(() -> {
                if (thread != null && thread.isAlive()) {
                    try {
                        thread.join();
                    } catch (InterruptedException ignored) {
                    }
                }
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    for (int i = 0; i < BlockRenderLayer.values().length; ++i) {
                        if (bufferRef[i] != null) {
                            bufferRef[i].deleteGlBuffers();
                        }
                        if (anotherBufferRef[i] != null) {
                            anotherBufferRef[i].deleteGlBuffers();
                        }
                    }
                });
                layerBufferBuilders.values().stream()
                        .filter(buffer -> buffer != null)
                        .forEach(BufferBuilderPool::returnBuffer);
                layerBufferBuilders.clear();
            });
        }
        tileEntities.clear();
        useCache = false;
        cacheState.set(CacheState.UNUSED);
        return this;
    }

    public WorldSceneRenderer needCompileCache() {
        if (useCache) {
            stopCompileCache();
            cacheState.set(CacheState.NEED);
        } else {
            switchLRRenderer();
        }
        return this;
    }

    public void stopCompileCache() {
        if (cacheState.get() == CacheState.COMPILING) {
            cacheState.set(CacheState.UNUSED);
            thread.interrupt();
        }
    }

    public WorldSceneRenderer setBeforeWorldRender(Consumer<WorldSceneRenderer> callback) {
        this.beforeRender = callback;
        return this;
    }

    public WorldSceneRenderer setAfterWorldRender(Consumer<WorldSceneRenderer> callback) {
        this.afterRender = callback;
        return this;
    }

    public WorldSceneRenderer addRenderedBlocks(Collection<BlockPos> blocks, ISceneRenderHook renderHook) {
        if (blocks != null) {
            this.renderedBlocksMap.getMap().put(blocks, renderHook);
        }
        return this;
    }

    public WorldSceneRenderer addRenderedBlocksToAnotherWorld(Collection<BlockPos> blocks, ISceneRenderHook renderHook) {
        if (blocks != null) {
            this.renderedBlocksMap.getAnotherMap().put(blocks, renderHook);
        }
        return this;
    }

    public WorldSceneRenderer setOnLookingAt(Consumer<RayTraceResult> onLookingAt) {
        this.onLookingAt = onLookingAt;
        return this;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setClearColor(int clearColor) {
        this.clearColor = clearColor;
    }

    public RayTraceResult getLastTraceResult() {
        return lastTraceResult;
    }

    public void render(float x, float y, float width, float height, int mouseX, int mouseY, boolean traceBlock) {
        // setupCamera
        PositionedRect positionedRect = getPositionedRect((int) x, (int) y, (int) width, (int) height);
        PositionedRect mouse = getPositionedRect(mouseX, mouseY, 0, 0);
        mouseX = mouse.position.x;
        mouseY = mouse.position.y;
        setupCamera(positionedRect);
        // render TrackedDummyWorld
        drawWorld();
        // check lookingAt
        if (traceBlock && isMouseOver(positionedRect, mouseX, mouseY)) {
            Vector3f hitPos = unProject(mouseX, mouseY);
            RayTraceResult result = lastTraceResult = rayTrace(hitPos);
            if (result != null) {
                if (onLookingAt != null) {
                    onLookingAt.accept(result);
                }
            }
        }
        // resetCamera
        resetCamera();
    }

    protected static boolean isMouseOver(final PositionedRect positionedRect, final int mouseX, final int mouseY) {
        return mouseX > positionedRect.position.x && mouseX < positionedRect.position.x + positionedRect.size.width
                && mouseY > positionedRect.position.y && mouseY < positionedRect.position.y + positionedRect.size.height;
    }

    public Vector3f getEyePos() {
        return eyePos;
    }

    public Vector3f getLookAt() {
        return lookAt;
    }

    public Vector3f getWorldUp() {
        return worldUp;
    }

    public void setCameraLookAt(Vector3 eyePos, Vector3f lookAt, Vector3f worldUp) {
        this.eyePos = eyePos.vector3f();
        this.lookAt = lookAt;
        this.worldUp = worldUp;
        if (viewEntity != null) {
            Vector3 xzProduct = new Vector3(lookAt.x - eyePos.x, 0, lookAt.z - eyePos.z);
            double angleYaw = Math.toDegrees(xzProduct.angle(Vector3.Z));
            if (xzProduct.angle(X) < Math.PI / 2) {
                angleYaw = -angleYaw;
            }
            double anglePitch = Math.toDegrees(new Vector3(lookAt).subtract(new Vector3(eyePos)).angle(Vector3.Y)) - 90;
            viewEntity.setLocationAndAngles(eyePos.x, eyePos.y, eyePos.z, (float) angleYaw, (float) anglePitch);
        }
    }

    public void setCameraLookAt(Vector3f lookAt, double radius, double rotationPitch, double rotationYaw) {
        Vector3 vecX = new Vector3(Math.cos(rotationPitch), 0, Math.sin(rotationPitch));
        Vector3 vecY = new Vector3(0, Math.tan(rotationYaw) * vecX.mag(), 0);
        Vector3 pos = vecX.copy().add(vecY).normalize().multiply(radius);
        setCameraLookAt(pos.add(lookAt.x, lookAt.y, lookAt.z), lookAt, worldUp);
    }

    protected PositionedRect getPositionedRect(int x, int y, int width, int height) {
        return new PositionedRect(new Position(x, y), new Size(width, height));
    }

    protected void setupCamera(PositionedRect positionedRect) {
        int x = positionedRect.getPosition().x;
        int y = positionedRect.getPosition().y;
        int width = positionedRect.getSize().width;
        int height = positionedRect.getSize().height;

        GlStateManager.pushAttrib();

        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        //setup viewport and clear GL buffers
        GlStateManager.viewport(x, y, width, height);

        clearView(x, y, width, height);

        //setup projection matrix to perspective
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        float aspectRatio = width / (height * 1.0f);
        GLU.gluPerspective(60.0f, aspectRatio, 0.1f, 10000.0f);

        //setup modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GLU.gluLookAt(eyePos.x, eyePos.y, eyePos.z, lookAt.x, lookAt.y, lookAt.z, worldUp.x, worldUp.y, worldUp.z);
    }

    protected void clearView(int x, int y, int width, int height) {
        int i = (clearColor & 0xFF0000) >> 16;
        int j = (clearColor & 0xFF00) >> 8;
        int k = (clearColor & 0xFF);
        GlStateManager.clearColor(i / 255.0f, j / 255.0f, k / 255.0f, (clearColor >> 24) / 255.0f);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    protected void resetCamera() {
        //reset viewport
        Minecraft minecraft = Minecraft.getMinecraft();
        GlStateManager.viewport(0, 0, minecraft.displayWidth, minecraft.displayHeight);

        //reset projection matrix
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();

        //reset modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();

        GlStateManager.enableBlend();
        GlStateManager.disableDepth();

        //reset attributes
        GlStateManager.popAttrib();
    }

    protected void drawWorld() {
        if (beforeRender != null) {
            beforeRender.accept(this);
        }

        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableCull();
        GlStateManager.enableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();

        boolean checkDisabledModel = getWorld() == mc.world || (getWorld() instanceof TrackedDummyWorld && ((TrackedDummyWorld) getWorld()).proxyWorld == mc.world);
        float particleTicks = mc.getRenderPartialTicks();
        if (useCache) {
            renderCacheBuffer(mc, oldRenderLayer, particleTicks, checkDisabledModel);
        } else {
            renderDefault(mc, particleTicks, checkDisabledModel, oldRenderLayer);
        }

        GlStateManager.shadeModel(7425);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);

        if (afterRender != null) {
            afterRender.accept(this);
        }
    }

    public boolean isCompiling() {
        return cacheState.get() == CacheState.COMPILING;
    }

    public boolean isCompilerThreadAlive() {
        if (thread != null) {
            if (thread.isAlive()) {
                return true;
            }
            thread = null;
        }
        return false;
    }

    public float getCompileProgress() {
        // 2000 blocks, 11 is per block unit.
        if (maxProgress <= 2000 * TOTAL_PROGRESS_UNIT) {
            return -1;
        }
        return (float) progress.get() / maxProgress;
    }

    public World getWorld() {
        return dummyWorld.getWorld();
    }

    public LRDummyWorld getLRDummyWorld() {
        return dummyWorld;
    }

    public VertexBuffer[] getVertexBuffers() {
        return vertexBuffers.getBuffer();
    }

    protected BufferBuilder getLayerBufferBuilder(final BlockRenderLayer layer) {
        BufferBuilder builder = layerBufferBuilders.get(layer);
        if (builder != null) {
            return builder;
        }
        synchronized (layerBufferBuilders) {
            builder = layerBufferBuilders.get(layer);
            if (builder == null) {
                layerBufferBuilders.put(layer, builder = BufferBuilderPool.borrowBuffer(256 * 1024));
            }
        }
        return builder;
    }

    public WorldSceneRenderer setVertexBuffers(VertexBuffer[] vertexBuffers) {
        this.vertexBuffers.setBuffer(vertexBuffers).setAnotherBuffer(vertexBuffers);
        for (int j = 0; j < BlockRenderLayer.values().length; ++j) {
            this.vertexBuffers.getBuffer()[j] = new VertexBuffer(DefaultVertexFormats.BLOCK);
            this.vertexBuffers.getAnotherBuffer()[j] = new VertexBuffer(DefaultVertexFormats.BLOCK);
        }
        return this;
    }

    public void refreshCache() {
        if (isCompiling() || isCompilerThreadAlive()) {
            return;
        }
        cacheState.set(CacheState.COMPILING);
        progress.set(0);
        maxProgress = renderedBlocksMap.getAnotherMap().keySet().stream()
                .map(Collection::size)
                .reduce(0, Integer::sum) * 11;

        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();
        Minecraft mc = Minecraft.getMinecraft();
        boolean checkDisabledModel = getWorld() == mc.world || (getWorld() instanceof TrackedDummyWorld && ((TrackedDummyWorld) getWorld()).proxyWorld == mc.world);

        thread = new Thread(() -> compileCache(mc, oldRenderLayer, checkDisabledModel));
        thread.setName("MMCE-PreviewCompiler-" + THREAD_ID.getAndIncrement());
        thread.start();
    }

    protected void renderDefault(final Minecraft mc, final float particleTicks, final boolean checkDisabledModel, final BlockRenderLayer oldRenderLayer) {
        BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
        try { // render block in each layer
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
                ForgeHooksClient.setRenderLayer(layer);
                if (pass == 1) {
                    renderTESR(0, particleTicks, checkDisabledModel);
                }
                renderedBlocksMap.getMap().forEach((renderedBlocks, hook) -> {
                    if (hook != null) {
                        hook.apply(false, pass, layer);
                    } else {
                        setDefaultPassRenderState(pass);
                    }
                    BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                    renderBlocks(checkDisabledModel, blockrendererdispatcher, layer, buffer, renderedBlocks, getWorld(), 0);

                    Tessellator.getInstance().draw();
                    Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
                });
            }
        } finally {
            ForgeHooksClient.setRenderLayer(oldRenderLayer);
        }
        renderTESR(1, particleTicks, checkDisabledModel);
    }

    protected void renderCacheBuffer(Minecraft mc, BlockRenderLayer oldRenderLayer, float particleTicks, boolean checkDisabledModel) {
        if (cacheState.get() == CacheState.NEED) {
            refreshCache();
        }
        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
            int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
            if (pass == 1) {
                renderTESR(0, particleTicks, checkDisabledModel);
            }

            GlStateManager.glEnableClientState(32884);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glEnableClientState(32888);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.glEnableClientState(32888);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glEnableClientState(32886);

            VertexBuffer vbo = getVertexBuffers()[layer.ordinal()];
            setDefaultPassRenderState(pass);
            vbo.bindBuffer();
            this.setupArrayPointers();
            vbo.drawArrays(7);
            OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
            GlStateManager.resetColor();

            for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements()) {
                VertexFormatElement.EnumUsage enumUsage = vertexformatelement.getUsage();
                int k1 = vertexformatelement.getIndex();

                switch (enumUsage) {
                    case POSITION:
                        GlStateManager.glDisableClientState(32884);
                        break;
                    case UV:
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + k1);
                        GlStateManager.glDisableClientState(32888);
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                        break;
                    case COLOR:
                        GlStateManager.glDisableClientState(32886);
                        GlStateManager.resetColor();
                }
            }
        }
        renderTESR(1, particleTicks, checkDisabledModel);
    }

    private void compileCache(final Minecraft mc, final BlockRenderLayer oldRenderLayer, final boolean checkDisabledModel) {
        Thread compilerThread = Thread.currentThread();
        BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
        Map<Collection<BlockPos>, ISceneRenderHook> renderedBlocksMap = this.renderedBlocksMap.getAnotherMap();
        for (final BlockRenderLayer layer : BlockRenderLayer.values()) {
            int progressUnit = LAYER_PROGRESS_UNITS.getInt(layer);
            ForgeHooksClient.setRenderLayer(layer);
            BufferBuilder buffer = getLayerBufferBuilder(layer);
            synchronized (buffer) {
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                for (final Collection<BlockPos> renderedBlocks : renderedBlocksMap.keySet()) {
                    renderBlocks(checkDisabledModel, blockrendererdispatcher, layer, buffer, renderedBlocks, getLRDummyWorld().getAnotherWorld(), progressUnit);
                }
                buffer.finishDrawing();
                buffer.reset();
            }
            ForgeHooksClient.setRenderLayer(null);
            if (!isCompiling()) {
                return;
            }
            mc.addScheduledTask(() -> {
                if (!useCache) {
                    return;
                }
                ByteBuffer buf = buffer.getByteBuffer();
                vertexBuffers.getAnotherBuffer()[layer.ordinal()].bufferData(buf);
            });
        }
        if (!isCompiling()) {
            return;
        }
        Set<BlockPos> poses = new HashSet<>();
        renderedBlocksMap.forEach((renderedBlocks, hook) -> {
            for (BlockPos pos : renderedBlocks) {
                progress.getAndIncrement();
                if (!isCompiling()) {
                    return;
                }
//                        if (checkDisabledModel && MultiblockWorldSavedData.modelDisabled.contains(pos)) {
//                            continue;
//                        }
                TileEntity tile = getLRDummyWorld().getAnotherWorld().getTileEntity(pos);
                if (tile != null && TileEntityRendererDispatcher.instance.getRenderer(tile) != null) {
                    poses.add(pos);
                }
            }
        });
        if (!isCompiling()) {
            return;
        }

        tileEntities = poses;
        cacheState.set(CacheState.COMPILED);
        maxProgress = -1;

        switchLRRenderer();

        thread = null;
    }

    public void switchLRRenderer() {
        dummyWorld.setUseLeft(!dummyWorld.isUseLeft());
        vertexBuffers.setUseLeft(!vertexBuffers.isUseLeft());
        renderedBlocksMap.setUseLeft(!renderedBlocksMap.isUseLeft());
        renderedBlocksMap.getAnotherMap().clear();
    }

    private void renderBlocks(final boolean checkDisabledModel,
                              final BlockRendererDispatcher rendererDispatcher,
                              final BlockRenderLayer layer,
                              final BufferBuilder buffer,
                              final Collection<BlockPos> renderedBlocks,
                              final World world,
                              final int progressUnit) {
        for (BlockPos pos : renderedBlocks) {
            if (maxProgress > 0) {
                progress.getAndAdd(progressUnit);
            }
//            if (checkDisabledModel && MultiblockWorldSavedData.modelDisabled.contains(pos)) {
//                continue;
//            }
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block == Blocks.AIR) continue;
            state = state.getActualState(world, pos);
            if (block.canRenderInLayer(state, layer)) {
                rendererDispatcher.renderBlock(state, pos, world, buffer);
            }
        }
    }

    private void setupArrayPointers() {
        GlStateManager.glVertexPointer(3, 5126, 28, 0);
        GlStateManager.glColorPointer(4, 5121, 28, 12);
        GlStateManager.glTexCoordPointer(2, 5126, 28, 16);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(2, 5122, 28, 24);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private void renderTESR(final int pass, float particle, boolean checkDisabledModel) {
        // render TESR
        RenderHelper.enableStandardItemLighting();
        ForgeHooksClient.setRenderPass(pass);
        if (!useCache) {
            renderedBlocksMap.getMap().forEach((renderedBlocks, hook) -> {
                if (hook != null) {
                    hook.apply(true, pass, null);
                } else {
                    setDefaultPassRenderState(pass);
                }
                for (BlockPos pos : renderedBlocks) {
//                    if (checkDisabledModel && MultiblockWorldSavedData.modelDisabled.contains(pos)) {
//                        continue;
//                    }
                    TileEntity tile = getWorld().getTileEntity(pos);
                    if (tile != null) {
                        if (tile.shouldRenderInPass(pass)) {
                            TileEntityRendererDispatcher.instance.render(tile, pos.getX(), pos.getY(), pos.getZ(), particle);
                        }
                    }
                }
            });
        } else {
            for (BlockPos pos : tileEntities) {
                TileEntity tile = getWorld().getTileEntity(pos);
                if (tile != null && tile.shouldRenderInPass(pass)) {
                    TileEntityRendererDispatcher.instance.render(tile, pos.getX(), pos.getY(), pos.getZ(), particle);
                }
            }
        }
        ForgeHooksClient.setRenderPass(-1);
        RenderHelper.disableStandardItemLighting();
    }

    public static void setDefaultPassRenderState(int pass) {
        GlStateManager.color(1, 1, 1, 1);
        if (pass == 0) { // SOLID
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.shadeModel(7424);
        } else { // TRANSLUCENT
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.depthMask(false);
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.shadeModel(7425);
        }
    }

    public RayTraceResult rayTrace(Vector3f hitPos) {
        Vec3d startPos = new Vec3d(this.eyePos.x, this.eyePos.y, this.eyePos.z);
        hitPos.scale(2); // Double view range to ensure pos can be seen.
        Vec3d endPos = new Vec3d((hitPos.x - startPos.x), (hitPos.y - startPos.y), (hitPos.z - startPos.z));
        return this.getWorld().rayTraceBlocks(startPos, endPos);
    }

    public Vector3f project(BlockPos pos) {
        //read current rendering parameters
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_MATRIX_BUFFER);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_MATRIX_BUFFER);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        //rewind buffers after write by OpenGL glGet calls
        MODELVIEW_MATRIX_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        VIEWPORT_BUFFER.rewind();

        //call gluProject with retrieved parameters
        GLU.gluProject(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, MODELVIEW_MATRIX_BUFFER, PROJECTION_MATRIX_BUFFER, VIEWPORT_BUFFER, OBJECT_POS_BUFFER);

        //rewind buffers after read by gluProject
        VIEWPORT_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        MODELVIEW_MATRIX_BUFFER.rewind();

        //rewind buffer after write by gluProject
        OBJECT_POS_BUFFER.rewind();

        //obtain position in Screen
        float winX = OBJECT_POS_BUFFER.get();
        float winY = OBJECT_POS_BUFFER.get();
        float winZ = OBJECT_POS_BUFFER.get();

        //rewind buffer after read
        OBJECT_POS_BUFFER.rewind();

        return new Vector3f(winX, winY, winZ);
    }

    public Vector3f unProject(int mouseX, int mouseY) {
        //read depth of pixel under mouse
        GL11.glReadPixels(mouseX, mouseY, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, PIXEL_DEPTH_BUFFER);

        //rewind buffer after write by glReadPixels
        PIXEL_DEPTH_BUFFER.rewind();

        //retrieve depth from buffer (0.0-1.0f)
        float pixelDepth = PIXEL_DEPTH_BUFFER.get();

        //rewind buffer after read
        PIXEL_DEPTH_BUFFER.rewind();

        //read current rendering parameters
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_MATRIX_BUFFER);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_MATRIX_BUFFER);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        //rewind buffers after write by OpenGL glGet calls
        MODELVIEW_MATRIX_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        VIEWPORT_BUFFER.rewind();

        //call gluUnProject with retrieved parameters
        GLU.gluUnProject(mouseX, mouseY, pixelDepth, MODELVIEW_MATRIX_BUFFER, PROJECTION_MATRIX_BUFFER, VIEWPORT_BUFFER, OBJECT_POS_BUFFER);

        //rewind buffers after read by gluUnProject
        VIEWPORT_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        MODELVIEW_MATRIX_BUFFER.rewind();

        //rewind buffer after write by gluUnProject
        OBJECT_POS_BUFFER.rewind();

        //obtain absolute position in world
        float posX = OBJECT_POS_BUFFER.get();
        float posY = OBJECT_POS_BUFFER.get();
        float posZ = OBJECT_POS_BUFFER.get();

        //rewind buffer after read
        OBJECT_POS_BUFFER.rewind();

        return new Vector3f(posX, posY, posZ);
    }

    /***
     * For better performance, You'd better handle the event {@link #setOnLookingAt(Consumer)} or {@link #getLastTraceResult()}
     * @param mouseX xPos in Texture
     * @param mouseY yPos in Texture
     * @return RayTraceResult Hit
     */
    protected RayTraceResult screenPos2BlockPosFace(int mouseX, int mouseY, int x, int y, int width, int height) {
        // render a frame
        GlStateManager.enableDepth();
        setupCamera(getPositionedRect(x, y, width, height));

        drawWorld();

        Vector3f hitPos = unProject(mouseX, mouseY);
        RayTraceResult result = rayTrace(hitPos);

        resetCamera();

        return result;
    }

    /***
     * For better performance, You'd better do project in {@link #setAfterWorldRender(Consumer)}
     * @param pos BlockPos
     * @param depth should pass Depth Test
     * @return x, y, z
     */
    protected Vector3f blockPos2ScreenPos(BlockPos pos, boolean depth, int x, int y, int width, int height) {
        // render a frame
        GlStateManager.enableDepth();
        setupCamera(getPositionedRect(x, y, width, height));

        drawWorld();
        Vector3f winPos = project(pos);

        resetCamera();

        return winPos;
    }

}
