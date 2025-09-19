package hellfirepvp.modularmachinery.common.block;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.block.prop.ParallelControllerData;
import hellfirepvp.modularmachinery.common.tiles.TileParallelController;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class BlockParallelController extends BlockMachineComponent implements BlockCustomName, BlockVariants {
    protected static final PropertyEnum<ParallelControllerData> CONTROLLER_TYPE = PropertyEnum.create("type", ParallelControllerData.class);

    public BlockParallelController() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ParallelControllerData data = ParallelControllerData.values()[MathHelper.clamp(stack.getMetadata(), 0, ParallelControllerData.values().length - 1)];
        tooltip.add(I18n.format("tile.modularmachinery.blockparallelcontroller.tip", data.getMaxParallelism()));
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (ParallelControllerData size : ParallelControllerData.values()) {
            items.add(new ItemStack(this, 1, size.ordinal()));
        }
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(CONTROLLER_TYPE).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CONTROLLER_TYPE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(CONTROLLER_TYPE, ParallelControllerData.values()[meta]);
    }

    @Override
    public String getIdentifierForMeta(int meta) {
        return getStateFromMeta(meta).getValue(CONTROLLER_TYPE).getName();
    }

    @Override
    public Iterable<IBlockState> getValidStates() {
        LinkedList<IBlockState> states = new LinkedList<>();
        for (ParallelControllerData type : ParallelControllerData.values()) {
            states.add(getDefaultState().withProperty(CONTROLLER_TYPE, type));
        }
        return states;
    }

    @Override
    public String getBlockStateName(IBlockState state) {
        return state.getValue(CONTROLLER_TYPE).getName();
    }

    @Override
    public boolean hasTileEntity() {
        return super.hasTileEntity();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return super.hasTileEntity(state);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileParallelController(state.getValue(CONTROLLER_TYPE).getMaxParallelism());
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileParallelController) {
                playerIn.openGui(ModularMachinery.MODID, CommonProxy.GuiType.PARALLEL_CONTROLLER.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }
}
