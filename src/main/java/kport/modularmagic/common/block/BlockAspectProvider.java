package kport.modularmagic.common.block;

import hellfirepvp.modularmachinery.common.block.BlockMachineComponent;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.blocks.ILabelable;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.items.consumables.ItemPhial;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.tiles.essentia.TileJarFillable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Optional.Interface(iface = "thaumcraft.api.blocks.ILabelable", modid = "thaumcraft")
public abstract class BlockAspectProvider extends BlockMachineComponent implements ILabelable {
    public BlockAspectProvider(final Material materialIn) {
        super(materialIn);
    }

    @Override
    @Optional.Method(modid = "thaumcraft")
    public boolean applyLabel(EntityPlayer player, BlockPos pos, EnumFacing enumFacing, ItemStack labelstack) {
        TileEntity te = player.world.getTileEntity(pos);
        if (enumFacing == EnumFacing.DOWN || enumFacing == EnumFacing.UP) {
            return false;
        }

        if (te instanceof TileJarFillable && ((TileJarFillable) te).aspectFilter == null) {
            if (((TileJarFillable) te).amount == 0 && ((IEssentiaContainerItem) labelstack.getItem()).getAspects(labelstack) == null) {
                return false;
            } else {
                if (((TileJarFillable) te).amount == 0 && ((IEssentiaContainerItem) labelstack.getItem()).getAspects(labelstack) != null) {
                    ((TileJarFillable) te).aspect = ((IEssentiaContainerItem) labelstack.getItem()).getAspects(labelstack).getAspects()[0];
                }

                this.onBlockPlacedBy(player.world, pos, player.world.getBlockState(pos), player, (ItemStack) null);
                ((TileJarFillable) te).aspectFilter = ((TileJarFillable) te).aspect;
                ((TileJarFillable) te).facing = enumFacing.getIndex();
                player.world.markAndNotifyBlock(pos, player.world.getChunk(pos), player.world.getBlockState(pos), player.world.getBlockState(pos), 3);
                te.markDirty();
                player.world.playSound((EntityPlayer) null, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundsTC.jar, SoundCategory.BLOCKS, 0.4F, 1.0F);
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    @Optional.Method(modid = "thaumcraft")
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileJarFillable && !((TileJarFillable) te).blocked && player.getHeldItem(hand).getItem() == ItemsTC.jarBrace) {
            ((TileJarFillable) te).blocked = true;
            player.getHeldItem(hand).shrink(1);
            if (world.isRemote) {
                world.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundsTC.key, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            } else {
                te.markDirty();
            }
        } else if (te instanceof TileJarFillable && player.isSneaking() && ((TileJarFillable) te).aspectFilter != null && side.ordinal() == ((TileJarFillable) te).facing) {
            ((TileJarFillable) te).aspectFilter = null;
            if (world.isRemote) {
                world.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundsTC.page, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            } else {
                world.spawnEntity(new EntityItem(world, (double) ((float) pos.getX() + 0.5F + (float) side.getXOffset() / 3.0F), (double) ((float) pos.getY() + 0.5F), (double) ((float) pos.getZ() + 0.5F + (float) side.getZOffset() / 3.0F), new ItemStack(ItemsTC.label)));
            }
            return true;
        } else if (te instanceof TileJarFillable && player.isSneaking() && player.getHeldItem(hand).isEmpty() && ((TileJarFillable) te).aspectFilter == null) {
            ((TileJarFillable) te).aspect = null;
            if (world.isRemote) {
                world.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundsTC.jar, SoundCategory.BLOCKS, 0.4F, 1.0F, false);
                world.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false);
            } else {
                AuraHelper.polluteAura(world, pos, (float) ((TileJarFillable) te).amount, true);
            }
            ((TileJarFillable) te).amount = 0;
            te.markDirty();
            return true;
        } else if (te instanceof TileJarFillable && player.getHeldItem(hand).getItem() instanceof ItemPhial) {
            Aspect aspect;
            TileJarFillable tile = (TileJarFillable) world.getTileEntity(pos);
            ItemStack phialStack = player.getHeldItem(hand);
            ItemPhial phial = (ItemPhial) phialStack.getItem();
            if (phial.getAspects(phialStack) == null && tile.amount >= 10) {
                if (world.isRemote) {
                    player.swingArm(hand);
                    return true;
                }

                aspect = Aspect.getAspect(tile.aspect.getTag());
                if (tile.takeFromContainer(aspect, 10)) {
                    player.getHeldItem(hand).shrink(1);
                    ItemStack phial1 = new ItemStack(ItemsTC.phial, 1, 1);
                    ((ItemPhial) phial1.getItem()).setAspects(phial1, (new AspectList()).add(aspect, 10));
                    if (!player.inventory.addItemStackToInventory(phial1)) {
                        world.spawnEntity(new EntityItem(world, (double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F), (double) ((float) pos.getZ() + 0.5F), phial1));
                    }

                    player.playSound(SoundEvents.ITEM_BOTTLE_FILL, 0.25F, 1.0F);
                    player.inventoryContainer.detectAndSendChanges();
                    return true;
                }
            } else if (phial.getAspects(phialStack) != null && phial.getAspects(phialStack).size() == 1) {
                aspect = phial.getAspects(phialStack).getAspects()[0];
                if (tile.amount <= 250 - 10 && tile.doesContainerAccept(aspect)) {
                    if (world.isRemote) {
                        player.swingArm(hand);
                        return true;
                    }

                    if (tile.addToContainer(aspect, 10) == 0) {
                        world.markAndNotifyBlock(pos, world.getChunk(pos), this.getDefaultState(), this.getDefaultState(), 3);
                        tile.markDirty();
                        player.getHeldItem(hand).shrink(1);
                        if (!player.inventory.addItemStackToInventory(new ItemStack(ItemsTC.phial, 1, 0))) {
                            world.spawnEntity(new EntityItem(world, (double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F), (double) ((float) pos.getZ() + 0.5F), new ItemStack(this, 1, 0)));
                        }
                        player.playSound(SoundEvents.ITEM_BOTTLE_FILL, 0.25F, 1.0F);
                        player.inventoryContainer.detectAndSendChanges();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) {
        return false;
    }
}
