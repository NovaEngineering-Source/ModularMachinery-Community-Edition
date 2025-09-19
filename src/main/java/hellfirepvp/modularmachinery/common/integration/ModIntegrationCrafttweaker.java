/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration;

import crafttweaker.mc1120.events.ScriptRunEvent;
import github.kasuminova.mmce.client.model.DynamicMachineModelRegistry;
import github.kasuminova.mmce.client.resource.GeoModelExternalLoader;
import github.kasuminova.mmce.common.concurrent.RecipeCraftingContextPool;
import github.kasuminova.mmce.common.upgrade.registry.RegistryUpgrade;
import github.kasuminova.mmce.common.util.Sides;
import github.kasuminova.mmce.common.util.concurrent.Action;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.MachineBuilder;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.MachineModifier;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.event.MMEvents;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.BlockArrayCache;
import hellfirepvp.modularmachinery.common.util.IBlockStateDescriptor;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import youyihj.zenutils.api.reload.ScriptReloadEvent;

import java.util.concurrent.CompletableFuture;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModIntegrationCrafttweaker
 * Created by HellFirePvP
 * Date: 18.08.2017 / 10:44
 */
@SuppressWarnings("MethodMayBeStatic")
public class ModIntegrationCrafttweaker {
    @SubscribeEvent
    public void onScriptsLoading(ScriptRunEvent.Pre event) {
        RecipeRegistry.getRegistry().clearLingeringRecipes();
        MachineBuilder.WAIT_FOR_LOAD.clear();
    }

    @SubscribeEvent
    public void onScriptsLoaded(ScriptRunEvent.Post event) {
        RecipeRegistry.reloadAdapters();
    }

    @SubscribeEvent
    @Optional.Method(modid = "zenutils")
    public void onScriptsReloading(ScriptReloadEvent.Pre event) {
        ICommandSender sender = event.getRequester();
        sender.sendMessage(new TextComponentTranslation("message.reloading"));

        MachineBuilder.WAIT_FOR_LOAD.clear();

        RegistryUpgrade.clearAll();

        sender.sendMessage(new TextComponentTranslation(
            "message.cleared.recipes", RecipeRegistry.registeredRecipeCount()));
        RecipeRegistry.getRegistry().clearAllRecipes();
        // Reset RecipeAdapterIncId
        RegistriesMM.ADAPTER_REGISTRY.getValuesCollection().forEach(RecipeAdapter::resetIncId);

        if (Sides.isClient() && Mods.GECKOLIB.isPresent()) {
            DynamicMachineModelRegistry.INSTANCE.onReload();
        }

        for (DynamicMachine loadedMachine : MachineRegistry.getLoadedMachines()) {
            loadedMachine.getMachineEventHandlers().clear();
            loadedMachine.getSmartInterfaceTypes().clear();
            loadedMachine.getCoreThreadPreset().clear();
            loadedMachine.getModifiers().clear();
            loadedMachine.getMultiBlockModifiers().clear();
        }
        // Clear Pool
        IBlockStateDescriptor.clearPool();
        BlockArray.BlockInformation.clearPool();
        // Reload JSON Machine
        MachineRegistry.preloadMachines();
        // Reload All Machine
        MachineRegistry.reloadMachine(MachineRegistry.loadMachines(null));
        sender.sendMessage(new TextComponentTranslation(
            "message.reloaded.machines", MachineRegistry.getLoadedMachines().size()));
    }

    @SubscribeEvent
    @Optional.Method(modid = "zenutils")
    public void onScriptsReloaded(ScriptReloadEvent.Post event) {
        ICommandSender sender = event.getRequester();
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        boolean isServer = server != null && server.isDedicatedServer();

        MachineRegistry.reloadMachine(MachineBuilder.WAIT_FOR_LOAD);

        if (Sides.isClient() && Mods.GECKOLIB.isPresent()) {
            ClientProxy.clientScheduler.addRunnable(GeoModelExternalLoader.INSTANCE::onReload, 0);
        }

        CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
            BlockArrayCache.buildCache(MachineRegistry.getLoadedMachines()));

        MachineModifier.loadAll();
        MMEvents.registryAll();

        RecipeCraftingContextPool.onReload();
        RecipeRegistry.getRegistry().loadRecipeRegistry(null, true);
        for (Action action : FactoryRecipeThread.WAIT_FOR_ADD) {
            action.doAction();
        }
        FactoryRecipeThread.WAIT_FOR_ADD.clear();

        if (!isServer) {
            ModIntegrationJEI.reloadRecipeWrappers();
        }

        future.join();

        // Flush the context to preview the changed structure.
        if (!isServer) {
            ModIntegrationJEI.reloadPreviewWrappers();
        }

        sender.sendMessage(new TextComponentTranslation(
            "message.reloaded.recipes", RecipeRegistry.registeredRecipeCount()));

        sender.sendMessage(new TextComponentTranslation("message.reloaded"));
    }

}
