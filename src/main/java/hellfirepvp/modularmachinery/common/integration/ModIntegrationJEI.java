/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration;

import com.google.common.collect.Lists;
import github.kasuminova.mmce.client.gui.GuiMEItemInputBus;
import github.kasuminova.mmce.client.gui.integration.handler.MEInputGhostSlotHandler;
import github.kasuminova.mmce.common.container.handler.MEInputRecipeTransferHandler;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluid;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluidGas;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluidRenderer;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridStackHelper;
import hellfirepvp.modularmachinery.common.integration.preview.CategoryStructurePreview;
import hellfirepvp.modularmachinery.common.integration.preview.StructurePreviewWrapper;
import hellfirepvp.modularmachinery.common.integration.recipe.CategoryDynamicRecipe;
import hellfirepvp.modularmachinery.common.integration.recipe.DynamicRecipeWrapper;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutHelper;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import mezz.jei.Internal;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.Config;
import mezz.jei.input.InputHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModIntegrationJEI
 * Created by HellFirePvP
 * Date: 10.07.2017 / 19:22
 */
@JEIPlugin
public class ModIntegrationJEI implements IModPlugin {
    public static final  String                                                           CATEGORY_PREVIEW        = "modularmachinery.preview";
    public static final  List<StructurePreviewWrapper>                                    PREVIEW_WRAPPERS        = Lists.newArrayList();
    private static final Map<DynamicMachine, CategoryDynamicRecipe>                       RECIPE_CATEGORIES       = new HashMap<>();
    private static final Map<DynamicMachine, Map<ResourceLocation, DynamicRecipeWrapper>> MACHINE_RECIPE_WRAPPERS = new HashMap<>();

    public static Field inputHandler = null;
    public static Field bookmarkList = null;

    public static IStackHelper        stackHelper;
    public static IJeiHelpers         jeiHelpers;
    public static IIngredientRegistry ingredientRegistry;
    public static IRecipeRegistry     recipeRegistry;
    public static IJeiRuntime         jeiRuntime;

    static {
        // I Just want to get the BookmarkList...
        try {
            Field inputHandler = Internal.class.getDeclaredField("inputHandler");
            inputHandler.setAccessible(true);
            ModIntegrationJEI.inputHandler = inputHandler;

            Field bookmarkList = InputHandler.class.getDeclaredField("bookmarkList");
            bookmarkList.setAccessible(true);
            ModIntegrationJEI.bookmarkList = bookmarkList;
        } catch (NoSuchFieldException e) {
            ModularMachinery.log.warn(e);
        }
    }

    public static String getCategoryStringFor(DynamicMachine machine) {
        return "modularmachinery.recipes." + machine.getRegistryName().getPath();
    }

    public static CategoryDynamicRecipe getCategory(DynamicMachine machine) {
        return RECIPE_CATEGORIES.get(machine);
    }

