package hellfirepvp.modularmachinery.common.util;

import hellfirepvp.modularmachinery.common.block.BlockStatedMachineComponent;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Rotation;
import net.minecraftforge.fluids.BlockFluidBase;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class IBlockStateDescriptor {

    // TODO It may not be necessary to use.
    private static final ObjectOpenHashSet<IBlockStateDescriptor> POOL = new ObjectOpenHashSet<>();

    private final List<IBlockState> applicable;

    public IBlockStateDescriptor(IBlockState state) {
        this.applicable = Collections.singletonList(state);
    }

    public IBlockStateDescriptor(Block block) {
        if (block instanceof BlockLiquid || block instanceof BlockFluidBase) {
            this.applicable = Collections.singletonList(block.getDefaultState());
            return;
        }

        List<IBlockState> applicable = new ReferenceArrayList<>();
        IntSet usedMetas = new IntOpenHashSet();
        for (IBlockState state : block.getBlockState().getValidStates()) {
            int meta = block.getMetaFromState(state);
            if (!usedMetas.contains(meta)) {
                usedMetas.add(meta);
                applicable.add(state);
            }
        }

        this.applicable = applicable.isEmpty() ? Collections.singletonList(block.getDefaultState()) : applicable;
    }

    protected IBlockStateDescriptor(List<IBlockState> applicable) {
        this.applicable = applicable;
    }

    public static IBlockStateDescriptor of(IBlockState state) {
        return new IBlockStateDescriptor(state).canonicalize();
    }

    public static IBlockStateDescriptor of(Block block) {
        return new IBlockStateDescriptor(block).canonicalize();
    }

    public static void clearPool() {
        POOL.clear();
    }

    public IBlockStateDescriptor copy() {
        return applicable.size() == 1
            ? new IBlockStateDescriptor(applicable.get(0))
            : new IBlockStateDescriptor(new ReferenceArrayList<>(applicable));
    }

    public IBlockStateDescriptor copyRotateYCCW(final AtomicBoolean rotated) {
        List<IBlockState> applicable = new ReferenceArrayList<>();
        for (IBlockState state : this.applicable) {
            IBlockState rotatedState = state.withRotation(Rotation.COUNTERCLOCKWISE_90);
            if (state != rotatedState) {
                rotated.set(true);
            }
            applicable.add(rotatedState);
        }
        return applicable.size() == 1
            ? new IBlockStateDescriptor(applicable.get(0)).canonicalize()
            : new IBlockStateDescriptor(applicable).canonicalize();
    }

    public boolean hasTileEntity() {
        for (IBlockState state : applicable) {
            if (state.getBlock().hasTileEntity(state)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasStatedMachineComponent() {
        for (final IBlockState state : applicable) {
            if (state.getBlock() instanceof BlockStatedMachineComponent) {
                return true;
            }
        }
        return false;
    }

    public List<IBlockState> getApplicable() {
        return applicable;
    }

    public IBlockStateDescriptor canonicalize() {
        synchronized (POOL) {
            return POOL.addOrGet(this);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof IBlockStateDescriptor && applicable.equals(((IBlockStateDescriptor) obj).applicable);
    }

    @Override
    public int hashCode() {
        return applicable.hashCode();
    }

}
