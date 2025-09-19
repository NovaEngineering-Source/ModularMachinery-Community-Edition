/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block;

import hellfirepvp.modularmachinery.client.util.EnergyDisplayUtil;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.prop.EnergyHatchData;
import hellfirepvp.modularmachinery.common.tiles.TileEnergyOutputHatch;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockEnergyOutputHatch
 * Created by HellFirePvP
 * Date: 08.07.2017 / 10:52
 */
public class BlockEnergyOutputHatch extends BlockEnergyHatch {
    @Optional.Method(modid = "gregtech")
    protected void addGTTooltip(List<String> tooltip, EnergyHatchData size) {
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.gregtech.voltage.out",
            MiscUtils.formatDecimal(size.getGTEnergyTransferVoltage()),
            size.getUnlocalizedGTEnergyTier()));
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.gregtech.amperage",
            String.valueOf(size.getGtAmperage())));
        tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.gregtech.storage",
            MiscUtils.formatDecimal(EnergyDisplayUtil.EnergyType.GT_EU.formatEnergyForDisplay(size.maxEnergy))));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        EnergyHatchData size = EnergyHatchData.values()[MathHelper.clamp(stack.getMetadata(), 0, EnergyHatchData.values().length - 1)];
        if (EnergyDisplayUtil.displayFETooltip) {
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.storage", MiscUtils.formatDecimal(size.maxEnergy)));
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.out.transfer", MiscUtils.formatDecimal(size.transferLimit)));
            tooltip.add("");
        }
        if (Mods.IC2.isPresent() && EnergyDisplayUtil.displayIC2EUTooltip) {
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.ic2.out.voltage",
                TextFormatting.BLUE + I18n.format(size.getUnlocalizedEnergyDescriptor())));
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.energyhatch.ic2.out.transfer",
                TextFormatting.BLUE + MiscUtils.formatDecimal(size.getIC2EnergyTransmission()),
                TextFormatting.BLUE + I18n.format("tooltip.energyhatch.ic2.powerrate")));
            tooltip.add("");
        }
        if (Mods.GREGTECH.isPresent() && EnergyDisplayUtil.displayGTEUTooltip) {
            addGTTooltip(tooltip, size);
            tooltip.add("");
        }
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEnergyOutputHatch(state.getValue(BUS_TYPE));
    }
}