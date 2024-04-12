package github.kasuminova.mmce.common.integration;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.me.helpers.AENetworkProxy;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ModIntegrationAE2 {

    public static void registerUpgrade() {
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemsMM.meFluidOutputBus), 5);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemsMM.meFluidinputBus), 5);
    }

    public static boolean securityCheck(EntityPlayer player, AENetworkProxy proxy) {
        final IGridNode gn = proxy.getNode();
        if (gn != null) {
            final IGrid g = gn.getGrid();
            final IEnergyGrid eg = g.getCache(IEnergyGrid.class);
            if (!eg.isNetworkPowered()) {
                return false;
            }

            final ISecurityGrid sg = g.getCache(ISecurityGrid.class);
            return !sg.hasPermission(player, SecurityPermissions.BUILD);
        }
        return false;
    }

}
