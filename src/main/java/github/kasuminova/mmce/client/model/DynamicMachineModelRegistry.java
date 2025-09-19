package github.kasuminova.mmce.client.model;

import hellfirepvp.modularmachinery.common.machine.DynamicMachine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DynamicMachineModelRegistry {
    public static final DynamicMachineModelRegistry                 INSTANCE             = new DynamicMachineModelRegistry();
    private final       Map<String, MachineControllerModel>         machineModelRegistry = new HashMap<>();
    private final       Map<DynamicMachine, MachineControllerModel> machineDefaultModel  = new HashMap<>();

    private DynamicMachineModelRegistry() {
    }

    public void registerMachineModel(String modelName, MachineControllerModel model) {
        machineModelRegistry.put(modelName, model);
    }

    public MachineControllerModel getMachineModel(String modelName) {
        return machineModelRegistry.get(modelName);
    }

    public void registerMachineDefaultModel(DynamicMachine machine, MachineControllerModel model) {
        machineDefaultModel.put(machine, model);
    }

    public MachineControllerModel getMachineDefaultModel(DynamicMachine machine) {
        return machineDefaultModel.get(machine);
    }

    public Collection<MachineControllerModel> getAllModels() {
        return machineModelRegistry.values();
    }

    public void onReload() {
        machineDefaultModel.clear();
        machineModelRegistry.clear();
    }
}
