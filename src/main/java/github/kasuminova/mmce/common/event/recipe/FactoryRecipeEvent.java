package github.kasuminova.mmce.common.event.recipe;

import crafttweaker.annotations.ZenRegister;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

@ZenRegister
@ZenClass("mods.modularmachinery.FactoryRecipeEvent")
public class FactoryRecipeEvent extends RecipeEvent {
    protected final FactoryRecipeThread recipeThread;

    public FactoryRecipeEvent(FactoryRecipeThread recipeThread, TileMultiblockMachineController controller) {
        super(controller, recipeThread, recipeThread.getContext());
        this.recipeThread = recipeThread;
    }

    @ZenGetter("factoryRecipeThread")
    public FactoryRecipeThread getFactoryRecipeThread() {
        return recipeThread;
    }
}
