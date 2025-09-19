package kport.modularmagic.common.integration.jei.helper;

import hellfirepvp.modularmachinery.ModularMachinery;
import kport.modularmagic.common.integration.jei.ingredient.Rainbow;
import mezz.jei.api.ingredients.IIngredientHelper;

import javax.annotation.Nullable;

public class RainbowHelper<T extends Rainbow> implements IIngredientHelper<T> {

    @Nullable
    @Override
    public T getMatch(Iterable<T> ingredients, T ingredientToMatch) {
        for (Rainbow rainbow : ingredients) {
            if (rainbow.isWorking() == ingredientToMatch.isWorking()) {
                return ingredientToMatch;
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(T ingredient) {
        return "Rainbow";
    }

    @Override
    public String getUniqueId(T ingredient) {
        return "rainbow";
    }

    @Override
    public String getWildcardId(T ingredient) {
        return "rainbow";
    }

    @Override
    public String getModId(T ingredient) {
        return ModularMachinery.MODID;
    }

    @Override
    public String getResourceId(T ingredient) {
        return "rainbow";
    }

    @Override
    public T copyIngredient(T ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(@Nullable T ingredient) {
        return null;
    }
}
