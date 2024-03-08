package kport.gugu_utils.common.tools;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.selection.PlayerStructureSelectionHelper;
import static kport.gugu_utils.common.Constants.NAME_RANGED_CONSTRUCTION_TOOL;
import kport.gugu_utils.tools.ItemNBTUtils;
import static kport.gugu_utils.tools.ResourceUtils.j;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRangedConstructTool extends Item {
    private static final String TAG_X = "startX";
    private static final String TAG_Y = "startY";
    private static final String TAG_Z = "startZ";

    public ItemRangedConstructTool() {

        setMaxStackSize(1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    public static BlockPos getNbtPos(ItemStack stack) {
        int x = ItemNBTUtils.getInt(stack, TAG_X, 0);
        int y = ItemNBTUtils.getInt(stack, TAG_Y, -1);
        int z = ItemNBTUtils.getInt(stack, TAG_Z, 0);
        return new BlockPos(x, y, z);
    }

    public static void setNbtPos(ItemStack stack, BlockPos pos) {

        ItemNBTUtils.setInt(stack, TAG_X, pos.getX());
        ItemNBTUtils.setInt(stack, TAG_Y, pos.getY());
        ItemNBTUtils.setInt(stack, TAG_Z, pos.getZ());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("message.gugu-utils.rangedconstructtool.creative"));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote && player.isCreative() && worldIn.getMinecraftServer().getPlayerList().canSendCommands(player.getGameProfile())) {
            ItemStack held = player.getHeldItemMainhand();
            if (held.isEmpty())
                held = player.getHeldItemOffhand();
            BlockPos first = getNbtPos(held);
            if (first.getY() > 0) {
                setNbtPos(held, new BlockPos(0, -1, 0));
                PlayerStructureSelectionHelper.purgeSelection(player);
                for (BlockPos blockPos : BlockPos.getAllInBoxMutable(first, pos)) {
                    if (worldIn.getBlockState(blockPos).getBlock() != Blocks.AIR) {
                        PlayerStructureSelectionHelper.toggleInSelection(player, blockPos.toImmutable());
                    }
                }
                PlayerStructureSelectionHelper.sendSelection(player);
            } else {
                setNbtPos(held, pos);
            }


        }
        return EnumActionResult.SUCCESS;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
