package github.kasuminova.mmce.common.integration.gregtech.handlerproxy;

import gregtech.api.capability.IEnergyContainer;
import hellfirepvp.modularmachinery.common.util.IEnergyHandlerAsync;

public class GTEnergyHandlerProxy implements IEnergyHandlerAsync {
    public static final int  ENERGY_MULTIPLIER = 4;
    public static final long MAX_CAPACITY      = Long.MAX_VALUE / ENERGY_MULTIPLIER;

    private final IEnergyContainer energyContainer;

    public GTEnergyHandlerProxy(final IEnergyContainer energyContainer) {
        this.energyContainer = energyContainer;
    }

    @Override
    public long getCurrentEnergy() {
        if (MAX_CAPACITY <= energyContainer.getEnergyStored()) {
            return Long.MAX_VALUE;
        } else {
            return energyContainer.getEnergyStored() * ENERGY_MULTIPLIER;
        }
    }

    @Override
    public void setCurrentEnergy(final long energy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getMaxEnergy() {
        if (MAX_CAPACITY <= energyContainer.getEnergyCapacity()) {
            return Long.MAX_VALUE;
        } else {
            return energyContainer.getEnergyCapacity() * ENERGY_MULTIPLIER;
        }
    }

    @Override
    public boolean extractEnergy(final long energy) {
        long convertedEnergy = energy / ENERGY_MULTIPLIER;

        long removed = -energyContainer.removeEnergy(convertedEnergy);
        if (removed < convertedEnergy) {
            energyContainer.addEnergy(removed);
            return false;
        }
        return true;
    }

    @Override
    public boolean receiveEnergy(final long energy) {
        long convertedEnergy = energy / ENERGY_MULTIPLIER;

        long added = energyContainer.addEnergy(convertedEnergy);
        if (added < energy) {
            energyContainer.removeEnergy(added);
            return false;
        }
        return true;
    }
}
