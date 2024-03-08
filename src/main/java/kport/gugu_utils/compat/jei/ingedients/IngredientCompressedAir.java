package kport.gugu_utils.compat.jei.ingedients;

import net.minecraft.util.ResourceLocation;

public class IngredientCompressedAir extends IngredientInfo {
    public int getTicks() {
        return ticks;
    }

    private final int ticks;

    public float getMinPressure() {
        return minPressure;
    }

    private final float minPressure;

    public IngredientCompressedAir(String displayName, int air, ResourceLocation resourceLocation, float minPressure) {
        super(displayName, air, resourceLocation);
        this.minPressure = minPressure;
        this.ticks = -1;
    }
    public IngredientCompressedAir(String displayName, int air, ResourceLocation resourceLocation, float minPressure, int ticks) {
        super(displayName, air, resourceLocation);
        this.ticks = ticks;
        this.minPressure = minPressure;
    }
}
