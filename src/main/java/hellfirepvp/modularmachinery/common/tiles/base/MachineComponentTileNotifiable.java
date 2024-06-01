package hellfirepvp.modularmachinery.common.tiles.base;

import github.kasuminova.mmce.common.event.machine.MachineEvent;

public interface MachineComponentTileNotifiable extends MachineComponentTile {

    void onMachineEvent(final MachineEvent event);

}
