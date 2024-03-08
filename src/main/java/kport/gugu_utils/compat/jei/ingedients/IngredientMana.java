package kport.gugu_utils.compat.jei.ingedients;

import net.minecraft.util.ResourceLocation;

public class IngredientMana extends IngredientInfo {
    public int getTicks() {
        return ticks;
    }

    private final int ticks;

    public IngredientMana(String displayName, int value, ResourceLocation resourceLocation) {
        super(displayName, value, resourceLocation);
        this.ticks = -1;
    }
    public IngredientMana(String displayName, int value, ResourceLocation resourceLocation, int ticks) {
        super(displayName, value, resourceLocation);
        this.ticks = ticks;
    }
}
