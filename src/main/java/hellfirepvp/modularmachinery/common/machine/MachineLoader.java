/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.machine;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.modifier.SingleBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.BlockInformationVariable;
import hellfirepvp.modularmachinery.common.util.FileUtils;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.Tuple;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: MachineLoader
 * Created by HellFirePvP
 * Date: 27.06.2017 / 11:53
 */
public class MachineLoader {

    public static final  Map<String, BlockArray.BlockInformation> VARIABLE_CONTEXT = new HashMap<>();
    private static final Gson                                     GSON             = new GsonBuilder()
        .registerTypeHierarchyAdapter(DynamicMachine.class, new DynamicMachine.MachineDeserializer())
        .registerTypeHierarchyAdapter(BlockInformationVariable.class, new BlockInformationVariable.Deserializer())
        .registerTypeHierarchyAdapter(SingleBlockModifierReplacement.class, new SingleBlockModifierReplacement.Deserializer())
        .registerTypeHierarchyAdapter(RecipeModifier.class, new RecipeModifier.Deserializer())
        .create();
    private static final Gson                                     PRELOAD_GSON     = new GsonBuilder()
        .registerTypeHierarchyAdapter(DynamicMachine.class, new DynamicMachinePreDeserializer())
        .create();
    private static       Map<String, Exception>                   failedAttempts   = new HashMap<>();

    public static Map<FileType, List<File>> discoverDirectory(File directory) {
        Map<FileType, List<File>> candidates = new EnumMap<>(FileType.class);
        for (FileType type : FileType.values()) {
            candidates.put(type, Lists.newLinkedList());
        }
        LinkedList<File> directories = Lists.newLinkedList();
        directories.add(directory);
        while (!directories.isEmpty()) {
            File dir = directories.remove(0);
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    directories.addLast(file);
                } else {
                    //I am *not* taking chances with this ordering
                    if (FileType.VARIABLES.accepts(file.getName())) {
                        candidates.get(FileType.VARIABLES).add(file);
                    } else if (FileType.MACHINE.accepts(file.getName())) {
                        candidates.get(FileType.MACHINE).add(file);
                    }
                }
            }
        }
        return candidates;
    }

    public static List<Tuple<DynamicMachine, String>> registerMachines(Collection<File> machineCandidates) {
        List<Tuple<DynamicMachine, String>> registeredMachinery = Lists.newArrayList();

        machineCandidates.parallelStream().forEach(file -> {
            try {
                String jsonString = FileUtils.readFile(file);
                DynamicMachine machine = JsonUtils.fromJson(PRELOAD_GSON, jsonString, DynamicMachine.class, false);
                if (machine != null) {
                    synchronized (registeredMachinery) {
                        registeredMachinery.add(new Tuple<>(machine, jsonString));
                    }
                }
            } catch (Exception exc) {
                failedAttempts.put(file.getPath(), exc);
            }
        });
        return registeredMachinery;
    }


    public static List<DynamicMachine> loadMachines(Collection<Tuple<DynamicMachine, String>> registeredMachineList) {
        List<DynamicMachine> loadedMachines = new ArrayList<>();

        registeredMachineList.parallelStream().forEach(registryAndJsonStr -> {
            DynamicMachine preloadMachine = registryAndJsonStr.getFirst();
            try {
                DynamicMachine loadedMachine = JsonUtils.fromJson(GSON, registryAndJsonStr.getSecond(), DynamicMachine.class, false);
                if (loadedMachine != null) {
                    preloadMachine.mergeFrom(loadedMachine);
                    synchronized (loadedMachines) {
                        loadedMachines.add(preloadMachine);
                    }
                }
            } catch (Exception exc) {
                ModularMachinery.log.warn(preloadMachine.registryName, exc);
            }
        });
        return loadedMachines;
    }

    public static Map<String, Exception> captureFailedAttempts() {
        Map<String, Exception> failed = failedAttempts;
        failedAttempts = new HashMap<>();
        return failed;
    }

    public static void prepareContext(List<File> files) {
        VARIABLE_CONTEXT.clear();

        files.forEach(file -> {
            try (InputStreamReader isr = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
                Map<String, BlockArray.BlockInformation> variables = JsonUtils.fromJson(GSON, isr, BlockInformationVariable.class).getDefinedVariables();
                for (String key : variables.keySet()) {
                    VARIABLE_CONTEXT.put(key, variables.get(key));
                }
            } catch (Exception exc) {
                failedAttempts.put(file.getPath(), exc);
            }
        });
    }

    public enum FileType {

        VARIABLES,
        MACHINE;

        public boolean accepts(String fileName) {
            return switch (this) {
                case VARIABLES -> fileName.endsWith(".var.json");
                default -> fileName.endsWith(".json");
            };
        }

    }

}
