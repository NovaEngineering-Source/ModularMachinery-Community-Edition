package github.kasuminova.mmce.common.serialize.crafting.requirement

import github.kasuminova.mmce.common.serialize.DataStructure
import github.kasuminova.mmce.common.serialize.getValue

class EnergyRequirementData : DataStructure() {

    val energyPerTick by long("energyPerTick")

}