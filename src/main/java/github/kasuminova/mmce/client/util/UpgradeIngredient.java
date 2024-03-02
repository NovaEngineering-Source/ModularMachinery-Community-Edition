package github.kasuminova.mmce.client.util;

import com.github.bsideup.jabel.Desugar;
import hellfirepvp.modularmachinery.common.modifier.AbstractModifierReplacement;
import hellfirepvp.modularmachinery.common.modifier.MultiBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.modifier.SingleBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.Map;

@Desugar
public record UpgradeIngredient(ItemStack descStack, AbstractModifierReplacement replacement,
                                Map<BlockPos, BlockArray.BlockInformation> replacementPattern) {

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

}
