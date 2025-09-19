/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.lib;

import github.kasuminova.mmce.common.block.appeng.BlockMEFluidInputBus;
import github.kasuminova.mmce.common.block.appeng.BlockMEFluidOutputBus;
import github.kasuminova.mmce.common.block.appeng.BlockMEGasInputBus;
import github.kasuminova.mmce.common.block.appeng.BlockMEGasOutputBus;
import github.kasuminova.mmce.common.block.appeng.BlockMEItemInputBus;
import github.kasuminova.mmce.common.block.appeng.BlockMEItemOutputBus;
import github.kasuminova.mmce.common.block.appeng.BlockMEPatternMirrorImage;
import github.kasuminova.mmce.common.block.appeng.BlockMEPatternProvider;
import hellfirepvp.modularmachinery.common.block.BlockCasing;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockEnergyInputHatch;
import hellfirepvp.modularmachinery.common.block.BlockEnergyOutputHatch;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.block.BlockFluidInputHatch;
import hellfirepvp.modularmachinery.common.block.BlockFluidOutputHatch;
import hellfirepvp.modularmachinery.common.block.BlockInputBus;
import hellfirepvp.modularmachinery.common.block.BlockOutputBus;
import hellfirepvp.modularmachinery.common.block.BlockParallelController;
import hellfirepvp.modularmachinery.common.block.BlockSmartInterface;
import hellfirepvp.modularmachinery.common.block.BlockUpgradeBus;
import kport.modularmagic.common.block.BlockAspectProviderInput;
import kport.modularmagic.common.block.BlockAspectProviderOutput;
import kport.modularmagic.common.block.BlockAuraProviderInput;
import kport.modularmagic.common.block.BlockAuraProviderOutput;
import kport.modularmagic.common.block.BlockConstellationProvider;
import kport.modularmagic.common.block.BlockGridProviderInput;
import kport.modularmagic.common.block.BlockGridProviderOutput;
import kport.modularmagic.common.block.BlockImpetusProviderInput;
import kport.modularmagic.common.block.BlockImpetusProviderOutput;
import kport.modularmagic.common.block.BlockLifeEssenceProviderInput;
import kport.modularmagic.common.block.BlockLifeEssenceProviderOutput;
import kport.modularmagic.common.block.BlockManaProviderInput;
import kport.modularmagic.common.block.BlockManaProviderOutput;
import kport.modularmagic.common.block.BlockRainbowProvider;
import kport.modularmagic.common.block.BlockStarlightProviderInput;
import kport.modularmagic.common.block.BlockStarlightProviderOutput;
import kport.modularmagic.common.block.BlockWillProviderInput;
import kport.modularmagic.common.block.BlockWillProviderOutput;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlocksMM
 * Created by HellFirePvP
 * Date: 28.06.2017 / 20:22
 */
public class BlocksMM {

    public static BlockController        blockController;
    public static BlockFactoryController blockFactoryController;

    public static BlockCasing blockCasing;

    public static BlockInputBus          itemInputBus;
    public static BlockOutputBus         itemOutputBus;
    public static BlockFluidInputHatch   fluidInputHatch;
    public static BlockFluidOutputHatch  fluidOutputHatch;
    public static BlockEnergyInputHatch  energyInputHatch;
    public static BlockEnergyOutputHatch energyOutputHatch;

    public static BlockSmartInterface     smartInterface;
    public static BlockParallelController parallelController;
    public static BlockUpgradeBus         upgradeBus;

    public static BlockMEItemOutputBus      meItemOutputBus;
    public static BlockMEItemInputBus       meItemInputBus;
    public static BlockMEFluidOutputBus     meFluidOutputBus;
    public static BlockMEFluidInputBus      meFluidInputBus;
    public static BlockMEGasOutputBus       meGasOutputBus;
    public static BlockMEGasInputBus        meGasInputBus;
    public static BlockMEPatternProvider    mePatternProvider;
    public static BlockMEPatternMirrorImage mePatternMirrorImage;

    public static BlockWillProviderInput  blockWillProviderInput;
    public static BlockWillProviderOutput blockWillProviderOutput;

    public static BlockLifeEssenceProviderInput  blockLifeEssenceProviderInput;
    public static BlockLifeEssenceProviderOutput blockLifeEssenceProviderOutput;

    public static BlockGridProviderInput  blockGridProviderInput;
    public static BlockGridProviderOutput blockGridProviderOutput;

    public static BlockRainbowProvider blockRainbowProvider;

    public static BlockAuraProviderInput  blockAuraProviderInput;
    public static BlockAuraProviderOutput blockAuraProviderOutput;

    public static BlockStarlightProviderInput  blockStarlightProviderInput;
    public static BlockStarlightProviderOutput blockStarlightProviderOutput;

    public static BlockConstellationProvider blockConstellationProvider;

    public static BlockManaProviderInput  blockManaProviderInput;
    public static BlockManaProviderOutput blockManaProviderOutput;

    public static BlockImpetusProviderInput  blockImpetusProviderInput;
    public static BlockImpetusProviderOutput blockImpetusProviderOutput;

    public static BlockAspectProviderInput  blockAspectProviderInput;
    public static BlockAspectProviderOutput blockAspectProviderOutput;
}
