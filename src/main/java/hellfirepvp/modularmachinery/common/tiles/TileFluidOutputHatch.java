/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles;

import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.prop.FluidHatchSize;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileFluidTank;
import hellfirepvp.modularmachinery.common.util.HybridGasTank;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.util.GasUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.Optional;

import java.util.EnumSet;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileFluidOutputHatch
 * Created by HellFirePvP
 * Date: 07.07.2017 / 18:59
 */
public class TileFluidOutputHatch extends TileFluidTank implements MachineComponentTile {

    public static int minWorkDelay = 5;
    public static int maxWorkDelay = 60;


    public TileFluidOutputHatch() {
    }

    public TileFluidOutputHatch(FluidHatchSize size) {
        super(size, IOType.OUTPUT);
    }


    @Override
    public void doRestrictedTick() {
        if (getWorld().isRemote || !canWork(minWorkDelay, maxWorkDelay)) {
            return;
        }
        if (getTank() != null) {
            if (Mods.MEKANISM.isPresent()) {
                //       if (getTank().getFluid() != null) {
                //          MEKFluidsOutput();
                //      } else {
                MekGasOutput();
                //         }
            }
        }
    }

    //TODO
    //Use the mekanism ejection to output the fluids
    /*
    @Optional.Method(modid = "mekanism")
    public void MEKFluidsOutput() {
        emitFluid(this, getTank());
    }

    @Optional.Method(modid = "mekanism")
    public static void emitFluid(TileFluidOutputHatch tile, FluidTank tank) {
        if (tank.getFluid() != null) {
            FluidStack toSend = new FluidStack(tank.getFluid(), Math.min(tank.getCapacity(), tank.getFluidAmount()));
            tank.drain(PipeUtils.emit(EnumSet.allOf(EnumFacing.class), toSend, tile), true);
        }
    }
    */

    //Use the mekanism ejection to output the gas
    @Optional.Method(modid = "mekanism")
    public void MekGasOutput() {
        if (this.getTank() instanceof HybridGasTank gasTank) {
            emitGas(this, gasTank.getTank());
        }
    }

    @Optional.Method(modid = "mekanism")
    public static void emitGas(TileFluidOutputHatch tile, GasTank tank) {
        if (tank.getGas() != null) {
            GasStack toSend = new GasStack(tank.getGas().getGas(), Math.min(tank.getMaxGas(), tank.getStored()));
            tank.draw(GasUtils.emit(toSend, tile, EnumSet.allOf(EnumFacing.class)), true);
        }
    }

}
