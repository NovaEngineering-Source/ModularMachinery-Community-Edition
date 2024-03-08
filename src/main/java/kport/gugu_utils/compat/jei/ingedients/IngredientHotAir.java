package kport.gugu_utils.compat.jei.ingedients;

import net.minecraft.util.ResourceLocation;

public class IngredientHotAir extends IngredientInfo {

    //最低工作温度
    private int minTemperature;

    //最高工作温度
    private int maxTemperature;


    public IngredientHotAir(String displayName, Object value, ResourceLocation resourceLocation, int minTemperature, int maxTemperature) {
        super(displayName, value, resourceLocation);
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
    }

    public int getMinTemperature() {
        return minTemperature;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }
}
