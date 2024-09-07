package kport.modularmagic.common.tile;

import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentManaProvider;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.ManaNetworkEvent;
import vazkii.botania.common.core.handler.ManaNetworkHandler;

public abstract class TileManaProvider extends TileColorableMachineComponent implements ITickable, IManaReceiver, MachineComponentTile {

    private static int manaCapacity = 100000;
    private volatile int mana = 0;

    public static void loadFromConfig(Configuration cfg) {
        manaCapacity = cfg.getInt("Mana Capacity", "mods.botania", manaCapacity, 0, Integer.MAX_VALUE, 
                "The maximum amount of mana this mana provider can hold."
        );
    }

    @Override
    public void update() {

    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);
        mana = compound.getInteger("mana");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setInteger("mana", mana);
    }

    @Override
    public int getCurrentMana() {
        return mana;
    }

    @Override
    public boolean isFull() {
        return mana >= getManaCapacity();
    }

    @Override
    public synchronized void recieveMana(int amount) {
        mana = MathHelper.clamp(mana + amount, 0, getManaCapacity());
        markNoUpdateSync();
    }

    public synchronized void reduceMana(int amount) {
        mana = MathHelper.clamp(mana - amount, 0, getManaCapacity());
        markNoUpdateSync();
    }

    public int getManaCapacity() {
        return manaCapacity;
    }

    @Override
    public boolean canRecieveManaFromBursts() {
        return false;
    }

    public static class Input extends TileManaProvider {

        @Override
        public MachineComponentManaProvider provideComponent() {
            return new MachineComponentManaProvider(IOType.INPUT, this);
        }

        @Override
        public boolean canRecieveManaFromBursts() {
            return true;
        }
    }

    public static class Output extends TileManaProvider implements IManaPool {

        @Override
        public void update() {
            if (!ManaNetworkHandler.instance.isPoolIn(this) && !isInvalid()) {
                ManaNetworkEvent.addPool(this);
            }
        }

        @Override
        public void invalidate() {
            super.invalidate();
            ManaNetworkEvent.removePool(this);
        }

        @Override
        public void onChunkUnload() {
            super.onChunkUnload();
            ManaNetworkEvent.removePool(this);
        }

        @Override
        public boolean isOutputtingPower() {
            return true;
        }

        @Override
        public EnumDyeColor getColor() {
            return EnumDyeColor.WHITE;
        }

        @Override
        public void setColor(EnumDyeColor arg0) {
        }

        @Override
        public MachineComponentManaProvider provideComponent() {
            return new MachineComponentManaProvider(IOType.OUTPUT, this);
        }
    }

}
