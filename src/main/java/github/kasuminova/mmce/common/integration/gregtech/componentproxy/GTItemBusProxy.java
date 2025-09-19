package github.kasuminova.mmce.common.integration.gregtech.componentproxy;

import github.kasuminova.mmce.common.machine.component.MachineComponentProxy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEInputBus;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEOutputBus;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GTItemBusProxy implements MachineComponentProxy<GTItemBusProxy.GTItemBusMachineComponent> {
    public static final GTItemBusProxy INSTANCE = new GTItemBusProxy();

    private GTItemBusProxy() {
    }

    @Nullable
    protected static GTItemBusMachineComponent getGtItemBusMachineComponent(final MultiblockAbility<IItemHandlerModifiable> ability,
                                                                            final List<IItemHandlerModifiable> abilities) {
        if (ability == MultiblockAbility.IMPORT_ITEMS) {
            return new GTItemBusMachineComponent(IOType.INPUT, abilities.get(0));
        }
        if (ability == MultiblockAbility.EXPORT_ITEMS) {
            return new GTItemBusMachineComponent(IOType.OUTPUT, abilities.get(0));
        }
        return null;
    }

    @Override
    public boolean isSupported(final TileEntity te) {
        if (te instanceof MetaTileEntityHolder metaTEHolder) {
            MetaTileEntity metaTE = metaTEHolder.getMetaTileEntity();
            return metaTE instanceof MetaTileEntityItemBus ||
                metaTE instanceof MetaTileEntityMEInputBus ||
                metaTE instanceof MetaTileEntityMEOutputBus;
        }
        return false;
    }

    @Override
    public GTItemBusMachineComponent proxyComponent(final TileEntity te) {
        if (!(te instanceof MetaTileEntityHolder metaTEHolder)) {
            return null;
        }
        MetaTileEntity metaTE = metaTEHolder.getMetaTileEntity();
        List<IItemHandlerModifiable> abilities = new ArrayList<>();
        if (metaTE instanceof MetaTileEntityItemBus itemBus) {
            itemBus.registerAbilities(abilities);
            return getGtItemBusMachineComponent(itemBus.getAbility(), abilities);
        }
        if (metaTE instanceof MetaTileEntityMEInputBus meInputBus) {
            meInputBus.registerAbilities(abilities);
            return getGtItemBusMachineComponent(meInputBus.getAbility(), abilities);
        }
        // TODO: Not fully supported, only 1 slots.
        if (metaTE instanceof MetaTileEntityMEOutputBus meOutputBus) {
            meOutputBus.registerAbilities(abilities);
            return getGtItemBusMachineComponent(meOutputBus.getAbility(), abilities);
        }
        return null;
    }

    public static class GTItemBusMachineComponent extends MachineComponent.ItemBus {
        private final IItemHandlerModifiable itemHandler;

        public GTItemBusMachineComponent(final IOType ioType, IItemHandlerModifiable itemHandler) {
            super(ioType);
            this.itemHandler = itemHandler;
        }

        @Override
        public boolean isAsyncSupported() {
            return false;
        }

        @Override
        public IItemHandlerModifiable getContainerProvider() {
            return itemHandler;
        }
    }
}
