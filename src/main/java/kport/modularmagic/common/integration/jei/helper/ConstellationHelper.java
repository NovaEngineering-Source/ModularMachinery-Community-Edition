package kport.modularmagic.common.integration.jei.helper;

import kport.modularmagic.common.integration.jei.ingredient.Constellation;
import mezz.jei.api.ingredients.IIngredientHelper;

import javax.annotation.Nullable;

public class ConstellationHelper<T extends Constellation> implements IIngredientHelper<T> {

    @Nullable
    @Override
    public T getMatch(Iterable<T> ingredients, T ingredientToMatch) {
        for (T constellation : ingredients) {
            if (constellation.getConstellation() == ingredientToMatch.getConstellation()) {
                return constellation;
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(T ingredient) {
        return ingredient.getConstellation().getSimpleName();
    }

    @Override
    public String getUniqueId(T ingredient) {
        return ingredient.getConstellation().getSimpleName();
    }

    @Override
    public String getWildcardId(T ingredient) {
        return ingredient.getConstellation().getSimpleName();
    }

    @Override
    public String getModId(T ingredient) {
        return "astralsorcery";
    }

    @Override
    public String getResourceId(T ingredient) {
        return ingredient.getConstellation().getSimpleName();
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
