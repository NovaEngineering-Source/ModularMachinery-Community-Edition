package kport.gugu_utils.common.tile;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import static kport.gugu_utils.common.Constants.NAME_PRESSUREHATCH;
import kport.gugu_utils.GuGuCompoments;
import kport.gugu_utils.common.components.GenericMachineCompoment;
import kport.gugu_utils.common.requirements.RequirementCompressedAir;
import kport.gugu_utils.common.requirements.basic.IConsumable;
import kport.gugu_utils.common.requirements.basic.ICraftNotifier;
import static kport.gugu_utils.tools.ResourceUtils.j;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.tileentity.IMinWorkingPressure;

import javax.annotation.Nullable;

public class TilePressureInputHatch extends TilePressureHatch implements IMinWorkingPressure,
    MachineComponentTile, IConsumable<RequirementCompressedAir.RT>, ICraftNotifier<RequirementCompressedAir.RT> {
    @Override
    public String getName() {
        return j("tile", ModularMachinery.MODID, NAME_PRESSUREHATCH, "input");
    }

    @GuiSynced
    private float lastWorkingPressure = -Float.MAX_VALUE;

    @Override
    public float getMinWorkingPressure() {
        return lastWorkingPressure;
    }

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new GenericMachineCompoment<>(this, (ComponentType) GuGuCompoments.COMPONENT_COMPRESSED_AIR);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void finishCrafting(RequirementCompressedAir.RT outputToken) {
        lastWorkingPressure = -Float.MAX_VALUE;
    }

    @Override
    public boolean consume(RequirementCompressedAir.RT outputToken, boolean doOperation) {
        float pressure = outputToken.getPressure();
        if (doOperation) {
            lastWorkingPressure = pressure;
        }

        IAirHandler airHandler = this.getAirHandler(null);
        if (pressure > 0 && airHandler.getPressure() < pressure) {
            outputToken.setError("craftcheck.failure.gugu-utils:compressed_air.input.not_enough_pressure");
            return false;
        }
        if (pressure < 0 && airHandler.getPressure() < pressure) {
            outputToken.setError("craftcheck.failure.gugu-utils:compressed_air.input.not_enough_vacuum");
            return false;
        }


        int consume = Math.min(outputToken.getAir(), airHandler.getAir());
        outputToken.setAir(outputToken.getAir() - consume);

        if (doOperation)
            airHandler.addAir(-consume);
        return consume > 0;
    }
}
