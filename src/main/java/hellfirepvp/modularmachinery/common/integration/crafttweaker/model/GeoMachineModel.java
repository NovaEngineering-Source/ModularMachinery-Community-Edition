package hellfirepvp.modularmachinery.common.integration.crafttweaker.model;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.client.model.DynamicMachineModelRegistry;
import github.kasuminova.mmce.client.model.MachineControllerModel;
import github.kasuminova.mmce.common.util.Sides;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.modularmachinery.GeoMachineModel")
public class GeoMachineModel {

    @ZenMethod
    public static void registerGeoMachineModel(String modelName, String modelLocation, String textureLocation, String animationFileLocation) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return;
        }

        ResourceLocation modelLocationRL = new ResourceLocation(modelLocation);
        ResourceLocation textureLocationRL = new ResourceLocation(textureLocation);
        ResourceLocation animationFileLocationRL = new ResourceLocation(animationFileLocation);

        if (modelLocationRL.getNamespace().equals("minecraft")) {
            modelLocationRL = new ResourceLocation(ModularMachinery.MODID, modelLocationRL.getPath());
        }
        if (textureLocationRL.getNamespace().equals("minecraft")) {
            textureLocationRL = new ResourceLocation(ModularMachinery.MODID, textureLocationRL.getPath());
        }
        if (animationFileLocationRL.getNamespace().equals("minecraft")) {
            animationFileLocationRL = new ResourceLocation(ModularMachinery.MODID, animationFileLocationRL.getPath());
        }

        MachineControllerModel model = new MachineControllerModel(modelName, modelLocationRL, textureLocationRL, animationFileLocationRL);
        if (Sides.isClient()) {
            model.initializePool();
        }
        DynamicMachineModelRegistry.INSTANCE.registerMachineModel(modelName, model);
    }

}
