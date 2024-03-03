package kport.gugu_utils.common.tile;

import kport.gugu_utils.common.IGuiProvider;
import kport.gugu_utils.IColorableTileEntity;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.nbt.NBTTagCompound;

public class TilePressureHatch extends TileEntityPneumaticBase implements IGuiProvider, IColorableTileEntity {
    public static final String KEY_MACHINE_COLOR = "machine_color";

    @DescSynced
    protected int machineColor = hellfirepvp.modularmachinery.common.data.Config.machineColor;

    public TilePressureHatch() {
        super(PneumaticValues.DANGER_PRESSURE_TIER_TWO, PneumaticValues.MAX_PRESSURE_TIER_TWO, 16000, 4);
    }

    @Override
    public int getMachineColor() {
        return this.machineColor;
    }

    @Override
    public void setMachineColor(int newColor) {
        this.machineColor = newColor;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey(KEY_MACHINE_COLOR))
            this.machineColor = tag.getInteger(KEY_MACHINE_COLOR);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger(KEY_MACHINE_COLOR, this.getMachineColor());
        return tag;
    }
}
