package kport.gugu_utils.common.tile;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import kport.gugu_utils.common.block.BlockEmberInputHatch;
import kport.gugu_utils.common.IRestorableTileEntity;
import kport.gugu_utils.CommonMMTile;
import kport.gugu_utils.GuGuCompoments;
import kport.gugu_utils.common.components.GenericMachineCompoment;
import kport.gugu_utils.common.embers.EmbersHatchVariant;
import kport.gugu_utils.common.requirements.RequirementEmber;
import kport.gugu_utils.common.requirements.basic.IConsumable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import teamroots.embers.api.capabilities.EmbersCapabilities;
import teamroots.embers.api.power.IEmberCapability;
import teamroots.embers.power.DefaultEmberCapability;

import javax.annotation.Nullable;

public class TileEmberInputHatch extends CommonMMTile implements IRestorableTileEntity, IConsumable<RequirementEmber.RT>, MachineComponentTile {

    private final IEmberCapability capability;
    private int emberCapacity = -1;

    public TileEmberInputHatch() {
        this.capability = new DefaultEmberCapability() {
            @Override
            public boolean acceptsVolatile() {
                return TileEmberInputHatch.this.getEmberCapacity() >= 3200;
            }
        };
    }

    public int getEmberCapacity() {
        if (emberCapacity < 0) {
            IBlockState bs = world.getBlockState(getPos());
            if (bs.equals(Blocks.AIR.getDefaultState())) {
                return -1;
            }
            EmbersHatchVariant variant = bs.getValue(BlockEmberInputHatch.VARIANT);
            emberCapacity = variant.getEmberMaxStorage();

        }
        return emberCapacity;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if (oldState.getBlock() != newState.getBlock())
            return true;
        if (oldState.getBlock() != BlocksMM.blockEmberInputProvider || newState.getBlock() != BlocksMM.blockEmberInputProvider)
            return true;
        return oldState.getValue(BlockEmberInputHatch.VARIANT) != newState.getValue(BlockEmberInputHatch.VARIANT);
    }


    @Override
    public void readRestorableFromNBT(NBTTagCompound compound) {
        capability.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeRestorableToNBT(NBTTagCompound compound) {
        capability.writeToNBT(compound);
        return compound;
    }


    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new GenericMachineCompoment<>(this, (ComponentType) GuGuCompoments.COMPONENT_EMBER);
    }

    @Override
    public boolean consume(RequirementEmber.RT outputToken, boolean doOperation) {

        double consume = this.capability.removeAmount(outputToken.getEmber(), doOperation);
        outputToken.setEmber(outputToken.getEmber() - consume);
        return consume > 0;
    }


    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == EmbersCapabilities.EMBER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (this.emberCapacity < 0 && getEmberCapacity() >= 0) {
            this.capability.setEmberCapacity(getEmberCapacity());
        }
        return capability == EmbersCapabilities.EMBER_CAPABILITY ? (T) this.capability : super.getCapability(capability, facing);
    }
}
