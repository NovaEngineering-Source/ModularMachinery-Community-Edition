package kport.modularmagic.common.event;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.item.ItemDynamicColor;
import kport.modularmagic.common.crafting.component.ModularMagicComponents;
import kport.modularmagic.common.crafting.requirement.types.ModularMagicRequirements;
import kport.modularmagic.common.item.ModularMagicItems;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RegistrationEvent {

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        for (Item block : ModularMagicItems.ITEMS) {
            event.getRegistry().register(block);
        }
    }

    @SubscribeEvent
    public static void registerItemModels(ModelRegistryEvent event) {
        for (Item item : ModularMagicItems.ITEMS) {
            if (item instanceof ItemDynamicColor) {
                ModularMagicItems.COLOR_ITEMS.add((ItemDynamicColor) item);
            }

            if (item instanceof final ItemBlock itemBlock) {
                Block block = itemBlock.getBlock();
                ResourceLocation registryName = block.getRegistryName();
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(registryName, "inventory"));
            }
        }
    }

    @SubscribeEvent
    public static void onComponentTypeRegister(RegistryEvent.Register<ComponentType> event) {
        for (ComponentType component : ModularMagicComponents.COMPONENTS) {
            event.getRegistry().register(component);
        }
    }

    @SubscribeEvent
    public static void onRequirementTypeRegister(RegistryEvent.Register event) {
        if (event.getGenericType() != RequirementType.class) {
            return;
        }
        for (RequirementType<?, ?> requirement : ModularMagicRequirements.REQUIREMENTS) {
            event.getRegistry().register(requirement);
        }
    }
}
