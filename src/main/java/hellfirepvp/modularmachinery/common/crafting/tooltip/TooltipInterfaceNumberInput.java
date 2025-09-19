package hellfirepvp.modularmachinery.common.crafting.tooltip;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementInterfaceNumInput;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceType;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.stream.Collectors;

public class TooltipInterfaceNumberInput extends RequirementTip {

    @Nonnull
    @Override
    public Collection<ComponentRequirement<?, ?>> filterRequirements(MachineRecipe recipe, Collection<ComponentRequirement<?, ?>> requirements) {
        return requirements.stream()
                           .filter(RequirementInterfaceNumInput.class::isInstance)
                           .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<String> buildTooltip(MachineRecipe recipe, Collection<ComponentRequirement<?, ?>> filteredComponents) {
        if (filteredComponents.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<String> tooltip = new ArrayList<>();
        for (ComponentRequirement<?, ?> filteredComponent : filteredComponents) {
            RequirementInterfaceNumInput requirement = (RequirementInterfaceNumInput) filteredComponent;
            float minValue = requirement.getMinValue();
            float maxValue = requirement.getMaxValue();

            SmartInterfaceType type = requirement.getType();
            String jeiTooltip = type.getJeiTooltip();
            if (!jeiTooltip.isEmpty()) {
                try {
                    if (type.getJeiTooltipArgsCount() == 1) {
                        tooltip.add(String.format(jeiTooltip, minValue));
                        continue;
                    } else if (type.getJeiTooltipArgsCount() == 2) {
                        tooltip.add(String.format(jeiTooltip, minValue, maxValue));
                        continue;
                    }
                    ModularMachinery.log.warn("Invalid JEITooltipArgsCount, Using default format...");
                    ModularMachinery.log.warn("Type: {}, TooltipMessage: {}", type.getType(), jeiTooltip);
                } catch (MissingFormatArgumentException ex) {
                    ModularMachinery.log.warn("Caught MissingFormatArgumentException! Using default format...");
                    ModularMachinery.log.warn("Type: {}, TooltipMessage: {}", type.getType(), jeiTooltip);
                }
            }

            if (minValue == maxValue) {
                tooltip.add(I18n.format("tooltip.machinery.smartinterface.value", minValue));
            } else {
                tooltip.add(I18n.format("tooltip.machinery.smartinterface.minvalue", minValue));
                tooltip.add(I18n.format("tooltip.machinery.smartinterface.maxvalue", maxValue));
            }
        }
        return tooltip;
    }
}
