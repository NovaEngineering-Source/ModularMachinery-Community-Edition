package github.kasuminova.mmce.client.model;

import com.google.common.collect.ImmutableSet;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;

import java.util.HashSet;
import java.util.Set;

public class StaticModelBones {

    private final GeoModel      model;
    private final AnimationFile animationFile;
    private       Set<String>   staticBones = new HashSet<>();
    ;

    private StaticModelBones(final GeoModel model, final AnimationFile animationFile) {
        this.model = model;
        this.animationFile = animationFile;
        compile();
    }

    public static StaticModelBones compile(final GeoModel model, final AnimationFile animation) {
        return new StaticModelBones(model, animation);
    }

    public void compile() {
        Set<String> animatedBones = animationFile.getAllAnimations().stream()
                                                 .flatMap(animation -> animation.boneAnimations.stream())
                                                 .map(boneAnimation -> boneAnimation.boneName)
                                                 .collect(ImmutableSet.toImmutableSet());

        model.topLevelBones.forEach(bone -> recursiveAdd(bone, animatedBones));
        staticBones = ImmutableSet.copyOf(staticBones);
    }

    private void recursiveAdd(final GeoBone bone, final Set<String> animatedBones) {
        if (animatedBones.contains(bone.name)) {
            return;
        }

        staticBones.add(bone.name);
        bone.childBones.stream()
                       .filter(childBone -> !childBone.childBones.isEmpty())
                       .filter(childBone -> !animatedBones.contains(childBone.name))
                       .forEach(staticBone -> recursiveAdd(staticBone, animatedBones));
    }

    public boolean isStaticBone(final String boneName) {
        return staticBones.contains(boneName);
    }

}
