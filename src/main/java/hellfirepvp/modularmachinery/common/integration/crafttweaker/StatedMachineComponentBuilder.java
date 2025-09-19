package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.block.BlockStatedMachineComponent;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.modularmachinery.StatedMachineComponentBuilder")
public class StatedMachineComponentBuilder {
    private final BlockStatedMachineComponent block;

    public StatedMachineComponentBuilder(final String registryName) {
        this.block = new BlockStatedMachineComponent();
        this.block.setRegistryName(
            new ResourceLocation(ModularMachinery.MODID, registryName));
    }

    @ZenMethod
    public static StatedMachineComponentBuilder newBuilder(String registryName) {
        return new StatedMachineComponentBuilder(registryName);
    }

    @ZenMethod
    public StatedMachineComponentBuilder setColoured(final boolean coloured) {
        block.setColoured(coloured);
        return this;
    }

    @ZenMethod
    public void build() {
        BlockStatedMachineComponent.WAIT_FOR_REGISTRY.add(block);
    }
}
