package hellfirepvp.modularmachinery.common.machine.factory;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.concurrent.FactoryRecipeSearchTask;
import github.kasuminova.mmce.common.util.concurrent.Action;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftingStatus;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.machine.RecipeThread;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.TileFactoryController;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.*;

/**
 * <p>TileFactoryController 的一部分，存储单独的配方运行数据。</p>
 * <p>不是真正意义上的线程。</p>
 * <p>Part of the TileFactoryController that stores data for separate recipe runs.</p>
 * <p>Not a thread in the true sense of the word.</p>
 */
@ZenRegister
@ZenClass("mods.modularmachinery.FactoryRecipeThread")
public class FactoryRecipeThread extends RecipeThread {
    public static final int IDLE_TIME_OUT = 200;
    public static final List<Action> WAIT_FOR_ADD = new ArrayList<>();
    private final TreeSet<MachineRecipe> recipeSet = new TreeSet<>();
    public int idleTime = 0;
    private TileFactoryController factory;
    private boolean isCoreThread;
    private String threadName;

    public FactoryRecipeThread(TileFactoryController factory) {
        this(factory, false, "", Collections.emptySet(), Collections.emptyMap());
    }

    public FactoryRecipeThread(
            TileFactoryController factory,
            boolean isCoreThread,
            String threadName,
            Set<MachineRecipe> recipeSet,
            Map<String, RecipeModifier> permanentModifiers) {
        super(factory);
        this.factory = factory;
        this.isCoreThread = isCoreThread;
        this.threadName = threadName;
        this.recipeSet.addAll(recipeSet);
        this.permanentModifiers.putAll(permanentModifiers);
    }

    @ZenMethod
    public static FactoryRecipeThread createCoreThread(String threadName) {
        return new FactoryRecipeThread(null, true, threadName, Collections.emptySet(), Collections.emptyMap());
    }

    public CraftingStatus onTick() {
        CraftingStatus status = super.onTick();
        if (status.isCrafting()) {
            this.idleTime = 0;
        }
        return status;
    }

    @Override
    public void fireStartedEvent() {
        factory.onThreadRecipeStart(this);
    }

    @Override
    public void fireFinishedEvent() {
        factory.onThreadRecipeFinished(this);
    }

    public void tryRestartRecipe() {
        activeRecipe.reset();
        activeRecipe.setMaxParallelism(factory.getAvailableParallelism());
        RecipeCraftingContext context = getContext().reset();
        flushContextModifier();

        RecipeCraftingContext.CraftingCheckResult result = factory.onRestartCheck(context);
        if (result.isSuccess()) {
            factory.onThreadRecipeStart(this);
        } else {
            activeRecipe = null;
            setContext(null);
            status = CraftingStatus.failure(result.getFirstErrorMessage(""));
            if (isCoreThread) {
                createRecipeSearchTask();
            }
        }
    }

    protected void createRecipeSearchTask() {
        TileFactoryController factory = this.factory;
        Iterable<MachineRecipe> recipeSet = this.recipeSet.isEmpty() ? RecipeRegistry.getRecipesFor(factory.getFoundMachine()) : this.recipeSet;
        searchTask = new FactoryRecipeSearchTask(
                factory,
                factory.getFoundMachine(),
                factory.getAvailableParallelism(),
                recipeSet,
                this,
                factory.getActiveRecipeList());
        factory.getWaitToExecute().add(searchTask);
    }

