package github.kasuminova.mmce.common.integration.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.GroovyPlugin;
import com.cleanroommc.groovyscript.api.INamed;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.ModPropertyContainer;
import com.cleanroommc.groovyscript.event.EventBusType;
import com.cleanroommc.groovyscript.event.GroovyEventManager;
import github.kasuminova.mmce.client.model.DynamicMachineModelRegistry;
import github.kasuminova.mmce.client.resource.GeoModelExternalLoader;
import github.kasuminova.mmce.common.concurrent.RecipeCraftingContextPool;
import github.kasuminova.mmce.common.event.machine.IEventHandler;
import github.kasuminova.mmce.common.event.machine.MachineEvent;
import github.kasuminova.mmce.common.upgrade.registry.RegistryUpgrade;
import groovy.lang.Closure;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.MachineBuilder;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.MachineModifier;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.event.MMEvents;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.util.BlockArrayCache;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GroovyScriptPlugin implements GroovyPlugin {

    private static GroovyContainer<?> container;

    public static GroovyContainer<?> getContainer() {
        return container;
    }

    @Override
    public @NotNull String getModId() {
        return ModularMachinery.MODID;
    }

    @Override
    public @NotNull String getContainerName() {
        return ModularMachinery.NAME;
    }

    @Override
    public @Nullable ModPropertyContainer createModPropertyContainer() {
        return new Container();
    }

    @Override
    public void onCompatLoaded(GroovyContainer<?> container) {
        GroovyScriptPlugin.container = container;
    }

    private static void onReload() {
        MachineBuilder.WAIT_FOR_LOAD.clear();

        RegistryUpgrade.clearAll();
        RecipeRegistry.getRegistry().clearAllRecipes();
        // Reset RecipeAdapterIncId
        RegistriesMM.ADAPTER_REGISTRY.getValuesCollection().forEach(RecipeAdapter::resetIncId);

        if (FMLCommonHandler.instance().getSide().isClient() && Mods.GECKOLIB.isPresent()) {
            DynamicMachineModelRegistry.INSTANCE.onReload();
        }

        for (DynamicMachine loadedMachine : MachineRegistry.getLoadedMachines()) {
            loadedMachine.getMachineEventHandlers().clear();
            loadedMachine.getSmartInterfaceTypes().clear();
            loadedMachine.getCoreThreadPreset().clear();
            loadedMachine.getModifiers().clear();
            loadedMachine.getMultiBlockModifiers().clear();
        }
        // Reload JSON Machine
        MachineRegistry.preloadMachines();
        // Reload All Machine
        MachineRegistry.reloadMachine(MachineRegistry.loadMachines(null));
    }

    private static void afterScriptRun() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        boolean isServer = server != null && server.isDedicatedServer();

        MachineRegistry.reloadMachine(MachineBuilder.WAIT_FOR_LOAD);

        if (FMLCommonHandler.instance().getSide().isClient() && Mods.GECKOLIB.isPresent()) {
            ClientProxy.clientScheduler.addRunnable(GeoModelExternalLoader.INSTANCE::onReload, 0);
        }

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> BlockArrayCache.buildCache(MachineRegistry.getLoadedMachines()));

        MachineModifier.loadAll();
        MMEvents.registryAll();

        RecipeCraftingContextPool.onReload();
        RecipeRegistry.getRegistry().loadRecipeRegistry(null, true);
        // TODO
        /*for (Action action : FactoryRecipeThread.WAIT_FOR_ADD) {
            action.doAction();
        }
        FactoryRecipeThread.WAIT_FOR_ADD.clear();*/

        if (!isServer) {
            ModIntegrationJEI.reloadRecipeWrappers();
        }

        future.join();

        // Flush the context to preview the changed structure.
        if (!isServer) {
            ModIntegrationJEI.reloadPreviewWrappers();
        }
    }

    private static class Container extends ModPropertyContainer {

        private final GroovyMachine dummy = new DummyMachine();
        private final Map<ResourceLocation, GroovyMachine> machines = new Object2ObjectOpenHashMap<>();
        private final Map<String, GroovyMachine> properties = new Object2ObjectOpenHashMap<>();

        public GroovyMachine machine(ResourceLocation rl) {
            GroovyMachine machine = this.machines.get(rl);
            if (machine == null) {
                DynamicMachine dynamicMachine = MachineRegistry.getRegistry().getMachine(rl);
                if (dynamicMachine != null) {
                    machine = new GroovyMachine(rl);
                    this.machines.put(rl, machine);
                    this.properties.put(rl.getPath(), machine);
                }
            }
            return machine;
        }

        public GroovyMachine machine(String mod, String name) {
            return machine(new ResourceLocation(mod, name));
        }

        public GroovyMachine machine(String name) {
            String mod;
            int i = name.indexOf(":");
            if (i > 0) {
                mod = name.substring(0, i);
                name = name.substring(i + 1);
            } else if (i == 0) {
                mod = ModularMachinery.MODID;
                name = name.substring(1);
            } else {
                mod = ModularMachinery.MODID;
            }
            return machine(new ResourceLocation(mod, name));
        }

        @Override
        public @Nullable Object getProperty(String name) {
            return machine(new ResourceLocation(ModularMachinery.MODID, name));
        }

        @Override
        public Map<String, Object> getProperties() {
            for (DynamicMachine machine : MachineRegistry.getLoadedMachines()) {
                if (!this.machines.containsKey(machine.getRegistryName())) {
                    GroovyMachine groovyMachine = new GroovyMachine(machine.getRegistryName());
                    this.machines.put(machine.getRegistryName(), groovyMachine);
                    this.properties.put(machine.getRegistryName().getPath(), groovyMachine);
                }
            }
            return Collections.unmodifiableMap(this.properties);
        }

        public Object getAt(String name) {
            return machine(name);
        }

        @Override
        public Collection<INamed> getRegistries() {
            return Collections.singleton(this.dummy);
        }

        public void machineEvent(String machineRegistryName, Class<? extends MachineEvent> clazz, Closure<?> listener) {
            GroovyEventManager.INSTANCE.listen(EventPriority.NORMAL, EventBusType.MAIN, clazz, event -> {
                DynamicMachine machine = MachineRegistry.getRegistry().getMachine(new ResourceLocation(ModularMachinery.MODID, machineRegistryName));
                if (machine != null) {
                    machine.addMachineEventHandler(clazz, IEventHandler.of(listener));
                } else {
                    GroovyLog.get().error("Could not find machine `" + machineRegistryName + "`!");
                }
            });
        }
    }

    private static class DummyMachine extends GroovyMachine {

        public DummyMachine() {
            super(new ResourceLocation(ModularMachinery.MODID, "machine_name"));
        }

        @Override
        public void onReload() {
            GroovyScriptPlugin.onReload();
        }

        @Override
        public void afterScriptLoad() {
            GroovyScriptPlugin.afterScriptRun();
        }

        @Override
        public GroovyRecipe recipeBuilder(String name) {
            throw new UnsupportedOperationException();
        }
    }
}
