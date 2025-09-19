package github.kasuminova.mmce.client.model;

import github.kasuminova.mmce.client.resource.GeoModelExternalLoader;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class MachineControllerModel extends AnimatedGeoModel<TileMultiblockMachineController> {
    protected final String           modelName;
    protected final ResourceLocation modelLocation;
    protected final ResourceLocation textureLocation;
    protected final ResourceLocation animationFileLocation;

    protected ModelPool pool = null;

    protected GeoModel currentModel = null;

    public MachineControllerModel(final String modelName, final ResourceLocation modelLocation, final ResourceLocation textureLocation, final ResourceLocation animationFileLocation) {
        this.modelName = modelName;
        this.modelLocation = modelLocation;
        this.textureLocation = textureLocation;
        this.animationFileLocation = animationFileLocation;
    }

    /**
     * 获取渲染实例，使用完成后必须调用 {@link #returnRenderInst()} 以归还实例。
     * <br/>
     * Gets the render instance and must call {@link #returnRenderInst()} to return the instance when use is complete.
     */
    public MachineControllerModel getRenderInstance() {
        if (pool.getOriginal() != this) {
            throw new IllegalStateException("Cannot return render instance of a model that is not the render instance!");
        }
        return pool.borrowRenderInst();
    }

    public void returnRenderInst() {
        pool.returnRenderInst(this);
    }

    public void initializePool() {
        pool = new ModelPool(this);
    }

    public StaticModelBones getStaticModelBones() {
        return pool.getStaticModelBones();
    }

    public ModelBufferSize getBufferSize() {
        return pool.getModelBufferSize();
    }

    public MachineControllerModel createRenderInstance() {
        MachineControllerModel model = new MachineControllerModel(modelName, modelLocation, textureLocation, animationFileLocation);
        model.pool = pool;
        return model;
    }

    public GeoModel getModel() {
        return getModel(modelLocation);
    }

    @Override
    public Animation getAnimation(final String name, final IAnimatable animatable) {
        return GeoModelExternalLoader.INSTANCE.getAnimation(animationFileLocation).getAnimation(name);
    }

    @Override
    public GeoModel getModel(final ResourceLocation location) {
        GeoModel model = pool.getModel(this);

        if (model != currentModel) {
            this.getAnimationProcessor().clearModelRendererList();
            for (GeoBone bone : model.topLevelBones) {
                registerBone(bone);
            }
            this.currentModel = model;
        }

        return model;
    }

    public String getModelName() {
        return modelName;
    }

    public ResourceLocation getModelLocation() {
        return modelLocation;
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public ResourceLocation getAnimationFileLocation() {
        return animationFileLocation;
    }

    @Override
    public ResourceLocation getModelLocation(final TileMultiblockMachineController object) {
        return modelLocation;
    }

    @Override
    public ResourceLocation getTextureLocation(final TileMultiblockMachineController object) {
        return textureLocation;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(final TileMultiblockMachineController animatable) {
        return animationFileLocation;
    }
}
