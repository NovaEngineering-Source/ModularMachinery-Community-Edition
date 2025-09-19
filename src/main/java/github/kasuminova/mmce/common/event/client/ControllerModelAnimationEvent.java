package github.kasuminova.mmce.common.event.client;

import com.github.bsideup.jabel.Desugar;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.client.model.DynamicMachineModelRegistry;
import github.kasuminova.mmce.common.event.machine.MachineEvent;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenSetter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@ZenRegister
@ModOnly("geckolib3")
@ZenClass("mods.modularmachinery.ControllerModelAnimationEvent")
public class ControllerModelAnimationEvent extends MachineEvent {
    private final List<AnimationCT>                               animations = new ArrayList<>();
    private final AnimationEvent<TileMultiblockMachineController> event;
    private final String                                          currentModelName;

    private int playState = 0;

    public ControllerModelAnimationEvent(final TileMultiblockMachineController controller, final AnimationEvent<TileMultiblockMachineController> event) {
        super(controller);
        this.event = event;
        String currentModelName = controller.getCurrentModelName();
        if (currentModelName.isEmpty()) {
            currentModelName = DynamicMachineModelRegistry.INSTANCE.getMachineDefaultModel(controller.getFoundMachine()).getModelName();
        }
        this.currentModelName = currentModelName;
    }

    @ZenMethod
    public void addAnimation(String animationName) {
        animations.add(new AnimationCT(animationName, false));
    }

    @ZenMethod
    public void addAnimation(String animationName, boolean loop) {
        animations.add(new AnimationCT(animationName, loop));
    }

    @ZenMethod
    public void setAnimation(String animationName) {
        animations.clear();
        addAnimation(animationName);
    }

    @ZenMethod
    public void setAnimation(String animationName, boolean loop) {
        animations.clear();
        addAnimation(animationName, loop);
    }

    @ZenGetter("currentModelName")
    public String getCurrentModelName() {
        return currentModelName;
    }

    @ZenGetter("transitionLengthTicks")
    public double getTransitionLengthTicks() {
        return event.getController().transitionLengthTicks;
    }

    @ZenSetter("transitionLengthTicks")
    public void setTransitionLengthTicks(double transitionLengthTicks) {
        event.getController().transitionLengthTicks = transitionLengthTicks;
    }

    @ZenGetter("playState")
    public int getPlayState() {
        return playState;
    }

    @ZenSetter("playState")
    public void setPlayState(final int playState) {
        this.playState = playState;
    }

    @Nullable
    @ZenGetter("currentAnimationName")
    public String getCurrentAnimationName() {
        Animation current = event.getController().getCurrentAnimation();
        return current == null ? null : current.animationName;
    }

    @ZenGetter("animationSpeed")
    public double getAnimationSpeed() {
        return event.getController().getAnimationSpeed();
    }

    @ZenGetter("animationTick")
    public double getAnimationTick() {
        return event.getAnimationTick();
    }

    @ZenGetter("animationState")
    public int getAnimationState() {
        return switch (event.getController().getAnimationState()) {
            case Running -> 0;
            case Transitioning -> 1;
            case Stopped -> 2;
        };
    }

    @ZenGetter("limbSwing")
    public float getLimbSwing() {
        return event.getLimbSwing();
    }

    @ZenGetter("limbSwingAmount")
    public float getLimbSwingAmount() {
        return event.getLimbSwingAmount();
    }

    @ZenGetter("partialTick")
    public float getPartialTick() {
        return event.getPartialTick();
    }

    @ZenGetter("moving")
    public boolean isMoving() {
        return event.isMoving();
    }

    public List<AnimationCT> getAnimations() {
        return animations;
    }

    @Desugar
    public record AnimationCT(String animationName, boolean loop) {
    }
}
