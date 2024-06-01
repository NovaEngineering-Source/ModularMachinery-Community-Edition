package hellfirepvp.modularmachinery.common.machine;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.concurrent.RecipeCraftingContextPool;
import github.kasuminova.mmce.common.concurrent.RecipeSearchTask;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftingStatus;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import io.netty.util.internal.ThrowableUtil;
import net.minecraft.nbt.NBTTagCompound;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ZenRegister
@ZenClass("mods.modularmachinery.RecipeThread")
public abstract class RecipeThread {
    protected final TileMultiblockMachineController ctrl;
    protected final Map<String, RecipeModifier> permanentModifiers = new ConcurrentHashMap<>();
    protected final Map<String, RecipeModifier> semiPermanentModifiers = new ConcurrentHashMap<>();

    protected ActiveMachineRecipe activeRecipe = null;
    protected CraftingStatus status = CraftingStatus.IDLE;
    protected boolean waitForFinish = false;

    private RecipeCraftingContext context = null;

    protected RecipeSearchTask searchTask = null;

    protected RecipeThread(TileMultiblockMachineController ctrl) {
        this.ctrl = ctrl;
    }

    protected static ActiveMachineRecipe deserializeActiveRecipe(NBTTagCompound tag, TileMultiblockMachineController factory) {
        ActiveMachineRecipe activeRecipe = null;
        NBTTagCompound recipeTag = tag.getCompoundTag("activeRecipe");
        if (tag.hasKey("activeRecipe")) {
            activeRecipe = new ActiveMachineRecipe(recipeTag);
        }
        if (activeRecipe != null && activeRecipe.getRecipe() == null) {
            activeRecipe = null;
            ModularMachinery.log.info("Couldn't find recipe named " + recipeTag.getString("recipeName") + " for controller at " + factory.getPos());
        }
        return activeRecipe;
    }

    public CraftingStatus onTick() {
        if (activeRecipe == null) {
            return status;
        }
        if (context == null) {
            setContext(createContext(activeRecipe));
        }
        return (status = activeRecipe.tick(ctrl, context));
    }

    public void onFinished() {
        if (activeRecipe == null) {
            waitForFinish = false;
            return;
        }
        if (context == null) {
            setContext(createContext(activeRecipe));
        }

        RecipeCraftingContext.CraftingCheckResult checkResult = context.canFinishCrafting();
        if (checkResult.isFailure()) {
            waitForFinish = true;
            status = CraftingStatus.failure(checkResult.getFirstErrorMessage(""));
            return;
        }

        waitForFinish = false;
        context.finishCrafting();
        fireFinishedEvent();
        semiPermanentModifiers.clear();

        tryRestartRecipe();
    }

    public boolean isCompleted() {
        return activeRecipe != null && activeRecipe.isCompleted();
    }

    public abstract void fireStartedEvent();

    public abstract void fireFinishedEvent();

    public abstract void tryRestartRecipe();

    public RecipeCraftingContext createContext(ActiveMachineRecipe activeRecipe) {
        RecipeCraftingContext context = ctrl.createContext(activeRecipe);
        context.addModifier(semiPermanentModifiers.values());
        context.addModifier(permanentModifiers.values());
        return context;
    }

    public void searchAndStartRecipe() {
        if (searchTask != null) {
            if (!searchTask.isDone()) {
                return;
            }

            ctrl.getTimeRecorder().addRecipeResearchUsedTime(searchTask.usedTime);

            RecipeCraftingContext context = null;
            try {
                context = searchTask.get();
                status = searchTask.getStatus();
            } catch (Exception e) {
                ModularMachinery.log.warn(ThrowableUtil.stackTraceToString(e));
            }
            searchTask = null;

            if (context == null) {
                ctrl.incrementRecipeSearchRetryCount();
                return;
            }

            if (context.canStartCrafting().isSuccess()) {
                setContext(context);
                this.activeRecipe = context.getActiveRecipe();
                this.status = CraftingStatus.SUCCESS;
                fireStartedEvent();
            } else {
                RecipeCraftingContextPool.returnCtx(context);
            }
        } else if (shouldSearchRecipe()) {
            createRecipeSearchTask();
        }
    }

