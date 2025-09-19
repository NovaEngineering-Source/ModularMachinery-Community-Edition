package ink.ikx.mmce.common.assembly;

import github.kasuminova.mmce.common.util.BlockPos2ValueMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MachineAssemblyManager {

    private static final Map<BlockPos, MachineAssembly> MACHINE_ASSEMBLY_MAP = new BlockPos2ValueMap<>();

    public static void addMachineAssembly(MachineAssembly machineAssembly) {
        MACHINE_ASSEMBLY_MAP.put(machineAssembly.getCtrlPos(), machineAssembly);
    }

    public static boolean checkMachineExist(BlockPos ctrlPos) {
        return MACHINE_ASSEMBLY_MAP.containsKey(ctrlPos);
    }

    public static Collection<MachineAssembly> getMachineAssemblyListFromPlayer(EntityPlayer player) {
        return MACHINE_ASSEMBLY_MAP.values().stream()
                                   .filter(assembly -> player.getGameProfile().getId().equals(
                                       assembly.getPlayer().getGameProfile().getId()))
                                   .collect(Collectors.toList());
    }

    public static void removeMachineAssembly(BlockPos ctrlPos) {
        MACHINE_ASSEMBLY_MAP.remove(ctrlPos);
    }

    public static void removeMachineAssembly(EntityPlayer player) {
        List<BlockPos> willBeRemoved = new ArrayList<>();
        for (final MachineAssembly assembly : MACHINE_ASSEMBLY_MAP.values()) {
            if (assembly.getPlayer().equals(player)) {
                willBeRemoved.add(assembly.getCtrlPos());
            }
        }
        willBeRemoved.forEach(MachineAssemblyManager::removeMachineAssembly);
    }

}
