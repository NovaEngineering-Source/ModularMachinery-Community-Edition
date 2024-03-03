package kport.gugu_utils.common.tile;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import kport.gugu_utils.GuGuCompoments;
import kport.gugu_utils.common.components.GenericMachineCompoment;
import kport.gugu_utils.common.requirements.RequirementMana;
import kport.gugu_utils.common.requirements.basic.IConsumable;

import javax.annotation.Nullable;

public class TileSparkManaInputHatch extends TileSparkManaHatch
        implements MachineComponentTile, IConsumable<RequirementMana.RT> {


    @Override
    public boolean canRecieveManaFromBursts() {
        return true;
    }

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new GenericMachineCompoment<>(this, (ComponentType) GuGuCompoments.COMPONENT_MANA);
    }

    @Override
    public boolean consume(RequirementMana.RT outputToken, boolean doOperation) {
        int consume = Math.min(outputToken.getMana(), this.mana);
        outputToken.setMana(outputToken.getMana() - consume);

        if(doOperation)
            this.mana -= consume;
        return consume > 0;
    }

}
