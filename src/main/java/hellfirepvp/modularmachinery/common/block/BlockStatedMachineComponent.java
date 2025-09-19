package hellfirepvp.modularmachinery.common.block;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.block.prop.WorkingState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockStatedMachineComponent extends BlockMachineComponent implements BlockVariants {
    public static final List<BlockStatedMachineComponent> WAIT_FOR_REGISTRY = new ArrayList<>();
    public static final PropertyEnum<WorkingState>        WORKING_STATE     = PropertyEnum.create("working_state", WorkingState.class);

    private boolean isColoured = false;

    public BlockStatedMachineComponent() {
        super(Material.IRON);
        setHardness(2F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }


    public boolean isOpaqueCube(IBlockState state) {
        return !Objects.equals(this.getRegistryName(), new ResourceLocation(ModularMachinery.MODID, "crushing_wheels"));
    }

    public boolean isColoured() {
        return isColoured;
    }

    public BlockStatedMachineComponent setColoured(final boolean coloured) {
        isColoured = coloured;
        return this;
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        String prefix = isColoured ? "tile" : "block";
        return I18n.format(prefix + ".modularmachinery." + getRegistryName().getPath() + ".name");
    }

    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(WORKING_STATE, WorkingState.values()[meta]);
    }

    @Override
    public int getMetaFromState(@Nonnull final IBlockState state) {
        return state.getValue(WORKING_STATE).ordinal();
    }

    @Override
    public Iterable<IBlockState> getValidStates() {
        ArrayList<IBlockState> states = new ArrayList<>();
        for (WorkingState type : WorkingState.values()) {
            states.add(getDefaultState().withProperty(WORKING_STATE, type));
        }
        return states;
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, WORKING_STATE);
    }

    @Override
    public String getBlockStateName(final IBlockState state) {
        return state.getValue(WORKING_STATE).getName();
    }

    @Override
    public boolean hasTileEntity(final IBlockState state) {
        return isColoured;
    }

    @Override
    public boolean hasTileEntity() {
        return isColoured;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return isColoured ? super.createTileEntity(world, state) : null;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        return isColoured ? super.createNewTileEntity(worldIn, meta) : null;
    }
}
