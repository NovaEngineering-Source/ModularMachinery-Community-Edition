package github.kasuminova.mmce.client.util;

import hellfirepvp.modularmachinery.common.modifier.AbstractModifierReplacement;
import hellfirepvp.modularmachinery.common.modifier.MultiBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.modifier.SingleBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Record cause OpenJ9 can't found this class.
 */
@SuppressWarnings("ClassCanBeRecord")
public final class UpgradeIngredient {
    private final ItemStack descStack;
    private final AbstractModifierReplacement replacement;
    private final Map<BlockPos, BlockArray.BlockInformation> replacementPattern;

    public UpgradeIngredient(ItemStack descStack, AbstractModifierReplacement replacement,
                             Map<BlockPos, BlockArray.BlockInformation> replacementPattern) {
        this.descStack = descStack;
        this.replacement = replacement;
        this.replacementPattern = replacementPattern;
    }

    public static UpgradeIngredient of(ItemStack descStack, AbstractModifierReplacement replacement) {
        Map<BlockPos, BlockArray.BlockInformation> replacementPattern;
        if (replacement instanceof SingleBlockModifierReplacement r) {
            replacementPattern = Collections.singletonMap(r.getPos(), r.getBlockInformation());
        } else if (replacement instanceof MultiBlockModifierReplacement r) {
            replacementPattern = r.getBlockArray().getPattern();
        } else {
            replacementPattern = Collections.emptyMap();
        }
        return new UpgradeIngredient(descStack, replacement, replacementPattern);
    }

    public ItemStack descStack() {
        return descStack;
    }

    public AbstractModifierReplacement replacement() {
        return replacement;
    }

    public Map<BlockPos, BlockArray.BlockInformation> replacementPattern() {
        return replacementPattern;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UpgradeIngredient) obj;
        return Objects.equals(this.descStack, that.descStack) &&
                Objects.equals(this.replacement, that.replacement) &&
                Objects.equals(this.replacementPattern, that.replacementPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descStack, replacement, replacementPattern);
    }

    @Override
    public String toString() {
        return "UpgradeIngredient[" +
                "descStack=" + descStack + ", " +
                "replacement=" + replacement + ", " +
                "replacementPattern=" + replacementPattern + ']';
    }

}
