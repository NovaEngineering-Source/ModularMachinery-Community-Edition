package kport.gugu_utils.compat.jei.ingedients;

import kport.gugu_utils.common.envtypes.EnvironmentType;
import net.minecraft.util.ResourceLocation;

public class IngredientEnvironment extends IngredientInfo{
    public EnvironmentType getType() {
        return (EnvironmentType) this.getValue();
    }
    public IngredientEnvironment(String displayName, Object value, ResourceLocation resourceLocation) {
        super(displayName, value, resourceLocation);
    }
}
