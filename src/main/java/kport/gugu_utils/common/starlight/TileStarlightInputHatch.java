package kport.gugu_utils.common.starlight;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
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
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import kport.gugu_utils.common.block.BlockStarLightInputHatch;
import kport.gugu_utils.common.IGuiProvider;
import kport.gugu_utils.common.components.GenericMachineCompoment;
import kport.gugu_utils.GuGuCompoments;
import kport.gugu_utils.IColorableTileEntity;
import kport.gugu_utils.common.requirements.RequirementStarlight;
import kport.gugu_utils.common.requirements.basic.IConsumable;
import static kport.gugu_utils.tools.ResourceUtils.j;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileStarlightInputHatch extends TileReceiverBase implements IColorableTileEntity,
    IGuiProvider, MachineComponentTile, IConsumable<RequirementStarlight.RT> {
    public static final String KEY_MACHINE_COLOR = "machine_color";
    public static final String KEY_STARLIGHT_STORED = "starlight_stored";
    public static final String KEY_CAN_SEE_SKY = "can_see_sky";

    protected int machineColor = hellfirepvp.modularmachinery.common.data.Config.machineColor;
    private int starlightStored;
    private IConstellation focusedConstellation;
    private int lastRequirement = -1;


    private float posDistribution;
    private boolean doesSeeSky;
    private int constellationTicks = -1;
    private int consumingCheckTicks = -1;

    private int starlightCap = -1;


    @Override
    public int getMachineColor() {
        return this.machineColor;
    }

    @Override
    public void setMachineColor(int newColor) {
        this.machineColor = newColor;
        //同步
        IBlockState state = world.getBlockState(this.getPos());
        world.notifyBlockUpdate(this.getPos(), state, state, 1 | 2);
        this.markDirty();
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);
        if (compound.hasKey(KEY_MACHINE_COLOR))
            this.machineColor = compound.getInteger(KEY_MACHINE_COLOR);
        if (compound.hasKey(KEY_STARLIGHT_STORED))
            this.starlightStored = compound.getInteger(KEY_STARLIGHT_STORED);
        if (compound.hasKey(KEY_CAN_SEE_SKY))
            this.doesSeeSky = compound.getBoolean(KEY_CAN_SEE_SKY);

    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        if (oldState.getBlock() != newState.getBlock())
            return true;
        if (oldState.getBlock() != BlocksMM.blockStarlightProviderInput || newState.getBlock() != BlocksMM.blockStarlightProviderInput)
            return true;
        return oldState.getValue(BlockStarLightInputHatch.VARIANT) != newState.getValue(BlockStarLightInputHatch.VARIANT);
    }
    //数据包保存及网络相关

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setInteger(KEY_MACHINE_COLOR, this.getMachineColor());
        compound.setInteger(KEY_STARLIGHT_STORED, this.getStarlightStored());
        compound.setBoolean(KEY_CAN_SEE_SKY, this.doesSeeSky);
    }

    @Nonnull
    @Override
    public ITransmissionReceiver provideEndpoint(BlockPos at) {
        return new TransmissionReceiver(at);
    }

    @Nullable
    public IConstellation getFocusedConstellation() {
        return this.focusedConstellation;
    }

    public void setFocusedConstellation(IConstellation focusedConstellation) {
        constellationTicks = 40;
        if (this.focusedConstellation == null || !this.focusedConstellation.equals(focusedConstellation)) {
            this.focusedConstellation = focusedConstellation;
        }
    }

    private void receiveStarlight(IWeakConstellation type, double amount) {
        if (amount <= 0.001) return;
        setFocusedConstellation(type);
        starlightStored = Math.min(getMaxStarlightStorage(), (int) (starlightStored + (amount * 200D)));
        markDirty();
    }

    private boolean starlightPassive() {
        boolean needUpdate = getStarlightStored() > 0;
        starlightStored *= 0.95;

        WorldSkyHandler handle = ConstellationSkyHandler.getInstance().getWorldHandler(getWorld());
        if (doesSeeSky() && handle != null) {
            int yLevel = getPos().getY();
            if (yLevel > 40) {
                float collect = 160;

                float dstr;
                if (yLevel > 120) {
                    dstr = 1F + ((yLevel - 120) / 272F);
                } else {
                    dstr = (yLevel - 20) / 100F;
                }

                if (posDistribution == -1) {
                    posDistribution = SkyCollectionHelper.getSkyNoiseDistribution(world, pos);
                }

                collect *= dstr;
                collect *= (0.6 + (0.4 * posDistribution));
                collect *= 0.2 + (0.8 * ConstellationSkyHandler.getInstance().getCurrentDaytimeDistribution(getWorld()));

                starlightStored = Math.min(getMaxStarlightStorage(), (int) (starlightStored + collect));
                return true;
            }
        }
        return needUpdate;
    }

    int getMaxStarlightStorage() {
        if (starlightCap < 0) {
            StarlightHatchVariant variant = world.getBlockState(getPos()).getValue(BlockStarLightInputHatch.VARIANT);
            starlightCap = variant.getStarlightMaxStorage();
        }
        return starlightCap;
    }


    protected void updateSkyState(boolean seesSky) {
        if (doesSeeSky != seesSky) {
            this.doesSeeSky = seesSky;
            markDirty();
        }
    }

    public boolean doesSeeSky() {
        return doesSeeSky;
    }

    @Nullable
    @Override
    public String getUnLocalizedDisplayName() {
        ItemStack hatch = new ItemStack(BlocksMM.blockStarlightProviderInput, 1, world.getBlockState(getPos()).getValue(BlockStarLightInputHatch.VARIANT).ordinal());
        return j(hatch.getTranslationKey(), "name");
    }

    //GUI相关

    public boolean canInteractWith(EntityPlayer playerIn) {
        // If we are too far away from this tile entity you cannot use it
        return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Override
    public void update() {
        if (starlightCap < 0) {
            StarlightHatchVariant variant = world.getBlockState(getPos()).getValue(BlockStarLightInputHatch.VARIANT);
            starlightCap = variant.getStarlightMaxStorage();
        }
        super.update();

        if ((ticksExisted & 15) == 0) {
            updateSkyState(MiscUtils.canSeeSky(this.getWorld(), this.getPos().offset(EnumFacing.UP), true, this.doesSeeSky));
        }

        if (!world.isRemote) {
            if (starlightPassive()) {
                markDirty();
            }

            constellationTicks--;
            if (constellationTicks <= 0) {
                setFocusedConstellation(null);
            }

            if (consumingCheckTicks > 0) {
                consumingCheckTicks--;
            } else if (lastRequirement > 0) {
                lastRequirement = -1;
            }
        }
    }

    @Override
    public boolean onSelect(EntityPlayer player) {
        return false;
    }

    public int getStarlightStored() {
        return this.starlightStored;
    }

    public void setStarlightStored(int starlightAmount) {
        if (this.starlightStored != starlightAmount) {
            this.starlightStored = starlightAmount;
            markDirty();
        }
    }

    public float getAmbientStarlightPercent() {
        return ((float) starlightStored) / ((float) getMaxStarlightStorage());
    }

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new GenericMachineCompoment<>(this, (ComponentType) GuGuCompoments.COMPONENT_STARLIGHT);
    }


    @Override
    public boolean consume(RequirementStarlight.RT outputToken, boolean doOperation) {
        if (doOperation) {
            setConsuming(true);
        }
        lastRequirement = outputToken.getStarlight();
        if (outputToken.getConstellation() != null && !outputToken.getConstellation().equals(this.getFocusedConstellation())) {
            outputToken.setError("craftcheck.failure.gugu-utils:starlight.input.wrong_constellation");
            return false;
        }
        if (outputToken.getStarlight() <= this.getStarlightStored()) {
            outputToken.setStarlight(0);
            return true;
        } else {
            return false;
        }
    }


    public void setConsuming(boolean consuming) {
        if (consuming) {
            consumingCheckTicks = 40;
        } else {
            consumingCheckTicks = -1;
        }
    }

    public int getLastRequirement() {
        return lastRequirement;
    }

    public static class TransmissionReceiver extends SimpleTransmissionReceiver {

        public TransmissionReceiver(BlockPos thisPos) {
            super(thisPos);
        }

        @Override
        public void onStarlightReceive(World world, boolean isChunkLoaded, IWeakConstellation type, double amount) {
            if (isChunkLoaded) {
                TileEntity te = world.getTileEntity(getLocationPos());
                if (te instanceof TileStarlightInputHatch) {
                    ((TileStarlightInputHatch) te).receiveStarlight(type, amount);
                }
            }
        }

        @Override
        public Provider getProvider() {
            return new Provider();
        }
    }

    public static class Provider implements TransmissionClassRegistry.TransmissionProvider {

        @Override
        public TransmissionReceiver provideEmptyNode() {
            return new TransmissionReceiver(null);
        }

        @Override
        public String getIdentifier() {
            return ModularMachinery.MODID + ":Provider";
        }

    }

}
