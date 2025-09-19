package github.kasuminova.mmce.common.event.client;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.event.machine.MachineEvent;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenSetter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ZenRegister
@ZenClass("mods.modularmachinery.ControllerGUIRenderEvent")
public class ControllerGUIRenderEvent extends MachineEvent {
    private final List<String> extraInfo = new ArrayList<>();

    public ControllerGUIRenderEvent(TileMultiblockMachineController controller) {
        super(controller);
    }

    @ZenGetter("extraInfo")
    public String[] getExtraInfo() {
        return extraInfo.toArray(new String[0]);
    }

    @ZenSetter("extraInfo")
    public void setExtraInfo(String... info) {
        this.extraInfo.addAll(Arrays.asList(info));
    }
}
