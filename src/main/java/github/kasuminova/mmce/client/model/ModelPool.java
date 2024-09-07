package github.kasuminova.mmce.client.model;

import github.kasuminova.mmce.client.resource.GeoModelExternalLoader;
import github.kasuminova.mmce.common.concurrent.TaskExecutor;
import software.bernie.geckolib3.geo.render.built.GeoModel;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ModelPool {

    // Reference thread pool size.
    public static final int POOL_LIMIT = TaskExecutor.CLIENT_THREAD_COUNT;

    private final MachineControllerModel original;

    private final Map<MachineControllerModel, GeoModel> renderInstMap = new ConcurrentHashMap<>();
    private final Queue<MachineControllerModel> renderInstPool = new ArrayBlockingQueue<>(POOL_LIMIT);
    private StaticModelBones staticModelBones = null;
    private ModelBufferSize modelBufferSize = null;

    public ModelPool(final MachineControllerModel original) {
        this.original = original;
        this.renderInstPool.offer(original);
    }

    public MachineControllerModel getOriginal() {
        return original;
    }

    public StaticModelBones getStaticModelBones() {
        if (staticModelBones == null) {
            staticModelBones = StaticModelBones.compile(original.getModel(), GeoModelExternalLoader.INSTANCE.getAnimation(original.animationFileLocation));
        }
        return staticModelBones;
    }

    public ModelBufferSize getModelBufferSize() {
        if (modelBufferSize == null) {
            modelBufferSize = ModelBufferSize.calculate(original.getModel(), getStaticModelBones());
        }
        return modelBufferSize;
    }

    public GeoModel getModel(final MachineControllerModel model) {
        if (model == original) {
            return GeoModelExternalLoader.INSTANCE.getModel(original.modelLocation);
        }
        return renderInstMap.get(model);
    }

    public synchronized MachineControllerModel borrowRenderInst() {
        if (renderInstPool.isEmpty()) {
            GeoModel loaded = GeoModelExternalLoader.INSTANCE.load(original.modelLocation);
            if (loaded == null) {
                throw new NullPointerException("Model file not found: " + original.modelLocation);
            }
            MachineControllerModel renderInst = original.createRenderInstance();
            renderInstMap.put(renderInst, loaded);
            return renderInst;
        }
        return renderInstPool.poll();
    }

    public synchronized void returnRenderInst(MachineControllerModel model) {
        renderInstPool.offer(model);
    }

    public synchronized void reset() {
        renderInstPool.clear();
        renderInstMap.clear();
    }

}
