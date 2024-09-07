package hellfirepvp.modularmachinery.common.tiles;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public class TileParallelController extends TileColorableMachineComponent implements MachineComponentTile {
    private final ParallelControllerProvider provider = new ParallelControllerProvider();
    private int maxParallelism = 1;
    private int parallelism = 1;

    public TileParallelController(int maxParallelism) {
        this.maxParallelism = maxParallelism;
        this.parallelism = maxParallelism;
    }

    public TileParallelController() {
    }

    @Nonnull
    @Override
    public ParallelControllerProvider provideComponent() {
        return provider;
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);
        if (compound.hasKey("maxParallelism")) {
            maxParallelism = compound.getInteger("maxParallelism");
        }
        if (compound.hasKey("parallelism")) {
            parallelism = compound.getInteger("parallelism");
            if (parallelism > maxParallelism) {
                parallelism = maxParallelism;
            }
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setInteger("maxParallelism", maxParallelism);
        compound.setInteger("parallelism", parallelism);
    }

    public class ParallelControllerProvider extends MachineComponent<ParallelControllerProvider> {

        private ParallelControllerProvider() {
            super(IOType.INPUT);
        }

        public int getParallelism() {
            return parallelism;
        }

        public void setParallelism(int newParallelism) {
            parallelism = newParallelism;
            markForUpdateSync();
        }

        public int getMaxParallelism() {
            return maxParallelism;
        }

        @Override
        public ComponentType getComponentType() {
            return ComponentTypesMM.COMPONENT_PARALLEL_CONTROLLER;
        }

        @Override
        public ParallelControllerProvider getContainerProvider() {
            return this;
        }
    }
}
