package kport.gugu_utils.compat;

import kport.gugu_utils.common.pressure.IUpgradeAcceptorWrapper;
import kport.gugu_utils.common.tile.TilePressureInputHatch;
import kport.gugu_utils.common.tile.TilePressureOutputHatch;
import me.desht.pneumaticcraft.api.PneumaticRegistry;

public class PneumaticCraftCompat {
    public static void postInit() {
        PneumaticRegistry.getInstance().getItemRegistry().registerUpgradeAcceptor(new IUpgradeAcceptorWrapper(new TilePressureInputHatch()));
        PneumaticRegistry.getInstance().getItemRegistry().registerUpgradeAcceptor(new IUpgradeAcceptorWrapper(new TilePressureOutputHatch()));

    }
}
