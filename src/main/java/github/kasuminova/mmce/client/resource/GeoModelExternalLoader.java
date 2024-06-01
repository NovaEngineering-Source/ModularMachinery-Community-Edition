package github.kasuminova.mmce.client.resource;

import com.google.common.base.Preconditions;
import github.kasuminova.mmce.client.model.DynamicMachineModelRegistry;
import github.kasuminova.mmce.client.model.MachineControllerModel;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.file.AnimationFileLoader;
import software.bernie.geckolib3.file.GeoModelLoader;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class GeoModelExternalLoader implements ISelectiveResourceReloadListener {
    public static final GeoModelExternalLoader INSTANCE = new GeoModelExternalLoader();

    private final GeoModelLoader geoModelLoader = new GeoModelLoader();
    private final AnimationFileLoader animationFileLoader = new AnimationFileLoader();
    private final MolangParser molangParser = new MolangParser();

    private volatile Map<ResourceLocation, GeoModel> geoModels = new ConcurrentHashMap<>();
    private volatile Map<ResourceLocation, AnimationFile> animations = new ConcurrentHashMap<>();

    private volatile IResourceManager modelSource = null;

    private GeoModelExternalLoader() {
    }

    public void loadAllModelAndAnimations(final IResourceManager resourceManager) {
        modelSource = resourceManager;

        Map<ResourceLocation, GeoModel> geoModels = new ConcurrentHashMap<>();
        Map<ResourceLocation, AnimationFile> animations = new ConcurrentHashMap<>();

        Collection<MachineControllerModel> models = DynamicMachineModelRegistry.INSTANCE.getAllModels();

        models.parallelStream().forEach(model -> {
            ResourceLocation animationFileLocation = model.getAnimationFileLocation();
            try {
                resourceManager.getResource(animationFileLocation);

                AnimationFile animationFile = animationFileLoader.loadAllAnimations(molangParser, animationFileLocation, resourceManager);
                if (animationFile != null) {
                    animations.put(animationFileLocation, animationFile);
                    ModularMachinery.log.debug("[MM-GeoModelExternalLoader] Loaded animation file: {}", animationFileLocation);
                }
            } catch (Exception e) {
                ModularMachinery.log.warn("[MM-GeoModelExternalLoader] Failed to load animation file: {}", animationFileLocation, e);
            }
            ResourceLocation modelLocation = model.getModelLocation();
            try {
                resourceManager.getResource(modelLocation);

                GeoModel geoModel = geoModelLoader.loadModel(resourceManager, modelLocation);
                if (geoModel != null) {
                    geoModels.put(modelLocation, geoModel);
                    ModularMachinery.log.debug("[MM-GeoModelExternalLoader] Loaded model file: {}", modelLocation);
                }
            } catch (Exception e) {
                ModularMachinery.log.warn("[MM-GeoModelExternalLoader] Failed to load model file: {}", modelLocation, e);
            }
        });

        Map<ResourceLocation, GeoModel> oldGeoModels = this.geoModels;
        Map<ResourceLocation, AnimationFile> oldAnimations = this.animations;
        synchronized (this) {
            this.animations = animations;
            this.geoModels = geoModels;
        }
        oldGeoModels.clear();
        oldAnimations.clear();

        ModularMachinery.log.info("[MM-GeoModelExternalLoader] Loaded {} animation files.", animations.size());
        ModularMachinery.log.info("[MM-GeoModelExternalLoader] Loaded {} model files.", geoModels.size());
    }

    public synchronized GeoModel getModel(ResourceLocation location) {
        GeoModel geoModel = geoModels.get(location);
        return Preconditions.checkNotNull(geoModel, "Model file not found: " + location.toString());
    }

    public synchronized GeoModel load(ResourceLocation location) {
        GeoModel model = geoModels.get(location);
        if (model == null) {
            throw new NullPointerException("Model file not found: " + location.toString());
        }
        try {
            return geoModelLoader.loadModel(modelSource, location);
        } catch (Throwable e) {
            ModularMachinery.log.warn("[MM-GeoModelExternalLoader] Failed to load model file: {}", location, e);
        }
        return null;
    }

    public synchronized AnimationFile getAnimation(ResourceLocation location) {
        AnimationFile geoModel = animations.get(location);
        return Preconditions.checkNotNull(geoModel, "Animation file not found: " + location.toString());
    }

    public void onReload() {
        loadAllModelAndAnimations(Minecraft.getMinecraft().getResourceManager());
    }

    @Override
    public void onResourceManagerReload(@Nonnull final IResourceManager resourceManager, final Predicate<IResourceType> resourcePredicate) {
        if (resourcePredicate.test(VanillaResourceType.MODELS)) {
            loadAllModelAndAnimations(resourceManager);
        }
    }
}
