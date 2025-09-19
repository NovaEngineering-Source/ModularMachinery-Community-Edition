package kport.modularmagic.common.tile;

import WayofTime.bloodmagic.core.data.Binding;
import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.core.data.SoulTicket;
import WayofTime.bloodmagic.item.ItemBindableBase;
import WayofTime.bloodmagic.orb.BloodOrb;
import WayofTime.bloodmagic.orb.IBloodOrb;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileInventory;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentLifeEssenceProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileLifeEssenceProvider extends TileInventory implements MachineComponentTile {
    protected volatile int lifeEssenceCache = 0;

    public TileLifeEssenceProvider() {
        super(1);
    }

    public SoulNetwork getSoulNetwork() {
        ItemStack stack = getInventory().getStackInSlot(0);
        if (!stack.isEmpty() && stack.getItem() instanceof final ItemBindableBase bloodOrb) {
            Binding binding = bloodOrb.getBinding(stack);
            if (binding != null) {
                return NetworkHelper.getSoulNetwork(binding);
            }
        }
        return null;
    }

    public int getOrbCapacity() {
        ItemStack stack = getInventory().getStackInSlot(0);
        if (!stack.isEmpty() && stack.getItem() instanceof IBloodOrb) {
            BloodOrb orb = ((IBloodOrb) stack.getItem()).getOrb(stack);
            return orb == null ? Integer.MIN_VALUE : orb.getCapacity();
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public IOInventory buildInventory(TileInventory tile, int size) {
        return new IOInventory(tile, new int[1], new int[1]) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() instanceof IBloodOrb;
            }
        };
    }

    public int getLifeEssenceCache() {
        return lifeEssenceCache;
    }

    public abstract int addLifeEssenceCache(int amount);

    public abstract int removeLifeEssenceCache(int amount);

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);
        this.lifeEssenceCache = compound.getInteger("lifeEssenceCache");
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setInteger("lifeEssenceCache", lifeEssenceCache);
    }

    public static class Input extends TileLifeEssenceProvider {

        @Override
        public synchronized void doRestrictedTick() {
            if (world.isRemote) {
                return;
            }

            SoulNetwork soulNetwork = getSoulNetwork();
            if (soulNetwork == null) {
                return;
            }

            int orbCapacity = getOrbCapacity();
            if (orbCapacity <= 0) {
                return;
            }

            int maxCapacity = orbCapacity / 10;

            if (lifeEssenceCache >= maxCapacity) {
                return;
            }

            int prev = lifeEssenceCache;
            lifeEssenceCache += soulNetwork.syphon(new SoulTicket(Math.min(maxCapacity - lifeEssenceCache, soulNetwork.getCurrentEssence())));
            if (prev != lifeEssenceCache) {
                markNoUpdate();
            }
        }

        @Override
        public int addLifeEssenceCache(final int amount) {
            return 0;
        }

        @Override
        public synchronized int removeLifeEssenceCache(int amount) {
            int maxCanConsume = Math.min(lifeEssenceCache, amount);
            lifeEssenceCache -= maxCanConsume;
            markNoUpdateSync();
            return maxCanConsume;
        }

        @Nullable
        @Override
        public MachineComponentLifeEssenceProvider provideComponent() {
            return new MachineComponentLifeEssenceProvider(this, IOType.INPUT);
        }
    }

    public static class Output extends TileLifeEssenceProvider {

        @Override
        public synchronized void doRestrictedTick() {
            if (world.isRemote) {
                return;
            }

            SoulNetwork soulNetwork = getSoulNetwork();
            if (soulNetwork == null) {
                return;
            }

            int prev = lifeEssenceCache;
            lifeEssenceCache -= soulNetwork.add(new SoulTicket(lifeEssenceCache), getOrbCapacity());
            if (prev != lifeEssenceCache) {
                markNoUpdate();
            }
        }

        @Override
        public synchronized int addLifeEssenceCache(int amount) {
            int orbCapacity = getOrbCapacity();
            if (orbCapacity <= 0) {
                return 0;
            }

            int maxCanAdd = orbCapacity - lifeEssenceCache;
            if (maxCanAdd <= 0) {
                return 0;
            }

            int added = Math.min(amount, maxCanAdd);
            lifeEssenceCache += added;
            markNoUpdateSync();
            return added;
        }

        @Override
        public int removeLifeEssenceCache(final int amount) {
            return 0;
        }

        @Nullable
        @Override
        public MachineComponentLifeEssenceProvider provideComponent() {
            return new MachineComponentLifeEssenceProvider(this, IOType.OUTPUT);
        }
    }
}
