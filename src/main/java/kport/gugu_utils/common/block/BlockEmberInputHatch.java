package kport.gugu_utils.common.block;

import kport.gugu_utils.common.VariantBlock;
import kport.gugu_utils.common.embers.EmbersHatchVariant;
import kport.gugu_utils.common.tile.TileEmberInputHatch;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import teamroots.embers.Embers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

public class BlockEmberInputHatch extends VariantBlock<EmbersHatchVariant> implements ITileEntityProvider {
    public static final PropertyEnum<EmbersHatchVariant> VARIANT = PropertyEnum.create("variant", EmbersHatchVariant.class);
    private static final String KEY_EMBER_TOOLTIP = "embers.tooltip.item.ember";

    public BlockEmberInputHatch() {
        super(Material.ROCK, EmbersHatchVariant.class);
        setHardness(2.0F);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    public PropertyEnum<EmbersHatchVariant> getVariant() {
        return VARIANT;
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
        return new TileEmberInputHatch();
    }


    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            double ember = tagCompound.getDouble("ember");
            double emberCapacity = tagCompound.getDouble("emberCapacity");
            DecimalFormat emberFormat = Embers.proxy.getDecimalFormat("embers.decimal_format.ember");
            addInformationLocalized(tooltip, KEY_EMBER_TOOLTIP, emberFormat.format(ember), emberFormat.format(emberCapacity));
        } else if (stack.getMetadata() < EmbersHatchVariant.VAULES.length) {
            EmbersHatchVariant variant = EmbersHatchVariant.VAULES[stack.getMetadata()];
            double emberCapacity = variant.getEmberMaxStorage();
            DecimalFormat emberFormat = Embers.proxy.getDecimalFormat("embers.decimal_format.ember");
            addInformationLocalized(tooltip, KEY_EMBER_TOOLTIP, emberFormat.format(0), emberFormat.format(emberCapacity));
        }
    }


}
