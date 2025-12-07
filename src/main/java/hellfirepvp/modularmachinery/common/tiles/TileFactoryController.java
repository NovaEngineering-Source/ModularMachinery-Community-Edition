package hellfirepvp.modularmachinery.common.tiles;

import github.kasuminova.mmce.common.concurrent.FactoryRecipeSearchTask;
import github.kasuminova.mmce.common.concurrent.RecipeCraftingContextPool;
import github.kasuminova.mmce.common.concurrent.RecipeSearchTask;
import github.kasuminova.mmce.common.event.Phase;
import github.kasuminova.mmce.common.event.recipe.FactoryRecipeFailureEvent;
import github.kasuminova.mmce.common.event.recipe.FactoryRecipeFinishEvent;
import github.kasuminova.mmce.common.event.recipe.FactoryRecipeStartEvent;
import github.kasuminova.mmce.common.event.recipe.FactoryRecipeTickEvent;
import github.kasuminova.mmce.common.util.concurrent.SequentialTaskExecutor;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftingStatus;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.machine.RecipeThread;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import io.netty.util.internal.ThrowableUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

public class TileFactoryController extends TileMultiblockMachineController {
    private final Map<String, FactoryRecipeThread> coreRecipeThreads = new LinkedHashMap<>();
    private final List<FactoryRecipeThread>        recipeThreadList  = new LinkedList<>();
    private final List<ForkJoinTask<?>>            waitToExecute     = new ArrayList<>();
    private       CraftingStatus                   controllerStatus  = CraftingStatus.MISSING_STRUCTURE;
    private       int                              totalParallelism  = 1;
    private       int                              extraThreadCount  = 0;
    private       BlockFactoryController           parentController  = null;
    private       FactoryRecipeSearchTask          searchTask        = null;
    private       SequentialTaskExecutor           threadTask        = null;

    private boolean redstoneEffected = false;

    public TileFactoryController() {

    }

    public TileFactoryController(IBlockState state) {
        this();
        if (state.getBlock() instanceof BlockFactoryController) {
            parentController = (BlockFactoryController) state.getBlock();
            controllerRotation = state.getValue(BlockController.FACING);
            parentMachine = parentController.getParentMachine();
        } else {
            ModularMachinery.log.warn("Invalid factory controller block at {} !", getPos());
            controllerRotation = EnumFacing.NORTH;
        }
    }

