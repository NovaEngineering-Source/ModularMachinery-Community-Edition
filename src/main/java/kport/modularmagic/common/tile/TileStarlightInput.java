package kport.modularmagic.common.tile;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.distribution.ConstellationSkyHandler;
import hellfirepvp.astralsorcery.common.constellation.distribution.WorldSkyHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionReceiver;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionReceiver;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionClassRegistry;
import hellfirepvp.astralsorcery.common.tile.base.TileReceiverBase;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.SkyCollectionHelper;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.tiles.base.ColorableMachineTile;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentStarlightProviderInput;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileStarlightInput extends TileReceiverBase implements MachineComponentTile, ColorableMachineTile {

    private int starlightAmount = 0;
    private int color           = Config.machineColor;

    @Override
    public int getMachineColor() {
        return this.color;
    }

    @Override
    public void setMachineColor(int newColor) {
        if (color == newColor) {
            return;
        }
        this.color = newColor;
        ModularMachinery.EXECUTE_MANAGER.addSyncTask(this::markForUpdate);
    }

    @Nullable
    @Override
    public MachineComponentStarlightProviderInput provideComponent() {
        return new MachineComponentStarlightProviderInput(this, IOType.INPUT);
    }

    public int getStarlightStored() {
        return starlightAmount;
    }

    public void setStarlight(int starlight) {
        starlightAmount = starlight;
    }

    @Override
    public void update() {
        super.update();

        if (!world.isRemote) {
            if (getPassiveStarlight()) {
                this.markDirty();
            }
        }
    }

    public boolean getPassiveStarlight() {
        int prev = starlightAmount;

        starlightAmount = Math.round(starlightAmount * 0.95F);
        WorldSkyHandler handler = ConstellationSkyHandler.getInstance().getWorldHandler(getWorld());
        if (world.canSeeSky(getPos().add(0, 1, 0)) && handler != null) {
            int yLevel = getPos().getY();
            if (yLevel > 40) {
                float collect = 160;

                float dstr;
                if (yLevel > 120) {
                    dstr = 1F + ((yLevel - 120) / 272F);
                } else {
                    dstr = (yLevel - 20) / 100F;
                }

                float posDistribution = SkyCollectionHelper.getSkyNoiseDistribution(world, pos);

                collect *= dstr;
                collect *= (0.6F + (0.4F * posDistribution));
                collect *= (0.2F + (0.8F * ConstellationSkyHandler.getInstance().getCurrentDaytimeDistribution(getWorld())));

                starlightAmount = Math.round(Math.min(10000, starlightAmount + collect));
            }
        }

        return prev != starlightAmount;
    }

    public void receiveStarlight(@Nullable IWeakConstellation type, double amount) {
        if (amount <= 0.001) {
            return;
        }

        this.starlightAmount = Math.min(10000, (int) (starlightAmount + (amount * 200.0D)));
        this.markDirty();
    }

    @Nullable
    @Override
    public String getUnLocalizedDisplayName() {
        return "tile.blockstarlightproviderinput.name";
    }

    @Nonnull
    @Override
    public ITransmissionReceiver provideEndpoint(BlockPos at) {
        return new TransmissionReceiverStarlightProvider(at);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setInteger("starlight", starlightAmount);
        compound.setInteger("casingColor", color);
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        starlightAmount = compound.getInteger("starlight");
        color = compound.getInteger("casingColor");
    }

    public static class TransmissionReceiverStarlightProvider extends SimpleTransmissionReceiver {

        public TransmissionReceiverStarlightProvider(BlockPos thisPos) {
            super(thisPos);
        }

        @Override
        public void onStarlightReceive(World world, boolean isChunkLoaded, IWeakConstellation type, double amount) {
            if (isChunkLoaded) {
                TileStarlightInput te = MiscUtils.getTileAt(world, getLocationPos(), TileStarlightInput.class, false);
                if (te != null) {
                    te.receiveStarlight(type, amount);
                }
            }
        }

        @Override
        public TransmissionClassRegistry.TransmissionProvider getProvider() {
            return new StarlightProviderReceiverProvider();
        }

    }

    public static class StarlightProviderReceiverProvider implements TransmissionClassRegistry.TransmissionProvider {

        @Override
        public TransmissionReceiverStarlightProvider provideEmptyNode() {
            return new TransmissionReceiverStarlightProvider(null);
        }

        @Override
        public String getIdentifier() {
            return ModularMachinery.MODID + ":TransmissionReceiverStarlightProvider";
        }
    }
}
