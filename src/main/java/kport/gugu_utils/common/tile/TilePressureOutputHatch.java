package kport.gugu_utils.common.tile;


import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import static kport.gugu_utils.common.Constants.NAME_PRESSUREHATCH;
import kport.gugu_utils.GuGuCompoments;
import kport.gugu_utils.common.components.GenericMachineCompoment;
import kport.gugu_utils.common.requirements.RequirementCompressedAir;
import kport.gugu_utils.common.requirements.basic.IGeneratable;
import static kport.gugu_utils.tools.ResourceUtils.j;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

public class TilePressureOutputHatch extends TilePressureHatch implements
    MachineComponentTile, IGeneratable<RequirementCompressedAir.RT> {
    public TilePressureOutputHatch() {
        addApplicableUpgrade();
    }
    @Override
    public String getName() {
        return j("tile", ModularMachinery.MODID, NAME_PRESSUREHATCH, "output");
    }

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new GenericMachineCompoment<>(this, (ComponentType) GuGuCompoments.COMPONENT_COMPRESSED_AIR);
    }

    @Override
    public boolean generate(RequirementCompressedAir.RT outputToken, boolean doOperation) {
        int air = outputToken.getAir();
        outputToken.setAir(0);
        if (doOperation)
            addAir(air);
        return air > 0;
    }

    @Override
    public void update() {
        super.update();
        //漏气
        if (!getWorld().isRemote) {
            List<Pair<EnumFacing, IAirHandler>> teList = getAirHandler(null).getConnectedPneumatics();
            IAirHandler airHandler = getAirHandler(null);
            if (teList.size() == 0){
                airHandler.airLeak(EnumFacing.UP);
            }
        }
    }


}
