package github.kasuminova.mmce.common.integration.theoneprobe;

import appeng.api.implementations.IPowerChannelState;
import appeng.integration.modules.theoneprobe.TheOneProbeText;
import github.kasuminova.mmce.common.tile.base.MEMachineComponent;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MachineryHatchInfoProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return "modularmachinery:machinery_hatch_info_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, IBlockState iBlockState, IProbeHitData iProbeHitData) {
        if (!iBlockState.getBlock().hasTileEntity(iBlockState)) {
            return;
        }
        TileEntity tileEntity = world.getTileEntity(iProbeHitData.getPos());
        if (!(tileEntity instanceof MEMachineComponent meHatch)) {
            return;
        }

        // Implementation taken from appeng.integration.modules.theoneprobe.PartInfoProvider
        final IPowerChannelState state = meHatch;

        final boolean isActive = state.isActive();
        final boolean isPowered = state.isPowered();

        if (isActive && isPowered) {
            iProbeInfo.text(TheOneProbeText.DEVICE_ONLINE.getLocal());
        } else if (isPowered) {
            iProbeInfo.text(TheOneProbeText.DEVICE_MISSING_CHANNEL.getLocal());
        } else {
            iProbeInfo.text(TheOneProbeText.DEVICE_OFFLINE.getLocal());
        }
    }
}