    @Override
    public void doControllerTick() {
        if (getStrongPower() > 0) {
            redstoneEffected = true;
            return;
        }

        switch (workMode) {
            case ASYNC -> {
                if (executeGroupId == -1) {
                    tickExecutor = ModularMachinery.EXECUTE_MANAGER.addTask(() -> {
                        if (doAsyncStep()) {
                            return;
                        }
                        doSyncStep(false);
                    }, timeRecorder.usedTimeAvg());
                } else {
                    tickExecutor = ModularMachinery.EXECUTE_MANAGER.addExecuteGroupTask(() -> {
                        if (doAsyncStep()) {
                            return;
                        }
                        doSyncStep(false);
                    }, executeGroupId);
                }
            }
            case SEMI_SYNC -> {
                if (executeGroupId == -1) {
                    tickExecutor = ModularMachinery.EXECUTE_MANAGER.addTask(() -> {
                        if (doAsyncStep()) {
                            return;
                        }
                        ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> doSyncStep(true));
                    }, timeRecorder.usedTimeAvg());
                } else {
                    tickExecutor = ModularMachinery.EXECUTE_MANAGER.addExecuteGroupTask(() -> {
                        if (doAsyncStep()) {
                            return;
                        }
                        ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> doSyncStep(true));
                    }, executeGroupId);
                }
            }
            case SYNC -> {
                tickExecutor = null;
                if (doAsyncStep()) {
                    return;
                }
                doSyncStep(false);
            }
        }
    }

    protected boolean doAsyncStep() {
        return !doStructureCheck() || !isStructureFormed();
    }

    protected void doSyncStep(boolean recordTime) {
        long tickStart = recordTime ? System.nanoTime() : 0;

        onMachineTick(Phase.START);

        final boolean prevWorkingStatus = isWorking();

        executeSeqTask();

        if (hasIdleThread()) {
            searchAndStartRecipe();
        }

        updateCoreThread();

        if (!coreRecipeThreads.isEmpty() || !recipeThreadList.isEmpty()) {
            doRecipeTick();
            markNoUpdateSync();
        }
        searchRecipeImmediately = false;

        final boolean workingStatus = isWorking();
        if (prevWorkingStatus != workingStatus) {
            updateStatedMachineComponentSync(workingStatus);
        }

        onMachineTick(Phase.END);

        if (recordTime) {
            timeRecorder.incrementUsedTime((int) TimeUnit.MICROSECONDS.convert(System.nanoTime() - tickStart, TimeUnit.NANOSECONDS));
        }
    }

    @Override
    public CraftingStatus getControllerStatus() {
        return this.controllerStatus;
    }

    @Override
    public void setControllerStatus(CraftingStatus status) {
        this.controllerStatus = status;
    }

    @Override
    public int currentRecipeSearchDelay() {
        if (coreRecipeThreads.isEmpty()) {
            return super.currentRecipeSearchDelay();
        }
        return Math.min(20 + this.recipeResearchRetryCounter * Math.max(10 / coreRecipeThreads.size(), 1), 150);
    }

    /**
     * 工厂开始运行队列中的配方。
     */
    protected void doRecipeTick() {
        cleanIdleTimeoutThread();

        for (FactoryRecipeThread thread : coreRecipeThreads.values()) {
            if (thread.getActiveRecipe() == null) {
                thread.searchAndStartRecipe();
            }
            doThreadRecipeTick(thread);
        }

        for (FactoryRecipeThread thread : recipeThreadList) {
            doThreadRecipeTick(thread);
        }
    }

    /**
     * 工厂线程开始执行配方 Tick
     */
    protected void doThreadRecipeTick(FactoryRecipeThread thread) {
        ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();
        if (activeRecipe == null) {
            thread.idleTime++;
            return;
        }

        // If this thread previously failed in completing the recipe,
        // it retries to complete the recipe.
        if (thread.isWaitForFinish()) {
            // To prevent performance drain due to long output blocking,
            // try to complete the recipe every 10 Tick instead of every Tick.
            if (ticksExisted % 10 == 0) {
                thread.onFinished();
            }
            return;
        }

        if (thread.getContext() == null) {
            thread.setContext(thread.createContext(activeRecipe));
        }

        CraftingStatus status = thread.getStatus();

        // PreTickEvent
        FactoryRecipeTickEvent event = new FactoryRecipeTickEvent(thread, this, Phase.START);
        event.postEvent();
        if (event.isFailure()) {
            return;
        }

        // RecipeTick
        if (status != thread.getStatus()) {
            status = thread.getStatus();
            thread.onTick();
            thread.setStatus(status);
        } else {
            status = thread.onTick();
        }

        if (isNotWorking(thread, status)) {
            if (enableFullDataSync) {
                markForUpdateSync();
            } else {
                markNoUpdateSync();
            }
            return;
        }

        // PostTickEvent
        new FactoryRecipeTickEvent(thread, this, Phase.END).postEvent();

        if (thread.isCompleted()) {
            thread.onFinished();
        }
    }

    protected boolean isNotWorking(final FactoryRecipeThread thread, final CraftingStatus status) {
        if (status.isCrafting()) {
            return false;
        }
        boolean destruct = onThreadRecipeFailure(thread);
        if (destruct) {
            // Destruction recipe
            thread.setActiveRecipe(null).setContext(null).getSemiPermanentModifiers().clear();
        }
        return true;
    }

    /**
     * <p>工厂线程开始执行一个配方。</p>
     */
    public void onThreadRecipeStart(FactoryRecipeThread thread) {
        new FactoryRecipeStartEvent(thread, this).postEvent();
        ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();
        activeRecipe.start(thread.getContext());
        resetRecipeSearchRetryCount();
        if (enableFullDataSync) {
            markForUpdateSync();
        } else {
            markNoUpdateSync();
        }
    }

    public boolean onThreadRecipeFailure(FactoryRecipeThread thread) {
        ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();
        if (activeRecipe == null) {
            return false;
        }

        MachineRecipe recipe = activeRecipe.getRecipe();
        FactoryRecipeFailureEvent event = new FactoryRecipeFailureEvent(
            thread, this, thread.getStatus().getUnlocMessage(),
            recipe.doesCancelRecipeOnPerTickFailure());
        event.postEvent();

        return event.isDestructRecipe();
    }

    /**
     * <p>工厂线程完成一个配方。</p>
     */
    public void onThreadRecipeFinished(FactoryRecipeThread thread) {
        new FactoryRecipeFinishEvent(thread, this).postEvent();
        if (enableFullDataSync) {
            markForUpdateSync();
        } else {
            markNoUpdateSync();
        }
    }

    @Override
    protected void onStructureFormed() {
        super.onStructureFormed();

        coreRecipeThreads.clear();
        foundMachine.getCoreThreadPreset().forEach((threadName, thread) ->
            coreRecipeThreads.put(threadName, thread.copyCoreThread(this)));
    }

    protected void searchAndStartRecipe() {
        if (searchTask != null) {
            RecipeSearchTask task = searchTask;
            if (!task.isDone()) {
                return;
            }

            timeRecorder.addRecipeResearchUsedTime(searchTask.usedTime);

            RecipeCraftingContext context = null;
            try {
                context = task.get();
            } catch (Exception e) {
                ModularMachinery.log.warn(ThrowableUtil.stackTraceToString(e));
            }

            if (context != null) {
                if (context.canStartCrafting().isSuccess()) {
                    offerRecipe(context);
                    if (hasIdleThread()) {
                        createRecipeSearchTask();
                    }
                } else {
                    RecipeCraftingContextPool.returnCtx(context);
                }
            } else {
                incrementRecipeSearchRetryCount();
                CraftingStatus status = task.getStatus();
                if (status != null && !controllerStatus.equals(status)) {
                    controllerStatus = status;
                    markNoUpdateSync();
                }
            }
            searchTask = null;
        } else if (searchRecipeImmediately || (this.ticksExisted % currentRecipeSearchDelay() == 0)) {
            createRecipeSearchTask();
        }
    }

    protected void executeSeqTask() {
        if (threadTask != null) {
            if (!threadTask.isDone() || waitToExecute.isEmpty()) {
                return;
            }
        } else if (waitToExecute.isEmpty()) {
            return;
        }
        threadTask = new SequentialTaskExecutor(waitToExecute);
        waitToExecute.clear();
        ModularMachinery.EXECUTE_MANAGER.submitForkJoinTask(threadTask);
    }

    @Override
    protected void resetMachine(boolean clearData) {
        super.resetMachine(clearData);
        if (clearData) {
            extraThreadCount = 0;
        }
    }

    @Override
    protected void resetRecipe() {
        recipeThreadList.clear();
        coreRecipeThreads.clear();
    }

    public List<FactoryRecipeThread> getFactoryRecipeThreadList() {
        return recipeThreadList;
    }

    public Map<String, FactoryRecipeThread> getCoreRecipeThreads() {
        return coreRecipeThreads;
    }

    /**
     * 获取工厂最大并行数。
     * 服务端调用。
     */
    public int getAvailableParallelism() {
        int maxParallelism = getMaxParallelism();
        for (FactoryRecipeThread thread : recipeThreadList) {
            ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();
            if (activeRecipe == null) {
                continue;
            }
            maxParallelism -= (activeRecipe.getParallelism() - 1);
        }
        for (FactoryRecipeThread thread : coreRecipeThreads.values()) {
            ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();
            if (activeRecipe == null) {
                continue;
            }
            maxParallelism -= (activeRecipe.getParallelism() - 1);
        }

        return Math.max(1, maxParallelism);
    }

    /**
     * 获取工厂最大并行数。
     * 仅限客户端。
     */
    public int getTotalParallelism() {
        return totalParallelism;
    }

    public List<ForkJoinTask<?>> getWaitToExecute() {
        return waitToExecute;
    }

    public void offerRecipe(RecipeCraftingContext context) {
        for (FactoryRecipeThread thread : recipeThreadList) {
            if (thread.getActiveRecipe() == null) {
                thread.setContext(context)
                      .setActiveRecipe(context.getActiveRecipe())
                      .setStatus(CraftingStatus.SUCCESS);
                onThreadRecipeStart(thread);
                return;
            }
        }

        if (recipeThreadList.size() > getMaxThreads()) {
            return;
        }

        FactoryRecipeThread thread = new FactoryRecipeThread(this);
        thread.setContext(context)
              .setActiveRecipe(context.getActiveRecipe())
              .setStatus(CraftingStatus.SUCCESS);
        recipeThreadList.add(thread);
        onThreadRecipeStart(thread);
    }

    public int getMaxThreads() {
        return extraThreadCount + foundMachine.getMaxThreads();
    }

    @Override
    public void flushContextModifier() {
        recipeThreadList.forEach(FactoryRecipeThread::flushContextModifier);
    }

    protected void createRecipeSearchTask() {
        searchTask = new FactoryRecipeSearchTask(
            this,
            getFoundMachine(),
            getAvailableParallelism(),
            RecipeRegistry.getRecipesFor(foundMachine),
            null, getActiveRecipeList());
        waitToExecute.add(searchTask);
    }

    /**
     * 更新核心线程列表。
     */
    protected void updateCoreThread() {
        Map<String, FactoryRecipeThread> threads = foundMachine.getCoreThreadPreset();
        if (threads.isEmpty()) {
            coreRecipeThreads.clear();
            return;
        }

        if (!coreRecipeThreads.isEmpty() && ticksExisted % 20 != 0) {
            return;
        }

        threads.forEach((name, thread) -> {
            if (!coreRecipeThreads.containsKey(name)) {
                coreRecipeThreads.put(name, thread.copyCoreThread(this));
            }
        });

        Iterator<Map.Entry<String, FactoryRecipeThread>> it = coreRecipeThreads.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<String, FactoryRecipeThread> threadEntry = it.next();
            String name = threadEntry.getKey();
            FactoryRecipeThread thread = threads.get(name);
            if (thread == null) {
                it.remove();
                continue;
            }

            FactoryRecipeThread factoryThread = threadEntry.getValue();
            Set<MachineRecipe> recipeSet = factoryThread.getRecipeSet();
            recipeSet.clear();
            recipeSet.addAll(thread.getRecipeSet());
        }
    }

    /**
     * 清理闲置时间过长的线程。
     */
    protected void cleanIdleTimeoutThread() {
        if (ticksExisted % 20 != 0) {
            return;
        }
        recipeThreadList.removeIf(thread -> thread.isIdle() && thread.idleTime >= FactoryRecipeThread.IDLE_TIME_OUT);
    }

    public boolean hasIdleThread() {
        if (recipeThreadList.size() < getMaxThreads()) {
            return true;
        }

        for (FactoryRecipeThread thread : recipeThreadList) {
            if (thread.getActiveRecipe() == null) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void updateComponents() {
        super.updateComponents();
        for (final FactoryRecipeThread thread : recipeThreadList) {
            RecipeCraftingContext ctx = thread.getContext();
            if (ctx == null) {
                continue;
            }
            ctx.updateComponents(foundComponents);
        }

        for (final FactoryRecipeThread thread : coreRecipeThreads.values()) {
            RecipeCraftingContext ctx = thread.getContext();
            if (ctx == null) {
                continue;
            }
            ctx.updateComponents(foundComponents);
        }
    }

    @Override
    protected boolean canCheckStructure() {
        if (redstoneEffected) {
            redstoneEffected = false;
            return true;
        }
        return super.canCheckStructure();
    }

    @Override
    protected void checkRotation() {
        IBlockState state = getWorld().getBlockState(getPos());
        if (state.getBlock() instanceof BlockFactoryController) {
            this.parentController = (BlockFactoryController) state.getBlock();
            this.parentMachine = parentController.getParentMachine();
            this.controllerRotation = state.getValue(BlockController.FACING);
        } else {
            ModularMachinery.log.warn("Invalid factory controller block at {} !", getPos());
            controllerRotation = EnumFacing.NORTH;
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        if (!isStructureFormed()) {
            return;
        }

        parentController = BlockFactoryController.FACTORY_CONTROLLERS.get(parentMachine);

        if (compound.hasKey("status")) {
            controllerStatus = CraftingStatus.deserialize(compound.getCompoundTag("status"));
        }

        extraThreadCount = compound.getByte("extraThreadCount");

        recipeThreadList.clear();
        coreRecipeThreads.clear();

        if (compound.hasKey("threadList", Constants.NBT.TAG_LIST)) {
            NBTTagList threadList = compound.getTagList("threadList", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < threadList.tagCount(); i++) {
                NBTTagCompound tagAt = threadList.getCompoundTagAt(i);
                FactoryRecipeThread thread = FactoryRecipeThread.deserialize(tagAt, this);
                if (thread != null) {
                    recipeThreadList.add(thread);
                }
            }
        }

        if (compound.hasKey("coreThreadList", Constants.NBT.TAG_LIST)) {
            NBTTagList threadList = compound.getTagList("coreThreadList", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < threadList.tagCount(); i++) {
                NBTTagCompound tagAt = threadList.getCompoundTagAt(i);
                FactoryRecipeThread thread = FactoryRecipeThread.deserialize(tagAt, this);
                if (thread != null) {
                    coreRecipeThreads.put(thread.getThreadName(), thread);
                }
            }
        }

        if (compound.hasKey("totalParallelism")) {
            totalParallelism = compound.getInteger("totalParallelism");
        }
    }

    @Override
    protected void readMachineNBT(NBTTagCompound compound) {
        if (compound.hasKey("parentMachine")) {
            ResourceLocation rl = new ResourceLocation(compound.getString("parentMachine"));
            parentMachine = MachineRegistry.getRegistry().getMachine(rl);
            if (parentMachine != null) {
                parentController = BlockFactoryController.FACTORY_CONTROLLERS.get(parentMachine);
            } else {
                ModularMachinery.log.info("Couldn't find machine named {} for controller at {}", rl, getPos());
            }
        }
        super.readMachineNBT(compound);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        if (!isStructureFormed()) {
            return;
        }

        if (!recipeThreadList.isEmpty()) {
            NBTTagList threadList = new NBTTagList();
            recipeThreadList.forEach(thread -> threadList.appendTag(thread.serialize()));
            compound.setTag("threadList", threadList);
        }

        if (!coreRecipeThreads.isEmpty()) {
            NBTTagList threadList = new NBTTagList();
            coreRecipeThreads.values().forEach(thread -> threadList.appendTag(thread.serialize()));
            compound.setTag("coreThreadList", threadList);
        }

        compound.setTag("status", controllerStatus.serialize());
        compound.setInteger("totalParallelism", getMaxParallelism());
        compound.setShort("extraThreadCount", (short) extraThreadCount);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        for (final FactoryRecipeThread thread : recipeThreadList) {
            thread.invalidate();
        }
        recipeThreadList.clear();
        for (final FactoryRecipeThread thread : coreRecipeThreads.values()) {
            thread.invalidate();
        }
        coreRecipeThreads.clear();
    }

    @Nullable
    @Override
    public ActiveMachineRecipe getActiveRecipe() {
        ActiveMachineRecipe[] activeRecipes = getActiveRecipeList();
        return activeRecipes.length == 0 ? null : activeRecipes[0];
    }

    @Nonnull
    @Override
    public ActiveMachineRecipe[] getActiveRecipeList() {
        List<ActiveMachineRecipe> list = new ArrayList<>();
        for (FactoryRecipeThread thread : coreRecipeThreads.values()) {
            ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();
            if (activeRecipe != null) {
                list.add(activeRecipe);
            }
        }
        for (FactoryRecipeThread thread : recipeThreadList) {
            ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();
            if (activeRecipe != null) {
                list.add(activeRecipe);
            }
        }
        return list.toArray(new ActiveMachineRecipe[0]);
    }

    @Override
    public RecipeThread[] getRecipeThreadList() {
        List<RecipeThread> list = new ArrayList<>();
        list.addAll(coreRecipeThreads.values());
        list.addAll(recipeThreadList);
        return list.toArray(new RecipeThread[0]);
    }

    @Override
    public int getExtraThreadCount() {
        return extraThreadCount;
    }

    @Override
    public void setExtraThreadCount(final int extraThreadCount) {
        this.extraThreadCount = extraThreadCount;
    }

    @Override
    public boolean isWorking() {
        if (lastStrongPower > 0) {
            return false;
        }
        if (coreRecipeThreads.isEmpty() && recipeThreadList.isEmpty()) {
            return false;
        }
        for (FactoryRecipeThread thread : coreRecipeThreads.values()) {
            if (thread.getActiveRecipe() != null && thread.getStatus().isCrafting()) {
                return true;
            }
        }
        for (FactoryRecipeThread thread : recipeThreadList) {
            if (thread.getActiveRecipe() != null && thread.getStatus().isCrafting()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addModifier(final String key, final RecipeModifier modifier) {
        coreRecipeThreads.values().forEach(thread -> thread.addModifier(key, modifier));
        recipeThreadList.forEach(thread -> thread.addModifier(key, modifier));
    }

    @Override
    public void removeModifier(final String key) {
        coreRecipeThreads.values().forEach(thread -> thread.removeModifier(key));
        recipeThreadList.forEach(thread -> thread.removeModifier(key));
    }

    @Override
    @Deprecated
    public void overrideStatusInfo(String newInfo) {

    }
}
