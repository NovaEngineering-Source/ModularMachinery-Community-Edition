package github.kasuminova.mmce.common.concurrent;

import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RecipeCraftingContextPool {
    private static final Map<ResourceLocation, Queue<RecipeCraftingContext>> POOL = new ConcurrentHashMap<>();

    private static int reloadCounter = 0;

    @Nonnull
    public static RecipeCraftingContext borrowCtx(@Nonnull final ActiveMachineRecipe activeRecipe,
                                                  @Nonnull final TileMultiblockMachineController ctrl) {
        if (POOL.isEmpty()) {
            return new RecipeCraftingContext(reloadCounter, activeRecipe, ctrl);
        }

        Queue<RecipeCraftingContext> queue = POOL.computeIfAbsent(
            activeRecipe.getRecipe().getRegistryName(), q -> new ConcurrentLinkedQueue<>());

        RecipeCraftingContext ctx;
        if ((ctx = queue.poll()) != null) {
            return ctx.init(activeRecipe, ctrl);
        }
        return new RecipeCraftingContext(reloadCounter, activeRecipe, ctrl);
    }

    public static void returnCtx(@Nonnull RecipeCraftingContext ctx) {
        if (ctx.getReloadCounter() != reloadCounter) {
            ctx.destroy();
            return;
        }
        POOL.computeIfAbsent(ctx.getParentRecipe().getRegistryName(), q -> new ConcurrentLinkedQueue<>())
            .offer(ctx.resetAll());
    }

    public static void onReload() {
        POOL.clear();

        reloadCounter++;
    }
}
