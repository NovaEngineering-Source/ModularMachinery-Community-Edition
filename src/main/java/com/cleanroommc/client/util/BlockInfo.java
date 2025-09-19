package com.cleanroommc.client.util;

import com.cleanroommc.client.util.world.DummyWorld;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockInfo {

    public static final BlockInfo EMPTY = new BlockInfo(Blocks.AIR);

    private final IBlockState    blockState;
    private final ItemStack      itemStack;
    private       TileEntity     tileEntity;
    private       NBTTagCompound teTag;

    public BlockInfo(Block block) {
        this(block.getDefaultState());
    }

    public BlockInfo(IBlockState blockState) {
        this(blockState, null);
    }

    public BlockInfo(IBlockState blockState, TileEntity tileEntity) {
        this(blockState, tileEntity, null);
    }

    public BlockInfo(IBlockState blockState, TileEntity tileEntity, ItemStack itemStack) {
        this(blockState, tileEntity, itemStack, null);
    }

    public BlockInfo(final IBlockState blockState, final TileEntity tileEntity, final ItemStack itemStack, final NBTTagCompound teTag) {
        this.blockState = blockState;
        this.tileEntity = tileEntity;
        this.itemStack = itemStack;
        this.teTag = teTag;
    }

    public static BlockInfo fromBlockState(IBlockState state) {
        try {
            if (state.getBlock().hasTileEntity(state)) {
                TileEntity tileEntity = state.getBlock().createTileEntity(new DummyWorld(), state);
                if (tileEntity != null) {
                    return new BlockInfo(state, tileEntity);
                }
            }
        } catch (Exception ignored) {
        }
        return new BlockInfo(state);
    }

    public IBlockState getBlockState() {
        return blockState;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public void setTileEntity(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    public ItemStack getItemStackForm() {
        return itemStack == null ? new ItemStack(Item.getItemFromBlock(blockState.getBlock()), 1, blockState.getBlock().damageDropped(blockState)) : itemStack;
    }

    public NBTTagCompound getTeTag() {
        return teTag;
    }

    public BlockInfo setTeTag(final NBTTagCompound teTag) {
        this.teTag = teTag;
        return this;
    }

    public void apply(World world, BlockPos pos) {
        world.setBlockState(pos, blockState);
        if (tileEntity == null) {
            return;
        }
        try {
            world.setTileEntity(pos, tileEntity);
        } catch (Throwable e) {
            return;
        }
        if (teTag == null) {
            return;
        }
        try {
            tileEntity.readFromNBT(teTag);
        } catch (Throwable e) {
            ModularMachinery.log.warn("Failed to apply NBT to TileEntity!", e);
        }
    }
}
