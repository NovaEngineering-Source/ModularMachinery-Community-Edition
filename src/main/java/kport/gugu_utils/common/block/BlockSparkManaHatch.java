package kport.gugu_utils.common.block;

import hellfirepvp.modularmachinery.ModularMachinery;
import static kport.gugu_utils.common.Constants.NAME_MANA;
import kport.gugu_utils.common.VariantBlock;
import kport.gugu_utils.common.IOHatchVariant;
import kport.gugu_utils.common.tile.TileSparkManaHatch;
import kport.gugu_utils.common.tile.TileSparkManaInputHatch;
import kport.gugu_utils.common.tile.TileSparkManaOutputHatch;
import static kport.gugu_utils.tools.ResourceUtils.TOOLTIP_PREFIX;
import static kport.gugu_utils.tools.ResourceUtils.j;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.wand.IWandHUD;
import vazkii.botania.api.wand.IWandable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Optional.Interface(iface = "vazkii.botania.api.wand.IWandHUD", modid = "botania")
@Optional.Interface(iface = "vazkii.botania.api.wand.IWandable",modid = "botania")
public class BlockSparkManaHatch extends VariantBlock<IOHatchVariant> implements ITileEntityProvider, IWandHUD, IWandable {

    public static final PropertyEnum<IOHatchVariant> VARIANT = PropertyEnum.create("variant", IOHatchVariant.class);


    public BlockSparkManaHatch() {
        super(Material.ROCK, IOHatchVariant.class);
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
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileSparkManaHatch hatch = (TileSparkManaHatch) world.getTileEntity(pos);
        if (hatch != null) {
            return TileSparkManaHatch.calculateComparatorLevel(hatch.getCurrentMana(), TileSparkManaHatch.MAX_MANA);
        }
        return 0;
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
        if (meta == IOHatchVariant.OUTPUT.ordinal()) {
            return new TileSparkManaOutputHatch();
        } else {
            return new TileSparkManaInputHatch();
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flags) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            int mana = tagCompound.getInteger(NAME_MANA);
            addInformationLocalized(tooltip, j(TOOLTIP_PREFIX, ModularMachinery.MODID, "sparkmanahatch"), mana, TileSparkManaHatch.MAX_MANA);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHUD(Minecraft mc, ScaledResolution res, World world, BlockPos pos) {
        ((TileSparkManaHatch) world.getTileEntity(pos)).renderHUD(mc, res);
    }

    @Override
    public boolean onUsedByWand(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing side) {
        ((TileSparkManaHatch) world.getTileEntity(pos)).onWanded(player, stack);
        return true;
    }

    @Override
    public PropertyEnum<IOHatchVariant> getVariant() {
        return VARIANT;
    }
}
