package github.kasuminova.mmce.common.integration.gregtech.componentproxy;

import github.kasuminova.mmce.common.integration.gregtech.handlerproxy.GTFluidTankProxy;
import github.kasuminova.mmce.common.machine.component.MachineComponentProxy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEInputHatch;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEOutputHatch;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GTFluidHatchProxy implements MachineComponentProxy<GTFluidHatchProxy.GTFluidHatchMachineComponent> {
    public static final GTFluidHatchProxy INSTANCE = new GTFluidHatchProxy();

    private GTFluidHatchProxy() {
    }

    @Nullable
    protected static GTFluidHatchMachineComponent getGtFluidHatchMachineComponent(final MultiblockAbility<IFluidTank> ability,
                                                                                  final List<IFluidTank> abilities) {
        if (ability == MultiblockAbility.IMPORT_FLUIDS) {
            return new GTFluidHatchMachineComponent(IOType.INPUT, abilities.get(0));
        }
        if (ability == MultiblockAbility.EXPORT_FLUIDS) {
            return new GTFluidHatchMachineComponent(IOType.OUTPUT, abilities.get(0));
        }
        return null;
    }

    @Override
    public boolean isSupported(final TileEntity te) {
        if (te instanceof MetaTileEntityHolder metaTEHolder) {
            MetaTileEntity metaTE = metaTEHolder.getMetaTileEntity();
            return metaTE instanceof MetaTileEntityFluidHatch ||
                metaTE instanceof MetaTileEntityMEInputHatch ||
                metaTE instanceof MetaTileEntityMEOutputHatch;
        }
        return false;
    }

    @Override
    public GTFluidHatchMachineComponent proxyComponent(final TileEntity te) {
        if (!(te instanceof MetaTileEntityHolder metaTEHolder)) {
            return null;
        }

        MetaTileEntity metaTE = metaTEHolder.getMetaTileEntity();
        List<IFluidTank> abilities = new ArrayList<>();
        if (metaTE instanceof MetaTileEntityFluidHatch itemBus) {
            itemBus.registerAbilities(abilities);
            return getGtFluidHatchMachineComponent(itemBus.getAbility(), abilities);
        }
        if (metaTE instanceof MetaTileEntityMEInputHatch meInputHatch) {
            meInputHatch.registerAbilities(abilities);
            return getGtFluidHatchMachineComponent(meInputHatch.getAbility(), abilities);
        }
        // TODO: Not fully supported, only 1 slots.
        if (metaTE instanceof MetaTileEntityMEOutputHatch meOutputHatch) {
            meOutputHatch.registerAbilities(abilities);
            return getGtFluidHatchMachineComponent(meOutputHatch.getAbility(), abilities);
        }
        return null;
    }

    public static class GTFluidHatchMachineComponent extends MachineComponent.FluidHatch {
        private final IFluidHandler fluidHandler;

        public GTFluidHatchMachineComponent(final IOType ioType, IFluidTank fluidTank) {
            super(ioType);
            this.fluidHandler = new GTFluidTankProxy(fluidTank);
        }

        @Override
        public boolean isAsyncSupported() {
            return false;
        }

        @Override
        public IFluidHandler getContainerProvider() {
            return fluidHandler;
        }
    }

}
