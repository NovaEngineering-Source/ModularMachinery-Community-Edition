package com.cleanroommc.client.util;

import com.cleanroommc.client.util.world.DummyWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.vecmath.Vector3f;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/25
 * @Description: TrackedDummyWorld. Used to build a Fake World.
 */
@SideOnly(Side.CLIENT)
public class TrackedDummyWorld extends DummyWorld {

    public final Set<BlockPos> renderedBlocks = new HashSet<>();
    private Predicate<BlockPos> renderFilter;
    private BiFunction<BlockPos, IBlockState, IBlockState> hookBlockState;
    private BiFunction<BlockPos, TileEntity, TileEntity> hookTileEntity;
    public final World proxyWorld;

    private final Vector3f minPos = new Vector3f(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final Vector3f maxPos = new Vector3f(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    public void setRenderFilter(Predicate<BlockPos> renderFilter) {
        this.renderFilter = renderFilter;
    }

    public void setBlockStateHook(BiFunction<BlockPos, IBlockState, IBlockState> hookBlockState) {
        this.hookBlockState = hookBlockState;
    }

    public void setTileEntityHook(BiFunction<BlockPos, TileEntity, TileEntity> hookTileEntity) {
        this.hookTileEntity = hookTileEntity;
    }

    public TrackedDummyWorld(){
        proxyWorld = null;
    }

    public TrackedDummyWorld(World world){
        proxyWorld = world;
    }

    public void addBlocks(Map<BlockPos, BlockInfo> renderedBlocks) {
        renderedBlocks.forEach(this::addBlock);
    }

    public void addBlock(BlockPos pos, BlockInfo blockInfo) {
        if (blockInfo.getBlockState().getBlock() == Blocks.AIR)
            return;
        this.renderedBlocks.add(pos);
        blockInfo.apply(this, pos);
    }

    public void removeBlock(BlockPos pos) {
        this.renderedBlocks.remove(pos);
    }

    @Override
    public TileEntity getTileEntity(@Nonnull BlockPos pos) {
        if (renderFilter != null && !renderFilter.test(pos))
            return null;
        TileEntity tileEntity = proxyWorld != null ? proxyWorld.getTileEntity(pos) : super.getTileEntity(pos);
        if (hookTileEntity != null) {
            return hookTileEntity.apply(pos, tileEntity);
        }
        return tileEntity;
    }

    @Nonnull
    @Override
    public IBlockState getBlockState(@Nonnull BlockPos pos) {
        if (renderFilter != null && !renderFilter.test(pos))
            return Blocks.AIR.getDefaultState(); //return air if not rendering this block
        IBlockState blockState =  proxyWorld != null ? proxyWorld.getBlockState(pos) : super.getBlockState(pos);
        if (hookBlockState != null) {
            return hookBlockState.apply(pos, blockState);
        }
        return blockState;
    }

    @Override
    public boolean setBlockState(@Nonnull BlockPos pos, @Nonnull IBlockState newState, int flags) {
        minPos.setX(Math.min(minPos.getX(), pos.getX()));
        minPos.setY(Math.min(minPos.getY(), pos.getY()));
        minPos.setZ(Math.min(minPos.getZ(), pos.getZ()));
        maxPos.setX(Math.max(maxPos.getX(), pos.getX()));
        maxPos.setY(Math.max(maxPos.getY(), pos.getY()));
        maxPos.setZ(Math.max(maxPos.getZ(), pos.getZ()));
        return super.setBlockState(pos, newState, flags);
    }

    public Vector3f getSize() {
        Vector3f result = new Vector3f();
        result.setX(maxPos.getX() - minPos.getX() + 1);
        result.setY(maxPos.getY() - minPos.getY() + 1);
        result.setZ(maxPos.getZ() - minPos.getZ() + 1);
        return result;
    }

    public Vector3f getMinPos() {
        return minPos;
    }

    public Vector3f getMaxPos() {
        return maxPos;
    }
}
