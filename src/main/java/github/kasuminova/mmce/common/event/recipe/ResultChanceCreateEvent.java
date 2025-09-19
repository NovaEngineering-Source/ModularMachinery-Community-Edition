package github.kasuminova.mmce.common.event.recipe;

import github.kasuminova.mmce.common.event.Phase;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.ResultChance;

public class ResultChanceCreateEvent extends RecipeEvent {
    public final Phase        phase;
    private      ResultChance resultChance;

    public ResultChanceCreateEvent(final TileMultiblockMachineController controller,
                                   final RecipeCraftingContext context,
                                   final ResultChance currentChance,
                                   final Phase phase) {
        super(controller, null, context);

        this.resultChance = currentChance;
        this.phase = phase;
    }

    public ResultChance getResultChance() {
        return resultChance;
    }

    public ResultChanceCreateEvent setResultChance(final ResultChance resultChance) {
        this.resultChance = resultChance;
        return this;
    }
}
