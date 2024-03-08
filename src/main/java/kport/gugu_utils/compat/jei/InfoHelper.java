package kport.gugu_utils.compat.jei;

import hellfirepvp.modularmachinery.ModularMachinery;
import kport.gugu_utils.compat.jei.ingedients.IngredientInfo;
import mezz.jei.api.ingredients.IIngredientHelper;

import javax.annotation.Nullable;

public class InfoHelper<T extends IngredientInfo> implements IIngredientHelper<T> {
    @Nullable
    @Override
    public T getMatch(Iterable<T> ingredients, T ingredientToMatch) {
        String re = ingredientToMatch.getResource().toString();
        for (T i : ingredients) {
            if (re.equals(i.getResource().toString())) {
                return i;
            }
        }
        return null;
    }


    @Override
    public String getDisplayName(T ingredient) {
        return ingredient.getDisplayName();
    }

    @Override
    public String getUniqueId(T ingredient) {
        return ingredient.getResource().toString();
    }

    @Override
    public String getWildcardId(T ingredient) {
        return ingredient.getResource().toString();
    }

    @Override
    public String getModId(T ingredient) {
        return ModularMachinery.MODID;
    }

    @Override
    public String getDisplayModId(T ingredient) {
        return ingredient.getResource().getNamespace();
    }

    @Override
    public String getResourceId(T ingredient) {
        return ingredient.getResource().getPath();
    }

    @Override
    public T copyIngredient(T ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(@Nullable T ingredient) {
        return "This is an ingredient just for info by GuGu Utils";
    }
}
