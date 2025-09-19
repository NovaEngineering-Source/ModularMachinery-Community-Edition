package github.kasuminova.mmce.common.event.recipe;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.event.Phase;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftingStatus;
import hellfirepvp.modularmachinery.common.machine.RecipeThread;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.modularmachinery.RecipeTickEvent")
public class RecipeTickEvent extends RecipeEvent {
    public final  Phase        phase;
    private final RecipeThread recipeThread;
    private       boolean      isFailure          = false;
    private       boolean      destructRecipe     = false;
    private       boolean      preventProgressing = false;
    private       String       failureReason      = null;

    public RecipeTickEvent(TileMultiblockMachineController controller, RecipeThread recipeThread, Phase phase) {
        super(controller, recipeThread, recipeThread.getContext());
        this.phase = phase;
        this.recipeThread = recipeThread;
    }

    @Override
    public void postEvent() {
        super.postEvent();

        if (preventProgressing) {
            ActiveMachineRecipe activeRecipe = recipeThread.getActiveRecipe();
            if (activeRecipe.getTick() > 0) {
                activeRecipe.setTick(activeRecipe.getTick() - 1);
            }
            recipeThread.setStatus(CraftingStatus.working(failureReason));
            return;
        }

        if (isFailure) {
            if (destructRecipe) {
                recipeThread.setActiveRecipe(null)
                            .setContext(null)
                            .setStatus(CraftingStatus.failure(failureReason))
                            .getSemiPermanentModifiers().clear();
                return;
            }
            recipeThread.setStatus(CraftingStatus.failure(failureReason));
        }
    }

    @ZenMethod
    public void preventProgressing(String reason) {
        this.preventProgressing = true;
        this.failureReason = reason;
        setCanceled(true);
    }

    @ZenMethod
    public void setFailed(boolean destructRecipe, String reason) {
        this.isFailure = true;
        this.destructRecipe = destructRecipe;
        this.failureReason = reason;
        setCanceled(true);
    }

    public boolean isFailure() {
        return isFailure;
    }

    public boolean isPreventProgressing() {
        return preventProgressing;
    }

    public boolean isDestructRecipe() {
        return destructRecipe;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
