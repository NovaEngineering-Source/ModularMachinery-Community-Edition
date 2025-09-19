package github.kasuminova.mmce.common.handler;

import github.kasuminova.mmce.common.event.machine.MachineEvent;
import github.kasuminova.mmce.common.upgrade.DynamicMachineUpgrade;
import github.kasuminova.mmce.common.upgrade.MachineUpgrade;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.helper.UpgradeEventHandlerCT;
import hellfirepvp.modularmachinery.common.tiles.TileUpgradeBus;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.MiscUtils;

import java.util.List;

public class UpgradeMachineEventHandler {

    public static void onMachineEvent(MachineEvent event) {
        TileMultiblockMachineController controller = event.getController();

        for (List<MachineUpgrade> upgrades : controller.getFoundUpgrades().values()) {
            for (final MachineUpgrade upgrade : upgrades) {
                if (upgrade instanceof DynamicMachineUpgrade dynamicUpgrade) {
                    if (dynamicUpgrade.isInvalid()) {
                        continue;
                    }
                }

                List<UpgradeEventHandlerCT> processors = upgrade.getEventHandlers(event.getClass());
                if (processors.isEmpty()) {
                    continue;
                }

                TileUpgradeBus parentBus = upgrade.getParentBus();
                if (parentBus == null) {
                    ModularMachinery.log.warn("Found a null UpgradeBus at controller " + MiscUtils.posToString(controller.getPos()));
                    continue;
                }

                TileUpgradeBus.UpgradeBusProvider provider = parentBus.provideComponent();
                upgrade.readNBT(provider.getUpgradeCustomData(upgrade));

                synchronized (parentBus) {
                    for (final UpgradeEventHandlerCT handler : processors) {
                        handler.handle(event, upgrade);
                        if (event.isCanceled()) {
                            break;
                        }
                    }
                }

                provider.setUpgradeCustomData(upgrade, upgrade.writeNBT());

                if (event.isCanceled()) {
                    return;
                }
            }
        }
    }

}
