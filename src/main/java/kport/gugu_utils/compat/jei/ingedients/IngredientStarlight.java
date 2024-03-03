package kport.gugu_utils.compat.jei.ingedients;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import net.minecraft.util.ResourceLocation;

public class IngredientStarlight extends IngredientInfo{
    public IConstellation getConstellation() {
        return constellation;
    }

    private final IConstellation constellation;

    public IngredientStarlight(String displayName, Object value, ResourceLocation resourceLocation, IConstellation constellation) {
        super(displayName, value, resourceLocation);
        this.constellation = constellation;
    }
}
