package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentOutputRestrictor;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeInterfaceNumInput;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.TileSmartInterface;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceData;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceType;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

public class RequirementInterfaceNumInput extends ComponentRequirement<Float, RequirementTypeInterfaceNumInput> {
    protected final SmartInterfaceType type;
    protected final float              minValue;
    protected final float              maxValue;

    public RequirementInterfaceNumInput(ResourceLocation machineName, String type, float minValue, float maxValue) throws NullPointerException {
        super(RequirementTypesMM.REQUIREMENT_INTERFACE_NUMBER_INPUT, IOType.INPUT);
        this.type = MachineRegistry.getRegistry().getMachine(machineName).getSmartInterfaceType(type);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public RequirementInterfaceNumInput(SmartInterfaceType type, float minValue, float maxValue) {
        super(RequirementTypesMM.REQUIREMENT_INTERFACE_NUMBER_INPUT, IOType.INPUT);
        this.type = type;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public RequirementInterfaceNumInput(SmartInterfaceType type, float value) {
        super(RequirementTypesMM.REQUIREMENT_INTERFACE_NUMBER_INPUT, IOType.INPUT);
        this.type = type;
        this.minValue = value;
        this.maxValue = value;
    }

    public SmartInterfaceType getType() {
        return type;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.component();
        if (cmp.getComponentType().equals(ComponentTypesMM.COMPONENT_SMART_INTERFACE) &&
            cmp instanceof final TileSmartInterface.SmartInterfaceProvider provider) {

            return provider.getMachineData(type.getType()) != null;
        } else {
            return false;
        }
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
        TileSmartInterface.SmartInterfaceProvider provider = (TileSmartInterface.SmartInterfaceProvider) component.getProvidedComponent();
        SmartInterfaceData data = provider.getMachineData(type.getType());
        if (data == null) {
            return CraftCheck.failure("component.missing.modularmachinery.interface.number");
        }
        float value = data.getValue();
        if (value >= minValue && value <= maxValue) {
            return CraftCheck.success();
        }

        String customMsg = type.getNotEqualMessage();
        return CraftCheck.failure(customMsg.isEmpty()
            ? "craftcheck.failure.interface.number.notequal"
            : customMsg);
    }

    @Override
    public RequirementInterfaceNumInput deepCopy() {
        return new RequirementInterfaceNumInput(type, minValue, maxValue);
    }

    @Override
    public RequirementInterfaceNumInput deepCopyModified(List<RecipeModifier> modifiers) {
        return deepCopy();
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "component.missing.modularmachinery.interface.number";
    }

    @Override
    public JEIComponent<Float> provideJEIComponent() {
        return null;
    }
}
