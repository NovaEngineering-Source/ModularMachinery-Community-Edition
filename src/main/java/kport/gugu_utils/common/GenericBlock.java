package kport.gugu_utils.common;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class GenericBlock extends Block {
    private static final Pattern COMPILE = Pattern.compile("@", Pattern.LITERAL);

    public GenericBlock(Material materialIn) {
        super(materialIn);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    protected void addInformationLocalized(List<String> tooltip, String key, Object... parameters) {
        String translated = I18n.format(key, parameters);
        translated = COMPILE.matcher(translated).replaceAll("\u00a7");
        Collections.addAll(tooltip, translated.split("\n"));
    }


    @Override
    public void getDrops(NonNullList<ItemStack> result, IBlockAccess world, BlockPos pos, IBlockState metadata, int fortune) {
        TileEntity tileEntity = world.getTileEntity(pos);

        // Always check this!
        if (tileEntity instanceof IRestorableTileEntity) {
            ItemStack stack = new ItemStack(this, 1, this.damageDropped(metadata));
            NBTTagCompound tagCompound = new NBTTagCompound();
            ((IRestorableTileEntity) tileEntity).writeRestorableToNBT(tagCompound);
            stack.setTagCompound(tagCompound);
            result.add(stack);
        } else {
            super.getDrops(result, world, pos, metadata, fortune);
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (willHarvest) {
            return true; // If it will harvest, delay deletion of the block until after getDrops
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.setBlockToAir(pos);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        TileEntity tileEntity = world.getTileEntity(pos);

        // Always check this!
        if (tileEntity instanceof IRestorableTileEntity) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound != null) {
                ((IRestorableTileEntity) tileEntity).readRestorableFromNBT(tagCompound);
            }
        }

    }


    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof IGuiProvider)) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        player.openGui(ModularMachinery.MODID, CommonProxy.GuiType.GUI_STARLIGHT_PROVIDER.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

}
