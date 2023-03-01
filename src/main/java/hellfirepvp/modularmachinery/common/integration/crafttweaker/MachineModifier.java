package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.concurrent.Action;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceType;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.LinkedList;
import java.util.List;

@ZenRegister
@ZenClass("mods.modularmachinery.MachineModifier")
public class MachineModifier {
    public static final List<Action> WAIT_FOR_MODIFY_LIST = new LinkedList<>();

    @ZenMethod
    public static void addSmartInterfaceType(String machineName, SmartInterfaceType type) {
        WAIT_FOR_MODIFY_LIST.add(() -> {
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineName));
            if (machine == null) {
                CraftTweakerAPI.logError("Could not find machine `" + machineName + "`!");
                return;
            }
            machine.addSmartInterfaceType(type);
        });
    }

    public static void loadAll() {
        for (Action waitForRegister : WAIT_FOR_MODIFY_LIST) {
            waitForRegister.doAction();
        }
    }
}
