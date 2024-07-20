package github.kasuminova.mmce.client.world;

import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.TaggedPositionBlockArray;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedList;
import java.util.List;

public class BlockModelHider {

    @SideOnly(Side.CLIENT)
    public static void hideOrShowBlocks(TileMultiblockMachineController ctrl) {
        if (Mods.MBD.isPresent()) {
            hideOrShowBlocksMBD(ctrl);
        } else if (Mods.COMPONENT_MODEL_HIDER.isPresent()) {
            hideOrShowBlocksPlugin(ctrl);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void onWorldUnload(World world) {
        if (Minecraft.getMinecraft().world == world) {
            if (Mods.MBD.isPresent()) {
                onWorldUnloadMBD();
            } else if (Mods.COMPONENT_MODEL_HIDER.isPresent()) {
                onWorldUnloadPlugin();
            }
        }
    }

    @Optional.Method(modid = "multiblocked")
    public static void onWorldUnloadMBD() {
        MultiblockWorldSavedData.clearDisabled();
    }

    @Optional.Method(modid = "component_model_hider")
    public static void onWorldUnloadPlugin() {
        MultiblockWorldSavedData.clearDisabled();
    }

    @SuppressWarnings("DuplicatedCode")
    @Optional.Method(modid = "multiblocked")
    private static void hideOrShowBlocksMBD(TileMultiblockMachineController ctrl) {
        DynamicMachine foundMachine = ctrl.getFoundMachine();
        BlockPos pos = ctrl.getPos();
        if (ctrl.isInvalid() || foundMachine == null || !foundMachine.isHideComponentsWhenFormed()) {
            MultiblockWorldSavedData.removeDisableModel(pos);
            return;
        }

        if (!MultiblockWorldSavedData.multiDisabled.containsKey(pos)) {
            TaggedPositionBlockArray foundPattern = ctrl.getFoundPattern();
            if (foundPattern != null) {
                List<BlockPos> transformed = new LinkedList<>();
                transformed.add(pos);
                for (BlockPos compPos : foundPattern.getPattern().keySet()) {
                    BlockPos add = compPos.add(pos);
                    transformed.add(add);
                }
                MultiblockWorldSavedData.addDisableModel(pos, transformed);
            }
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @Optional.Method(modid = "component_model_hider")
    private static void hideOrShowBlocksPlugin(TileMultiblockMachineController ctrl) {
        DynamicMachine foundMachine = ctrl.getFoundMachine();
        BlockPos pos = ctrl.getPos();
        if (ctrl.isInvalid() || foundMachine == null || !foundMachine.isHideComponentsWhenFormed()) {
            MultiblockWorldSavedData.removeDisableModel(pos);
            return;
        }

        if (!MultiblockWorldSavedData.multiDisabled.containsKey(pos)) {
            TaggedPositionBlockArray foundPattern = ctrl.getFoundPattern();
            if (foundPattern != null) {
                List<BlockPos> transformed = new LinkedList<>();
                transformed.add(pos);
                for (BlockPos compPos : foundPattern.getPattern().keySet()) {
                    BlockPos add = compPos.add(pos);
                    transformed.add(add);
                }
                MultiblockWorldSavedData.addDisableModel(pos, transformed);
            }
        }
    }

}
