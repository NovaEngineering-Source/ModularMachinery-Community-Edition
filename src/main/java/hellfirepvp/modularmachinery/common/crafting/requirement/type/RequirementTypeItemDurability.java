package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItemDurability;
import hellfirepvp.modularmachinery.common.machine.IOType;
import net.minecraft.item.ItemStack;

public class RequirementTypeItemDurability extends RequirementType<ItemStack, RequirementItemDurability> {

    @Override
    public ComponentRequirement<ItemStack, ? extends RequirementType<ItemStack, RequirementItemDurability>> createRequirement(final IOType type, final JsonObject jsonObject) {
        return null;
    }

}