    public NBTTagCompound serialize() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("status", status.serialize());
        if (activeRecipe != null && activeRecipe.getRecipe() != null) {
            tag.setTag("activeRecipe", activeRecipe.serialize());
        }
        if (isCoreThread) {
            tag.setString("coreThreadName", threadName);
        }
        if (!permanentModifiers.isEmpty()) {
            NBTTagList tagList = new NBTTagList();
            permanentModifiers.forEach((key, modifier) -> {
                if (key != null && modifier != null) {
                    NBTTagCompound modifierTag = new NBTTagCompound();
                    modifierTag.setString("key", key);
                    modifierTag.setTag("modifier", modifier.serialize());
                    tagList.appendTag(modifierTag);
                }
            });
            tag.setTag("permanentModifiers", tagList);
        }
        if (!semiPermanentModifiers.isEmpty()) {
            NBTTagList tagList = new NBTTagList();
            semiPermanentModifiers.forEach((key, modifier) -> {
                if (key != null && modifier != null) {
                    NBTTagCompound modifierTag = new NBTTagCompound();
                    modifierTag.setString("key", key);
                    modifierTag.setTag("modifier", modifier.serialize());
                    tagList.appendTag(modifierTag);
                }
            });
            tag.setTag("semiPermanentModifiers", tagList);
        }
        return tag;
    }

    public static FactoryRecipeThread deserialize(NBTTagCompound tag, TileFactoryController factory) {
        if (!tag.hasKey("status")) {
            return null;
        }

        Map<String, RecipeModifier> permanentModifiers = new HashMap<>();
        if (tag.hasKey("permanentModifiers", Constants.NBT.TAG_LIST)) {
            NBTTagList tagList = tag.getTagList("permanentModifiers", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound modifierTag = tagList.getCompoundTagAt(i);
                permanentModifiers.put(modifierTag.getString("key"), RecipeModifier.deserialize(modifierTag.getCompoundTag("modifier")));
            }
        }

        Map<String, RecipeModifier> semiPermanentModifiers = new HashMap<>();
        if (tag.hasKey("semiPermanentModifiers", Constants.NBT.TAG_LIST)) {
            NBTTagList tagList = tag.getTagList("semiPermanentModifiers", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound modifierTag = tagList.getCompoundTagAt(i);
                semiPermanentModifiers.put(modifierTag.getString("key"), RecipeModifier.deserialize(modifierTag.getCompoundTag("modifier")));
            }
        }

        ActiveMachineRecipe activeRecipe = deserializeActiveRecipe(tag, factory);

        // https://github.com/KasumiNova/ModularMachinery-Community-Edition/issues/34
        if (factory.getFoundMachine() != null
                && activeRecipe != null
                && !activeRecipe.getRecipe().getOwningMachineIdentifier().equals(factory.getFoundMachine().getRegistryName())) {
            activeRecipe = null;
        }

        FactoryRecipeThread thread = (FactoryRecipeThread) new FactoryRecipeThread(factory)
                .setActiveRecipe(activeRecipe)
                .setStatus(CraftingStatus.deserialize(tag.getCompoundTag("status")));
        thread.permanentModifiers.putAll(permanentModifiers);
        thread.semiPermanentModifiers.putAll(semiPermanentModifiers);

        // Core Thread
        if (tag.hasKey("coreThreadName")) {
            Map<String, FactoryRecipeThread> threads = factory.getFoundMachine().getCoreThreadPreset();
            FactoryRecipeThread coreThread = threads.get(tag.getString("coreThreadName"));
            if (coreThread == null) {
                return thread;
            }
            return coreThread.copyDataToAnother(factory, thread);
        }
        // Simple Thread
        return thread;
    }

    public TileFactoryController getFactory() {
        return factory;
    }

    /**
     * 当前线程是否在工作。
     */
    @ZenGetter("isWorking")
    public boolean isWorking() {
        return status.getStatus() == TileMultiblockMachineController.Type.CRAFTING;
    }

    /**
     * 当前线程是否闲置
     */
    @ZenGetter("isIdle")
    public boolean isIdle() {
        return activeRecipe == null && getContext() == null;
    }

    /**
     * 当前线程是否为核心线程（即始终存在）
     */
    @ZenGetter("isCoreThread")
    public boolean isCoreThread() {
        return isCoreThread;
    }

    /**
     * 当前线程的名称（仅限核心线程）
     */
    @ZenGetter("threadName")
    public String getThreadName() {
        return threadName;
    }

    /**
     * 为当前线程添加固定配方（仅核心线程生效）。
     */
    @ZenMethod
    public FactoryRecipeThread addRecipe(String recipeName) {
        if (factory != null) {
            MachineRecipe recipe = RecipeRegistry.getRecipe(new ResourceLocation(ModularMachinery.MODID, recipeName));
            if (recipe != null) {
                addRecipe(recipe);
            } else {
                CraftTweakerAPI.logError("[ModularMachinery] Cloud not found recipe by name " + recipeName + "!");
            }
        } else {
            WAIT_FOR_ADD.add(() -> {
                MachineRecipe recipe = RecipeRegistry.getRecipe(new ResourceLocation(ModularMachinery.MODID, recipeName));
                if (recipe != null) {
                    addRecipe(recipe);
                } else {
                    CraftTweakerAPI.logError("[ModularMachinery] Cloud not found recipe by name " + recipeName + "!");
                }
            });
        }

        return this;
    }

    public FactoryRecipeThread addRecipe(MachineRecipe recipe) {
        recipeSet.add(recipe);
        return this;
    }

    public TreeSet<MachineRecipe> getRecipeSet() {
        return recipeSet;
    }

    public FactoryRecipeThread copyCoreThread(TileFactoryController factory) {
        return new FactoryRecipeThread(factory, true, threadName, recipeSet, permanentModifiers);
    }

    public FactoryRecipeThread copyDataToAnother(TileFactoryController factory, FactoryRecipeThread another) {
        another.factory = factory;
        another.isCoreThread = isCoreThread;
        another.threadName = threadName;
        another.recipeSet.addAll(recipeSet);
        another.permanentModifiers.putAll(permanentModifiers);

        return another;
    }
}
