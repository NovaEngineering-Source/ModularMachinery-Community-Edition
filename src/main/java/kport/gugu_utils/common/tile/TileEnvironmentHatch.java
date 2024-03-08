package kport.gugu_utils.common.tile;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import static kport.gugu_utils.common.Constants.STRING_RESOURCE_ENVIRONMENT;
import kport.gugu_utils.CommonMMTile;
import kport.gugu_utils.GuGuCompoments;
import kport.gugu_utils.common.requirements.RequirementEnvironment;
import kport.gugu_utils.common.requirements.basic.CraftingResourceHolder;
import kport.gugu_utils.common.requirements.basic.IConsumable;

import javax.annotation.Nullable;

public class TileEnvironmentHatch extends CommonMMTile implements IConsumable<RequirementEnvironment.RT>, MachineComponentTile {

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new MachineComponent(IOType.INPUT) {
            @Override
            public ComponentType getComponentType() {
                return (ComponentType) GuGuCompoments.COMPONENT_ENVIRONMENT;
            }

            @Override
            public Object getContainerProvider() {
                return new CraftingResourceHolder<>(TileEnvironmentHatch.this);
            }
        };
    }

    @Override
    public boolean consume(RequirementEnvironment.RT outputToken, boolean doOperation) {
        if (outputToken.getType().isMeet(getWorld(), getPos())) {
            return true;
        }
        outputToken.setError("craftcheck.failure." + STRING_RESOURCE_ENVIRONMENT + "." + outputToken.getType().getName());
        return false;
    }


}
