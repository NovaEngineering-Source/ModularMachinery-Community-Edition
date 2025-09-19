package kport.modularmagic.common.tile;

import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.power.IWorldPowerMultiplier;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.tile.TilePower;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.tiles.base.ColorableMachineTile;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentGridProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileGridProvider extends TilePower implements IWorldPowerMultiplier, MachineComponentTile, ITickable, ColorableMachineTile {

    private volatile float power;
    private          int   tick;
    private          int   color = Config.machineColor;

    @Override
    public int getMachineColor() {
        return this.color;
    }

    @Override
    public void setMachineColor(int newColor) {
        if (color == newColor) {
            return;
        }
        this.color = newColor;
        ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> {
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
            this.markDirty();
        });
    }

    @Override
    public float multiplier(@Nullable World world) {
        return 1.0F;
    }

    @Override
    public void onPowerChanged() {
    }

    @Override
    public float getPower() {
        return this.power;
    }

    public void setPower(float power) {
        this.tick = 2;
        this.power = power;
    }

    public PowerManager.PowerFreq getFreq() {
        return PowerManager.instance.getPowerFreq(this.frequency);
    }

    @Override
    public void update() {
        if (this.tick > 0) {
            this.tick--;
        } else if (this.power != 0) {
            this.power = 0;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.color = nbt.hasKey("casingColor") ? nbt.getInteger("casingColor") : Config.machineColor;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("casingColor", this.color);
        return nbt;
    }

    @Override
    public void addToDescriptionPacket(XUPacketBuffer packet) {
        super.addToDescriptionPacket(packet);
        packet.writeInt(this.color);
    }

    @Override
    public void handleDescriptionPacket(XUPacketBuffer packet) {
        super.handleDescriptionPacket(packet);
        this.color = packet.readInt();
    }

    public static class Input extends TileGridProvider {

        @Override
        public IWorldPowerMultiplier getMultiplier() {
            return this;
        }

        @Nullable
        @Override
        public MachineComponentGridProvider provideComponent() {
            return new MachineComponentGridProvider(this, IOType.INPUT);
        }
    }

    public static class Output extends TileGridProvider {

        @Override
        public IWorldPowerMultiplier getMultiplier() {
            return this;
        }

        @Nullable
        @Override
        public MachineComponentGridProvider provideComponent() {
            return new MachineComponentGridProvider(this, IOType.OUTPUT);
        }
    }
}
