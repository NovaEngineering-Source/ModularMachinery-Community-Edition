/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.item.ItemBlockController;
import hellfirepvp.modularmachinery.common.item.ItemDynamicColor;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockController
 * Created by HellFirePvP
 * Date: 28.06.2017 / 20:48
 */
@SuppressWarnings("deprecation")
public class BlockController extends BlockMachineComponent implements ItemDynamicColor {
    public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);
    public static final PropertyBool FORMED = PropertyBool.create("formed");

    public static final Map<DynamicMachine, BlockController> MACHINE_CONTROLLERS = new HashMap<>();
    public static final Map<DynamicMachine, BlockController> MOC_MACHINE_CONTROLLERS = new HashMap<>();

    protected DynamicMachine parentMachine = null;

    public BlockController() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(FORMED, false));
    }

    public BlockController(DynamicMachine parentMachine) {
        this();
        this.parentMachine = parentMachine;
        setRegistryName(new ResourceLocation(
                ModularMachinery.MODID, parentMachine.getRegistryName().getPath() + "_controller")
        );
    }

    public BlockController(String namespace, DynamicMachine parentMachine) {
        this();
        this.parentMachine = parentMachine;
        setRegistryName(new ResourceLocation(
                namespace, parentMachine.getRegistryName().getPath() + "_controller")
        );
    }

    public static BlockController getControllerWithMachine(DynamicMachine machine) {
        return MACHINE_CONTROLLERS.get(machine);
    }

    public static BlockController getMocControllerWithMachine(DynamicMachine machine) {
        return MOC_MACHINE_CONTROLLERS.get(machine);
    }

    public DynamicMachine getParentMachine() {
        return parentMachine;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (this.getRegistryName().getNamespace().equals("modularcontroller") && !Config.disableMocDeprecatedTip) {
            tooltip.add(I18n.format("tile.modularmachinery.machinecontroller.deprecated.tip.0"));
            tooltip.add(I18n.format("tile.modularmachinery.machinecontroller.deprecated.tip.1"));
        }
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> result, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState metadata, int fortune) {
        ItemStack stack = getRestorableDropItem(world, pos, metadata);
        if (stack != null && !stack.isEmpty()) {
            result.add(stack);
        } else {
            super.getDrops(result, world, pos, metadata, fortune);
        }
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        ItemStack stack = getRestorableDropItem(world, pos, state);
        if (stack != null && !stack.isEmpty()) {
            return stack;
        } else {
            return super.getPickBlock(state, target, world, pos, player);
        }
    }

    private ItemStack getRestorableDropItem(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        Random rand = world instanceof World ? ((World)world).rand : RANDOM;
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileMultiblockMachineController ctrl && ctrl.getOwner() != null) {
            UUID ownerUUID = ctrl.getOwner();
            Item dropped = getItemDropped(state, rand, damageDropped(state));
            if (dropped instanceof ItemBlockController) {
                ItemStack stackCtrl = new ItemStack(dropped, 1);
                if (ownerUUID != null) {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setString("owner", ownerUUID.toString());
                    stackCtrl.setTagCompound(tag);
                }
                return stackCtrl;
            } else {
                ModularMachinery.log.warn("Cannot get controller drops at World: " + world + ", Pos: " + MiscUtils.posToString(pos));
            }
        }
        return null;
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, TileEntity te, @Nonnull ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.setBlockToAir(pos);
    }

    @Override
    public void breakBlock(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileMultiblockMachineController ctrl) {
            IOInventory inv = ctrl.getInventory();
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    spawnAsEntity(worldIn, pos, stack);
                    inv.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void neighborChanged(@Nonnull final IBlockState state, final World world, @Nonnull final BlockPos pos, @Nonnull final Block blockIn, @Nonnull final BlockPos fromPos) {
        if (world.getTileEntity(pos) instanceof TileMultiblockMachineController ctrl) {
            ctrl.onNeighborChange();
        }
    }

    @Override
    public boolean canConnectRedstone(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        return true;
    }

    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(@Nonnull final IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean onBlockActivated(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileMachineController) {
                playerIn.openGui(ModularMachinery.MODID, CommonProxy.GuiType.CONTROLLER.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Nonnull
    @Override
    public IBlockState getActualState(@Nonnull final IBlockState state, @Nonnull final IBlockAccess worldIn, @Nonnull final BlockPos pos) {
        return worldIn.getTileEntity(pos) instanceof TileMultiblockMachineController ctrl ? state.withProperty(FORMED, ctrl.isStructureFormed()) : state;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, FORMED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasComparatorInputOverride(@Nonnull IBlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getComparatorInputOverride(@Nonnull IBlockState blockState, World worldIn, @Nonnull BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof final TileMultiblockMachineController ctrl) {
            return ctrl.isWorking() ? 15 : ctrl.getFoundMachine() != null ? 1 : 0;
        }
        return 0;
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
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileMachineController(state);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileMachineController(getStateFromMeta(meta));
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        if (parentMachine != null) {
            return I18n.format("tile.modularmachinery.machinecontroller.name", parentMachine.getLocalizedName());
        }
        return I18n.format("tile.modularmachinery.blockcontroller.name");
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return parentMachine == null || parentMachine.getControllerBoundingBox().equals(FULL_BLOCK_AABB);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return isFullBlock(state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isTranslucent(IBlockState state) {
        return isFullBlock(state);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return parentMachine != null ? parentMachine.getControllerBoundingBox() : FULL_BLOCK_AABB;
    }

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        if (parentMachine == null) return Config.machineColor;
        return parentMachine.getMachineColor();
    }

    @Override
    public int getColorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        if (parentMachine == null) return Config.machineColor;
        return parentMachine.getMachineColor();
    }
}