    protected boolean shouldSearchRecipe() {
        return ctrl.isSearchRecipeImmediately() || (ctrl.getTicksExisted() % ctrl.currentRecipeSearchDelay() == 0);
    }

    public void invalidate() {
        setActiveRecipe(null).setContext(null);
        permanentModifiers.clear();
        semiPermanentModifiers.clear();

        if (searchTask == null || searchTask.isDone()) {
            return;
        }

        RecipeCraftingContext ctx;
        try {
            ctx = searchTask.get(50L, TimeUnit.MICROSECONDS);
            if (ctx != null) {
                RecipeCraftingContextPool.returnCtx(ctx);
            }
        } catch (TimeoutException ex) {
            ModularMachinery.log.warn("DIM: {} - {} controller is timeout to wait searchTask, discard search result.",
                    ctrl.getWorld().getWorldInfo().getWorldName(), ctrl.getPos());
        } catch (Exception ignored) {
        }
        searchTask = null;
    }

    protected abstract void createRecipeSearchTask();

    public void flushContextModifier() {
        if (context == null) {
            return;
        }
        context.overrideModifier(MiscUtils.flatten(ctrl.getFoundModifiers().values()));
        context.addModifier(ctrl.getCustomModifiers().values());
        context.addModifier(semiPermanentModifiers.values());
        context.addModifier(permanentModifiers.values());
    }

    public RecipeThread setActiveRecipe(ActiveMachineRecipe activeRecipe) {
        this.activeRecipe = activeRecipe;
        return this;
    }

    public RecipeCraftingContext getContext() {
        return context;
    }

    public RecipeThread setContext(RecipeCraftingContext context) {
        if (this.context != null && this.context != context) {
            RecipeCraftingContextPool.returnCtx(this.context);
        }
        this.context = context;
        return this;
    }

    public CraftingStatus getStatus() {
        return status;
    }

    public RecipeThread setStatus(CraftingStatus status) {
        this.status = status;
        return this;
    }

    public boolean isWaitForFinish() {
        return waitForFinish;
    }

    public Map<String, RecipeModifier> getPermanentModifiers() {
        return permanentModifiers;
    }

    public Map<String, RecipeModifier> getSemiPermanentModifiers() {
        return semiPermanentModifiers;
    }

    @ZenGetter("activeRecipe")
    public ActiveMachineRecipe getActiveRecipe() {
        return activeRecipe;
    }

    /**
     * 添加一个半永久配方修改器，它会在完成配方后被清空。
     *
     * @param name     名称
     * @param modifier 修改器
     */
    @ZenMethod
    public void addModifier(String name, RecipeModifier modifier) {
        semiPermanentModifiers.put(name, modifier);
        flushContextModifier();
    }

    /**
     * 删除一个半永久配方修改器。
     *
     * @param name 名称
     */
    @ZenMethod
    public void removeModifier(String name) {
        RecipeModifier removed = semiPermanentModifiers.remove(name);
        if (removed != null) {
            flushContextModifier();
        }
    }

    /**
     * 线程内是否存在指定名称的半永久配方修改器。
     *
     * @param name 名称
     */
    @ZenMethod
    public boolean hasModifier(String name) {
        return semiPermanentModifiers.containsKey(name);
    }

    /**
     * 添加一个永久配方修改器，它将永远存在这个线程中。
     * @param name     名称
     * @param modifier 修改器
     */
    @ZenMethod
    public void addPermanentModifier(String name, RecipeModifier modifier) {
        permanentModifiers.put(name, modifier);
        flushContextModifier();
    }

    /**
     * 线程内是否存在指定名称的永久配方修改器。
     *
     * @param name 名称
     */
    @ZenMethod
    public boolean hasPermanentModifier(String name) {
        return permanentModifiers.containsKey(name);
    }

    /**
     * 删除一个永久配方修改器。
     *
     * @param name 名称
     */
    @ZenMethod
    public void removePermanentModifier(String name) {
        RecipeModifier removed = permanentModifiers.remove(name);
        if ( removed!= null) {
            flushContextModifier();
        }
    }

    /**
     * 设置当前线程的状态信息。
     * @param info 信息
     */
    @ZenMethod
    public void setStatusInfo(String info) {
        status = new CraftingStatus(status.getStatus(), info);
    }
}
