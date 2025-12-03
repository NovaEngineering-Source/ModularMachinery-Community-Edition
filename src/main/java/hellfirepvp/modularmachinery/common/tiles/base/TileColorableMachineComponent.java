/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles.base;

import github.kasuminova.mmce.common.util.Sides;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.common.data.Config;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileColorableMachineComponent
 * Created by HellFirePvP
 * Date: 15.08.2017 / 16:20
 */
public class TileColorableMachineComponent extends TileEntitySynchronized implements ColorableMachineTile, MachineGroupInput {

    private int definedColor = Config.machineColor;
    private int groupId;
    private boolean isGroupInput;

    public void setGroupId(int groupId) {
        this.groupId = groupId;
        markChunkDirty();
    }

    @Override
    public int getMachineColor() {
        return this.definedColor;
    }

    @Override
    public void setMachineColor(int newColor) {
        if (definedColor == newColor) {
            return;
        }

        this.definedColor = newColor;
        this.markForUpdateSync();
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        if (!compound.hasKey("casingColor")) {
            definedColor = Config.machineColor;
            return;
        }

        int newColor = compound.getInteger("casingColor");
        if (definedColor != newColor) {
            definedColor = newColor;
            Sides.CLIENT.runIfPresent(() -> ClientProxy.clientScheduler.addRunnable(() -> {
                World world = getWorld();
                //noinspection ConstantValue
                if (world != null) {
                    world.addBlockEvent(pos, world.getBlockState(pos).getBlock(), 1, 1);
                }
            }, 0));
        }

        if (canGroupInput()) {
            groupId = compound.getInteger("groupId");
            isGroupInput = compound.getBoolean("isGroupInput");
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setInteger("casingColor", this.definedColor);
        if (canGroupInput()) {
            compound.setInteger("groupId", this.groupId);
            compound.setBoolean("isGroupInput", this.isGroupInput);
        }
    }

    @Override
    public boolean canGroupInput() {
        return false;
    }

    @Override
    public boolean isGroupInput() {
        return isGroupInput;
    }

    @Override
    public void setGroupInput(boolean b) {
        isGroupInput = b;
        markChunkDirty();
    }

    @Override
    public int getGroupId() {
        if (isGroupInput()) return groupId;
        return -1;
    }

}