/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles.base;

import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.prop.FluidHatchSize;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.HybridGasTank;
import hellfirepvp.modularmachinery.common.util.HybridTank;
import hellfirepvp.modularmachinery.common.util.RedstoneHelper;
import mekanism.api.gas.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileFluidTank
 * Created by HellFirePvP
 * Date: 07.07.2017 / 17:51
 */
@Optional.InterfaceList({
        @Optional.Interface(modid = "mekanism", iface = "mekanism.api.gas.IGasHandler"),
        @Optional.Interface(modid = "mekanism", iface = "mekanism.api.gas.ITubeConnection")
})
@SuppressWarnings("deprecation")
public abstract class TileFluidTank extends TileColorableMachineComponent implements MachineComponentTile, IGasHandler, ITubeConnection, SelectiveUpdateTileEntity {

    private HybridTank tank;
    private IOType ioType;
    private FluidHatchSize hatchSize;
    private int prevRedstoneLevel = 0;

    public TileFluidTank() {
    }

    public TileFluidTank(FluidHatchSize size, IOType type) {
        this.tank = size.buildTank(this, true, true);
        this.hatchSize = size;
        this.ioType = type;
    }

    @Optional.Method(modid = "mekanism")
    private static boolean checkMekanismGasCapabilitiesPresence(Capability<?> capability) {
        return checkMekanismGasCapabilities(capability);
    }

    @Optional.Method(modid = "mekanism")
    private static boolean checkMekanismGasCapabilities(Capability<?> capability) {
        String gasType = IGasHandler.class.getName();
        String tubeConnectionName = ITubeConnection.class.getName();
        return capability != null && (capability.getName().equals(gasType) || capability.getName().equals(tubeConnectionName));
    }

    public HybridTank getTank() {
        return tank;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        if (Mods.MEKANISM.isPresent()) {
            if (checkMekanismGasCapabilitiesPresence(capability)) {
                return true;
            }
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) tank;
        }
        if (Mods.MEKANISM.isPresent()) {
            if (checkMekanismGasCapabilities(capability)) {
                return (T) this;
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        this.ioType = compound.getBoolean("input") ? IOType.INPUT : IOType.OUTPUT;
        this.hatchSize = FluidHatchSize.values()[MathHelper.clamp(compound.getInteger("size"), 0, FluidHatchSize.values().length - 1)];
        HybridTank newTank = hatchSize.buildTank(this, true, true);
        NBTTagCompound tankTag = compound.getCompoundTag("tank");
        newTank.readFromNBT(tankTag);
        this.tank = newTank;
        if (Mods.MEKANISM.isPresent()) {
            this.readMekGasData(tankTag);
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setBoolean("input", ioType == IOType.INPUT);
        compound.setInteger("size", this.hatchSize.ordinal());
        NBTTagCompound tankTag = new NBTTagCompound();
        this.tank.writeToNBT(tankTag);
        if (Mods.MEKANISM.isPresent()) {
            this.writeMekGasData(tankTag);
        }
        compound.setTag("tank", tankTag);
    }

    @Override
    public void markNoUpdate() {
        int redstoneLevel = RedstoneHelper.getRedstoneLevel(this);
        if (prevRedstoneLevel != redstoneLevel) {
            prevRedstoneLevel = redstoneLevel;
            this.requireUpdateComparatorLevel = true;
        }
        super.markNoUpdate();
        this.requireUpdateComparatorLevel = false;
    }

    @Nullable
    @Override
    public MachineComponent<?> provideComponent() {
        return new MachineComponent.FluidHatch(ioType) {
            @Override
            public HybridTank getContainerProvider() {
                return TileFluidTank.this.tank;
            }
        };
    }

    //Mek things

    @Override
    @Optional.Method(modid = "mekanism")
    public boolean canTubeConnect(EnumFacing side) {
        return true;
    }

    @Optional.Method(modid = "mekanism")
    private void writeMekGasData(NBTTagCompound compound) {
        if (this.tank instanceof HybridGasTank gasTank) {
            gasTank.writeGasToNBT(compound);
        }
    }

    @Optional.Method(modid = "mekanism")
    private void readMekGasData(NBTTagCompound compound) {
        if (this.tank instanceof HybridGasTank gasTank) {
            gasTank.readGasFromNBT(compound);
        }
    }

    @Override
    @Optional.Method(modid = "mekanism")
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        return this.tank instanceof HybridGasTank gasTank ? gasTank.receiveGas(side, stack, doTransfer) : 0;
    }

    @Override
    @Optional.Method(modid = "mekanism")
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        return this.tank instanceof HybridGasTank gasTank ? gasTank.drawGas(side, amount, doTransfer) : null;
    }

    @Override
    @Optional.Method(modid = "mekanism")
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return this.tank instanceof HybridGasTank gasTank && gasTank.canReceiveGas(side, type);
    }

    @Override
    @Optional.Method(modid = "mekanism")
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return this.tank instanceof HybridGasTank gasTank && gasTank.canDrawGas(side, type);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return this.tank instanceof HybridGasTank gasTank ? gasTank.getTankInfo() : NONE;
    }
}
