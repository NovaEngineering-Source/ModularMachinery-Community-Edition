package kport.gugu_utils;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class GuGuEvent {

    @SubscribeEvent
    public void onComponentTypeRegister(RegistryEvent.Register<ComponentType> event) {
        GuGuCompoments.initComponents(event.getRegistry());
    }

    @SubscribeEvent
    public void onRequirementTypeRegister(RegistryEvent.Register event) {
        if (event.getGenericType() != RequirementType.class)
            return;
        GuGuRequirements.initRequirements(event.getRegistry());
    }
}
