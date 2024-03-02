/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.machine;

import crafttweaker.annotations.ZenRegister;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.data.DataLoadProfiler;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.ProgressManager;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: MachineRegistry
 * Created by HellFirePvP
 * Date: 27.06.2017 / 11:53
 */
@ZenRegister
@ZenClass("mods.modularmachinery.MachineRegistry")
public class MachineRegistry implements Iterable<DynamicMachine> {

    private static final MachineRegistry INSTANCE = new MachineRegistry();
    //Type: <MachineName, <Machine, JSONString>>
    private static final Map<ResourceLocation, Tuple<DynamicMachine, String>> WAIT_FOR_LOAD_MACHINERY = new HashMap<>();
    private static final Map<ResourceLocation, DynamicMachine> LOADED_MACHINERY = new HashMap<>();

    private MachineRegistry() {
    }

    public static MachineRegistry getRegistry() {
        return INSTANCE;
    }

    @ZenMethod
    public static String[] getAllRegisteredMachinery() {
        return LOADED_MACHINERY.keySet().stream().map(rl -> rl.getNamespace() + "." + rl.getPath()).toArray(String[]::new);
    }

    public static void preloadMachines() {
        ProgressManager.ProgressBar barMachinery = ProgressManager.push("MachineRegistry", 2);
        barMachinery.step("Discovering Files");
        DataLoadProfiler profiler = new DataLoadProfiler();

        Map<MachineLoader.FileType, List<File>> candidates = MachineLoader.discoverDirectory(CommonProxy.dataHolder.getMachineryDirectory());
        barMachinery.step("Registry Machines");

        DataLoadProfiler.StatusLine machines = profiler.createLine("Machines: ");
        DataLoadProfiler.Status success = machines.appendStatus("%s registered");
        DataLoadProfiler.Status failed = machines.appendStatus("%s failed");

        List<Tuple<DynamicMachine, String>> found = MachineLoader.registerMachines(candidates.get(MachineLoader.FileType.MACHINE));
        success.setCounter(found.size());
        Map<String, Exception> failures = MachineLoader.captureFailedAttempts();
        failed.setCounter(failures.size());
        if (!failures.isEmpty()) {
            ModularMachinery.log.warn("Encountered " + failures.size() + " problems while registering machinery!");
            for (String fileName : failures.keySet()) {
                ModularMachinery.log.warn("Couldn't load machinery " + fileName);
                failures.get(fileName).printStackTrace();
            }
        }
        ProgressManager.pop(barMachinery);

        for (Tuple<DynamicMachine, String> waitForRegistry : found) {
            WAIT_FOR_LOAD_MACHINERY.put(waitForRegistry.getFirst().getRegistryName(), waitForRegistry);
        }
    }

    public static Collection<DynamicMachine> loadMachines(@Nullable ICommandSender sender) {
        ProgressManager.ProgressBar barMachinery = ProgressManager.push("MachineRegistry", 3);
        barMachinery.step("Discovering Files");
        DataLoadProfiler profiler = new DataLoadProfiler();

        Map<MachineLoader.FileType, List<File>> candidates = MachineLoader.discoverDirectory(CommonProxy.dataHolder.getMachineryDirectory());
        barMachinery.step("Loading Variables");
        MachineLoader.prepareContext(candidates.get(MachineLoader.FileType.VARIABLES));

        DataLoadProfiler.StatusLine variables = profiler.createLine("Variables: ");
        DataLoadProfiler.Status success = variables.appendStatus("%s loaded");
        DataLoadProfiler.Status failed = variables.appendStatus("%s failed");

        success.setCounter(MachineLoader.VARIABLE_CONTEXT.size());

        Map<String, Exception> failures = MachineLoader.captureFailedAttempts();

        failed.setCounter(failures.size());
        if (!failures.isEmpty()) {
            ModularMachinery.log.warn("Encountered " + failures.size() + " problems while loading variables!");
            for (String fileName : failures.keySet()) {
                ModularMachinery.log.warn("Couldn't load variables of " + fileName);
                failures.get(fileName).printStackTrace();
            }
        }
        barMachinery.step("Loading Machines");

        DataLoadProfiler.StatusLine machines = profiler.createLine("Machines: ");
        success = machines.appendStatus("%s loaded");
        failed = machines.appendStatus("%s failed");

        List<DynamicMachine> found = MachineLoader.loadMachines(WAIT_FOR_LOAD_MACHINERY.values());
        WAIT_FOR_LOAD_MACHINERY.clear();

        success.setCounter(found.size());
        failures = MachineLoader.captureFailedAttempts();
        failed.setCounter(failures.size());
        if (!failures.isEmpty()) {
            ModularMachinery.log.warn("Encountered " + failures.size() + " problems while loading machinery!");
            for (String fileName : failures.keySet()) {
                ModularMachinery.log.warn("Couldn't load machinery " + fileName);
                failures.get(fileName).printStackTrace();
            }
        }
        ProgressManager.pop(barMachinery);
        profiler.printLines(sender);
        return Collections.unmodifiableList(found);
    }

    public static void registerMachines(Collection<DynamicMachine> machines) {
        for (DynamicMachine machine : machines) {
            LOADED_MACHINERY.put(machine.getRegistryName(), machine);
        }
    }

    public static void reloadMachine(Collection<DynamicMachine> machines) {
        for (DynamicMachine machine : machines) {
            DynamicMachine loaded = LOADED_MACHINERY.get(machine.getRegistryName());
            if (loaded != null) {
                loaded.mergeFrom(machine);
            } else {
                LOADED_MACHINERY.put(machine.getRegistryName(), machine);
            }
        }
    }

    public static List<DynamicMachine> getWaitForLoadMachines() {
        List<DynamicMachine> machineList = new ArrayList<>();
        for (Tuple<DynamicMachine, String> value : WAIT_FOR_LOAD_MACHINERY.values()) {
            machineList.add(value.getFirst());
        }

        return machineList;
    }

    public static List<DynamicMachine> getLoadedMachines() {
        List<DynamicMachine> machineList = new ArrayList<>(LOADED_MACHINERY.values());
        return Collections.unmodifiableList(machineList);
    }

    @Nullable
    public DynamicMachine getMachine(@Nullable ResourceLocation name) {
        if (name == null) return null;
        return LOADED_MACHINERY.get(name);
    }

    @Override
    public Iterator<DynamicMachine> iterator() {
        return LOADED_MACHINERY.values().iterator();
    }

}
