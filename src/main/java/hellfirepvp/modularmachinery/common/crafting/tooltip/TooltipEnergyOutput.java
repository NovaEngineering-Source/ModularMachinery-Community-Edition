/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.tooltip;

import hellfirepvp.modularmachinery.client.util.EnergyDisplayUtil;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TooltipEnergyOutput
 * Created by HellFirePvP
 * Date: 14.07.2019 / 19:37
 */
public class TooltipEnergyOutput extends RequirementTip {

    @Nonnull
    @Override
    public Collection<ComponentRequirement<?, ?>> filterRequirements(MachineRecipe recipe, Collection<ComponentRequirement<?, ?>> requirements) {
        return requirements.stream()
                           .filter(RequirementEnergy.class::isInstance)
                           .filter(r -> r.getActionType() == IOType.OUTPUT)
                           .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<String> buildTooltip(MachineRecipe recipe, Collection<ComponentRequirement<?, ?>> filteredComponents) {
        long totalEnergyOut = 0;
        for (ComponentRequirement<?, ?> energy : filteredComponents) {
            totalEnergyOut += ((RequirementEnergy) energy).getRequiredEnergyPerTick();
        }
        List<String> tooltip = new ArrayList<>(3);
        if (totalEnergyOut > 0) {
            String energyType = I18n.format(EnergyDisplayUtil.type.getUnlocalizedFormat());
            long energyOut = EnergyDisplayUtil.type.formatEnergyForDisplay(totalEnergyOut);

            tooltip.add(I18n.format("tooltip.machinery.energy.out") + I18n.format("tooltip.machinery.energy.tick", MiscUtils.formatNumber(energyOut), energyType));
            tooltip.add(I18n.format("tooltip.machinery.energy.total", MiscUtils.formatNumber(energyOut * recipe.getRecipeTotalTickTime()), energyType));
        }
        return tooltip;
    }
}