    public static void reloadRecipeWrappers() {
        RECIPE_CATEGORIES.values().forEach(CategoryDynamicRecipe::reloadCategory);

        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            Iterable<MachineRecipe> recipes = RecipeRegistry.getRecipesFor(machine);
            Map<ResourceLocation, DynamicRecipeWrapper> wrappers = MACHINE_RECIPE_WRAPPERS.computeIfAbsent(machine, v -> new LinkedHashMap<>());
            for (MachineRecipe recipe : recipes) {
                if (!recipe.getLoadJEI()) {
                    continue;
                }
                DynamicRecipeWrapper wrapper = wrappers.get(recipe.getRegistryName());
                if (wrapper != null) {
                    wrapper.reloadWrapper(recipe);
                }
            }
        }
    }

    public static void reloadPreviewWrappers() {
//        PREVIEW_WRAPPERS.forEach(StructurePreviewWrapper::flushContext);
    }

    public static void addItemStackToBookmarkList(ItemStack stack) {
        if (inputHandler == null || bookmarkList == null || stack.isEmpty()) {
            return;
        }

        try {
            InputHandler handler = (InputHandler) inputHandler.get(null);
            BookmarkList bookmark = (BookmarkList) bookmarkList.get(handler);

            if (!Config.isBookmarkOverlayEnabled()) {
                Config.toggleBookmarkEnabled();
            }
            bookmark.add(stack);
        } catch (IllegalAccessException e) {
            ModularMachinery.log.warn(e);
        }
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(ItemsMM.blueprint, (s) -> {
            DynamicMachine machine = ItemBlueprint.getAssociatedMachine(s);
            if (machine == null) {
                return ISubtypeRegistry.ISubtypeInterpreter.NONE;
            }
            return machine.getRegistryName().toString();
        });
    }

    /**
     * AE2FCR need this.
     */
    @Override
    @Deprecated
    public void registerIngredients(IModIngredientRegistration registry) {
        try {
            registry.register(() -> HybridFluid.class, Lists.newArrayList(), new HybridStackHelper<>(), new HybridFluidRenderer<>());
            if (Mods.MEKANISM.isPresent()) {
                registerHybridGas(registry);
            }
        } catch (Exception exc) {
            ModularMachinery.log.warn("Error setting up HybridFluid JEI registration! Check the log after this for more details! Report this error!");
            exc.printStackTrace();
            throw exc;
        }
    }


    @Optional.Method(modid = "mekanism")
    private void registerHybridGas(IModIngredientRegistration registry) {
        registry.register(() -> HybridFluidGas.class, Lists.newArrayList(), new HybridStackHelper<>(), new HybridFluidRenderer<>());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        jeiHelpers = registry.getJeiHelpers();
        RecipeLayoutHelper.init();

        registry.addRecipeCategories(new CategoryStructurePreview());

        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            CategoryDynamicRecipe recipe = new CategoryDynamicRecipe(machine);
            RECIPE_CATEGORIES.put(machine, recipe);
            registry.addRecipeCategories(recipe);
        }
    }

    @Override
    public void register(IModRegistry registry) {
        jeiHelpers = registry.getJeiHelpers();
        ingredientRegistry = registry.getIngredientRegistry();
        RecipeLayoutHelper.init();

        registry.addRecipeCatalyst(new ItemStack(BlocksMM.blockController), CATEGORY_PREVIEW);
        registry.addRecipeCatalyst(new ItemStack(BlocksMM.blockFactoryController), CATEGORY_PREVIEW);
        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            ItemStack stack = new ItemStack(ItemsMM.blueprint);
            ItemBlueprint.setAssociatedMachine(stack, machine);
            String machineCategory = getCategoryStringFor(machine);
            registry.addRecipeCatalyst(stack, machineCategory);
        }

        registry.addGhostIngredientHandler(GuiMEItemInputBus.class, new MEInputGhostSlotHandler());

        // Only handle MM recipes
        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            String machineCategory = getCategoryStringFor(machine);
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(new MEInputRecipeTransferHandler(), machineCategory);
        }

        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            PREVIEW_WRAPPERS.add(new StructurePreviewWrapper(machine));
        }

        registry.addRecipes(PREVIEW_WRAPPERS, CATEGORY_PREVIEW);

        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            Iterable<MachineRecipe> recipes = RecipeRegistry.getRecipesFor(machine);
            Map<ResourceLocation, DynamicRecipeWrapper> wrappers = MACHINE_RECIPE_WRAPPERS.computeIfAbsent(machine, v -> new LinkedHashMap<>());
            for (MachineRecipe recipe : recipes) {
                if (!recipe.getLoadJEI()) {
                    continue;
                }
                wrappers.put(recipe.getRegistryName(), new DynamicRecipeWrapper(recipe));
            }
            registry.addRecipes(wrappers.values(), getCategoryStringFor(machine));
        }

        BlockController.MACHINE_CONTROLLERS.values().forEach(controller ->
            registry.addRecipeCatalyst(new ItemStack(controller),
                getCategoryStringFor(controller.getParentMachine()))
        );
        BlockController.MOC_MACHINE_CONTROLLERS.values().forEach(controller ->
            registry.addRecipeCatalyst(new ItemStack(controller),
                getCategoryStringFor(controller.getParentMachine()))
        );
        BlockFactoryController.FACTORY_CONTROLLERS.values().forEach(controller ->
            registry.addRecipeCatalyst(new ItemStack(controller),
                getCategoryStringFor(controller.getParentMachine()))
        );
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        recipeRegistry = jeiRuntime.getRecipeRegistry();
        ModIntegrationJEI.jeiRuntime = jeiRuntime;
    }

}
