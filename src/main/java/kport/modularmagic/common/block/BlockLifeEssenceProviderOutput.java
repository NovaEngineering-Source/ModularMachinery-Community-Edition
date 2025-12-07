package kport.modularmagic.common.block;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.block.BlockMachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.TileInventory;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import kport.modularmagic.common.tile.TileLifeEssenceProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class BlockLifeEssenceProviderOutput extends BlockMachineComponent {

    public BlockLifeEssenceProviderOutput() {
        super(Material.IRON);
        setHardness(2F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
        setLightLevel(8);
    }

    @Override
    public void breakBlock(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileInventory) {
            IOInventory inv = ((TileInventory) te).getInventory();
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
    public boolean onBlockActivated(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileLifeEssenceProvider) {
                playerIn.openGui(ModularMachinery.instance, CommonProxy.GuiType.GUI_ESSENCE_PROVIDER.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    @NotNull
    @Override
    public EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return super.hasTileEntity(state);
    }


    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileLifeEssenceProvider.Output();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }
}
