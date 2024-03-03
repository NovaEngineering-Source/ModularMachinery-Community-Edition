package kport.gugu_utils.common.block;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.modularmachinery.ModularMachinery;
import static kport.gugu_utils.common.Constants.NAME_STARLIGHTHATCH_INPUT;
import kport.gugu_utils.common.VariantBlock;
import kport.gugu_utils.common.starlight.StarlightHatchVariant;
import kport.gugu_utils.common.starlight.TileStarlightInputHatch;
import static kport.gugu_utils.tools.ResourceUtils.TOOLTIP_PREFIX;
import static kport.gugu_utils.tools.ResourceUtils.j;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
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

public class BlockStarLightInputHatch extends VariantBlock<StarlightHatchVariant> implements ITileEntityProvider {
    public static final PropertyEnum<StarlightHatchVariant> VARIANT = PropertyEnum.create("variant", StarlightHatchVariant.class);


    public BlockStarLightInputHatch() {
        super(Material.ROCK, StarlightHatchVariant.class);
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
        return new TileStarlightInputHatch();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileStarlightInputHatch) {
            ((TileStarlightInputHatch) te).onBreak();
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flags) {
        final ItemStack attunementAltar = new ItemStack(BlocksAS.blockAltar, 1, 1);
        final ItemStack traitAltar = new ItemStack(BlocksAS.blockAltar, 1, 3);
        int starlightCap = 1000 * (2 << (stack.getMetadata() * 2));
        StarlightHatchVariant variant = VariantBlock.getVariant(stack, StarlightHatchVariant.class);
        addInformationLocalized(tooltip, j(TOOLTIP_PREFIX, ModularMachinery.MODID, NAME_STARLIGHTHATCH_INPUT), starlightCap);
        addInformationLocalized(tooltip, j(TOOLTIP_PREFIX, ModularMachinery.MODID, NAME_STARLIGHTHATCH_INPUT, "ext", variant.getName()),
                variant == StarlightHatchVariant.BASIC ? attunementAltar.getDisplayName() : traitAltar.getDisplayName());
    }


    @Override
    public PropertyEnum<StarlightHatchVariant> getVariant() {
        return VARIANT;
    }

}
