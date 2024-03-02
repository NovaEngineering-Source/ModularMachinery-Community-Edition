package hellfirepvp.modularmachinery.common.modifier;

import net.minecraft.item.ItemStack;

import java.util.List;

public class DynamicModifierReplacement extends AbstractModifierReplacement {
    public DynamicModifierReplacement(final String modifierName, final List<RecipeModifier> modifier, final List<String> description, final ItemStack descriptiveStack) {
        super(modifierName, modifier, description, descriptiveStack);
    }

}
