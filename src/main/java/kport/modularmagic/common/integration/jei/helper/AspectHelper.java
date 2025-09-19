package kport.modularmagic.common.integration.jei.helper;

import com.google.common.collect.Iterables;
import hellfirepvp.modularmachinery.ModularMachinery;
import mezz.jei.api.ingredients.IIngredientHelper;
import thaumcraft.api.aspects.AspectList;

import javax.annotation.Nullable;

public class AspectHelper<T extends AspectList> implements IIngredientHelper<T> {

    @Nullable
    @Override
    public T getMatch(Iterable<T> ingredients, T ingredientToMatch) {
        if (Iterables.isEmpty(ingredients)) {
            return null;
        }

        for (T list : ingredients) {
            if (list.getAspects()[0].getTag().equalsIgnoreCase(ingredientToMatch.getAspects()[0].getTag())) {
                return list;
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(T ingredient) {
        return ingredient.getAspects()[0].getName();
    }

    @Override
    public String getUniqueId(T ingredient) {
        return ingredient.getAspects()[0].getTag();
    }

    @Override
    public String getWildcardId(T ingredient) {
        return ingredient.getAspects()[0].getTag();
    }

    @Override
    public String getModId(T ingredient) {
        return ModularMachinery.MODID;
    }

    @Override
    public String getResourceId(T ingredient) {
        return null;
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
