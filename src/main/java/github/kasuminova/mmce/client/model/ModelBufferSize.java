package github.kasuminova.mmce.client.model;

import github.kasuminova.mmce.client.renderer.MachineControllerRenderer;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public class ModelBufferSize {

    private static final int BYTES_PER_CUBE = MachineControllerRenderer.VERTEX_FORMAT.getSize() * (6 * 4);

    private int bufferSize = BYTES_PER_CUBE; // preventing last grow
    private int bloomBufferSize = BYTES_PER_CUBE; // preventing last grow
    private int transparentBufferSize = BYTES_PER_CUBE; // preventing last grow
    private int bloomTransparentBufferSize = BYTES_PER_CUBE; // preventing last grow

    private int staticBufferSize = BYTES_PER_CUBE; // preventing last grow
    private int staticBloomBufferSize = BYTES_PER_CUBE; // preventing last grow
    private int staticTransparentBufferSize = BYTES_PER_CUBE; // preventing last grow
    private int staticBloomTransparentBufferSize = BYTES_PER_CUBE; // preventing last grow

    private final GeoModel model;
    private final StaticModelBones staticModelBones;

    public static ModelBufferSize calculate(final GeoModel model, final StaticModelBones staticModelBones) {
        return new ModelBufferSize(model, staticModelBones);
    }

    private ModelBufferSize(final GeoModel model, final StaticModelBones staticModelBones) {
        this.model = model;
        this.staticModelBones = staticModelBones;
        calculate();
    }

    private void calculate() {
        for (final GeoBone bone : model.topLevelBones) {
            calculateRecursive(bone, false, false);
        }
    }

    public void calculateRecursive(final GeoBone bone, boolean bloom, boolean transparent) {
        boolean isStatic = staticModelBones.isStaticBone(bone.name);
        
        if ((bloom && transparent) || (isBloom(bone) && isTransparent(bone))) {
            bloom = true;
            transparent = true;
            if (isStatic) {
                staticBloomTransparentBufferSize += (BYTES_PER_CUBE * bone.childCubes.size());
            } else {
                bloomTransparentBufferSize += (BYTES_PER_CUBE * bone.childCubes.size());
            }
        } else if (bloom || isBloom(bone)) {
            bloom = true;
            if (isStatic) {
                staticBloomBufferSize += (BYTES_PER_CUBE * bone.childCubes.size());
            } else {
                bloomBufferSize += (BYTES_PER_CUBE * bone.childCubes.size());
            }
        } else if (transparent || isTransparent(bone)) {
            transparent = true;
            if (isStatic) {
                staticTransparentBufferSize += (BYTES_PER_CUBE * bone.childCubes.size());
            } else {
                transparentBufferSize += (BYTES_PER_CUBE * bone.childCubes.size());
            }
        } else {
            if (isStatic) {
                staticBufferSize += (BYTES_PER_CUBE * bone.childCubes.size());
            } else {
                bufferSize += (BYTES_PER_CUBE * bone.childCubes.size());
            }
        }

        if (!bone.childBones.isEmpty()) {
            for (final GeoBone child : bone.childBones) {
                calculateRecursive(child, bloom, transparent);
            }
        }
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getBloomBufferSize() {
        return bloomBufferSize;
    }

    public int getTransparentBufferSize() {
        return transparentBufferSize;
    }

    public int getBloomTransparentBufferSize() {
        return bloomTransparentBufferSize;
    }

    public int getStaticBufferSize() {
        return staticBufferSize;
    }

    public int getStaticBloomBufferSize() {
        return staticBloomBufferSize;
    }

    public int getStaticTransparentBufferSize() {
        return staticTransparentBufferSize;
    }

    public int getStaticBloomTransparentBufferSize() {
        return staticBloomTransparentBufferSize;
    }

    private static boolean isBloom(final GeoBone bone) {
        return bone.name.startsWith("emissive") || bone.name.startsWith("bloom");
    }

    private static boolean isTransparent(final GeoBone bone) {
        return bone.name.startsWith("transparent") || bone.name.startsWith("emissive_transparent") || bone.name.startsWith("bloom_transparent");
    }
}
