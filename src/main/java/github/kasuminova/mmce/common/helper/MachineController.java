package github.kasuminova.mmce.common.helper;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.modularmachinery.MachineController")
public class MachineController {

    /**
     * 获取对应 IWorld 中的某个坐标的控制器。
     *
     * @param worldCT IWorld
     * @param posCT   IBlockPos
     * @return 如果无控制器则返回 null，否则返回 IMachineController 实例。
     */
    @ZenMethod
    public static IMachineController getControllerAt(IWorld worldCT, IBlockPos posCT) {
        World world = CraftTweakerMC.getWorld(worldCT);
        BlockPos pos = CraftTweakerMC.getBlockPos(posCT);
        if (world == null || pos == null || !world.isBlockLoaded(pos)) {
            return null;
        }

        TileEntity te = world.getTileEntity(pos);
        return te instanceof IMachineController ? (IMachineController) te : null;
    }

    /**
     * 获取对应 IWorld 中的某个坐标的控制器。
     *
     * @param worldCT IWorld
     * @return 如果无控制器则返回 null，否则返回 IMachineController 实例。
     */
    @ZenMethod
    public static IMachineController getControllerAt(IWorld worldCT, int x, int y, int z) {
        World world = CraftTweakerMC.getWorld(worldCT);
        BlockPos pos = new BlockPos(x, y, z);

        if (world == null || !world.isBlockLoaded(pos)) {
            return null;
        }

        TileEntity te = world.getTileEntity(pos);
        return te instanceof IMachineController ? (IMachineController) te : null;
    }

}
