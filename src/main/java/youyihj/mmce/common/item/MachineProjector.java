package youyihj.mmce.common.item;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import youyihj.mmce.common.preview.StructurePreviewHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author youyihj
 */
public class MachineProjector extends Item {
    public static final MachineProjector INSTANCE = new MachineProjector();

    private MachineProjector() {
        setMaxStackSize(1);
        setRegistryName(new ResourceLocation(ModularMachinery.MODID, "machine_projector"));
        setTranslationKey(ModularMachinery.MODID + '.' + "machine_projector");
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState blockState = worldIn.getBlockState(pos);
        Block block = blockState.getBlock();
        if (block instanceof BlockController && worldIn.isRemote) {
            BlockController controller = (BlockController) block;
            DynamicMachine machine = controller.getParentMachine();
            if (machine != null) {
                StructurePreviewHelper.renderMachinePreview(machine, pos);
                return EnumActionResult.SUCCESS;
            }
        }
        if (block instanceof BlockFactoryController && worldIn.isRemote) {
            BlockFactoryController controller = (BlockFactoryController) block;
            DynamicMachine machine = controller.getParentMachine();
            if (machine != null) {
                StructurePreviewHelper.renderMachinePreview(machine, pos);
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("tooltip.modularmachinery.machine_projector"));
    }
}
