package kport.gugu_utils.common.block;

import hellfirepvp.modularmachinery.ModularMachinery;
import static kport.gugu_utils.common.Constants.NAME_ENVIRONMENTHATCH;
import static kport.gugu_utils.common.Constants.RESOURCE_ENVIRONMENTHATCH;
import kport.gugu_utils.common.GenericBlock;
import kport.gugu_utils.common.tile.TileEnvironmentHatch;
import static kport.gugu_utils.tools.ResourceUtils.j;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockEnvironmentHatch extends GenericBlock implements ITileEntityProvider {

    public BlockEnvironmentHatch() {
        super(Material.ROCK);
        setHardness(2.0F);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 0);
    }


    @Override
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
        worldIn.markBlockRangeForRenderUpdate(pos, pos);
        return true;
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEnvironmentHatch();
    }


    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {

    }


}
