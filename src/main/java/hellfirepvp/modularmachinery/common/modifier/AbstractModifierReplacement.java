package hellfirepvp.modularmachinery.common.modifier;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractModifierReplacement {
    private static final AtomicInteger        DEFAULT_NAME_COUNTER = new AtomicInteger(0);
    protected final      String               modifierName;
    protected final      List<RecipeModifier> modifier;
    protected final      List<String>         description;
    protected final      ItemStack            descriptiveStack;

    public AbstractModifierReplacement(List<RecipeModifier> modifier, String description, ItemStack descriptiveStack) {
        this(null, modifier, description, descriptiveStack);
    }

    public AbstractModifierReplacement(String modifierName, List<RecipeModifier> modifier, String description, ItemStack descriptiveStack) {
        this.modifierName = modifierName == null ? "ReplacementModifier - " + DEFAULT_NAME_COUNTER.getAndIncrement() : modifierName;
        this.modifier = modifier;
        this.description = description.isEmpty() ? Lists.newArrayList() : MiscUtils.splitStringBy(description, "\n");
        this.descriptiveStack = descriptiveStack;
    }

    public AbstractModifierReplacement(String modifierName, List<RecipeModifier> modifier, List<String> description, ItemStack descriptiveStack) {
        this.modifierName = modifierName;
        this.modifier = modifier;
        this.description = description;
        this.descriptiveStack = descriptiveStack;
    }

    public String getModifierName() {
        return modifierName;
    }

    public List<RecipeModifier> getModifiers() {
        return Collections.unmodifiableList(modifier);
    }

    public List<String> getDescriptionLines() {
        return description;
    }

    public ItemStack getDescriptiveStack() {
        return descriptiveStack;
    }
}
