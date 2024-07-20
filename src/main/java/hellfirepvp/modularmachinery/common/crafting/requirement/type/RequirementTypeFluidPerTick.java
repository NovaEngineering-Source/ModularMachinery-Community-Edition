package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluidPerTick;
import hellfirepvp.modularmachinery.common.machine.IOType;
import net.minecraftforge.fluids.FluidStack;

public class RequirementTypeFluidPerTick extends RequirementType<FluidStack, RequirementFluidPerTick> {
    @Override
    public RequirementFluidPerTick createRequirement(IOType type, JsonObject jsonObject) {
        return null;
    }
}
