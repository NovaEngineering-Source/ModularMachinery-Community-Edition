package github.kasuminova.mmce.common.integration.groovyscript;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.api.GroovyLog;
import github.kasuminova.mmce.common.event.client.ControllerGUIRenderEvent;
import github.kasuminova.mmce.common.event.client.ControllerModelAnimationEvent;
import github.kasuminova.mmce.common.event.client.ControllerModelGetEvent;
import github.kasuminova.mmce.common.event.machine.*;
import groovy.lang.Closure;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.RecipeFailureActions;
import hellfirepvp.modularmachinery.common.machine.TaggedPositionBlockArray;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.modifier.MultiBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.modifier.SingleBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.IBlockStateDescriptor;
import hellfirepvp.modularmachinery.common.util.SmartInterfaceType;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;

import java.util.*;

public class GroovyMachineBuilder extends BlockArrayBuilder {

    public static final Map<ResourceLocation, DynamicMachine> PRE_LOAD_MACHINES = new Object2ObjectOpenHashMap<>();

    private final DynamicMachine machine;
    private final Map<BlockPos, List<SingleBlockModifierReplacement>> blockModifierMap = new Object2ObjectOpenHashMap<>();
    private final Char2ObjectMap<List<SingleBlockModifierReplacement>> blockModifierCharMap = new Char2ObjectOpenHashMap<>();

    public GroovyMachineBuilder(String registryName) {
        this.machine = new DynamicMachine(registryName);
        this.blockArray = this.machine.getPattern();
        color(0xFFFFFF); // config is read after grs preInit
    }

    public static GroovyMachineBuilder builder(String registryName) {
        return new GroovyMachineBuilder(registryName);
    }

    /**
     * 获取机械结构组成。
     */
    public TaggedPositionBlockArray getBlockArray() {
        return blockArray;
    }

    /**
     * 设置此机械是否受并行控制器影响。
     */
    public GroovyMachineBuilder parallelizable(boolean isParallelizable) {
        machine.setParallelizable(isParallelizable);
        return this;
    }

    /**
     * 设置此机械的最大并行数。
     *
     * @param maxParallelism 并行数
     */
    public GroovyMachineBuilder maxParallelism(int maxParallelism) {
        machine.setMaxParallelism(maxParallelism);
        return this;
    }

    /**
     * 设置此机械的内置并行数。
     *
     * @param parallelism 内置并行数
     */
    public GroovyMachineBuilder internalParallelism(int parallelism) {
        machine.setInternalParallelism(parallelism);
        return this;
    }

    public GroovyMachineBuilder blockModifier(int x, int y, int z, IBlockState blockStates, String description, RecipeModifier... modifiers) {
        return blockModifier(x, y, z, Collections.singletonList(blockStates), description, modifiers);
    }

    /**
     * 添加单方块配方修改器。
     *
     * @param x           X
     * @param y           Y
     * @param z           Z
     * @param blockStates BlockState
     * @param description 描述
     * @param modifiers   修改器列表
     */
    public GroovyMachineBuilder blockModifier(int x, int y, int z, Iterable<IBlockState> blockStates, String description, RecipeModifier... modifiers) {
        List<IBlockStateDescriptor> stateDescriptorList = new ArrayList<>();
        for (IBlockState blockState : blockStates) {
            stateDescriptorList.add(new IBlockStateDescriptor(blockState));
        }
        singleBlockModifier(new BlockPos(x, y, z), new BlockArray.BlockInformation(stateDescriptorList), description, modifiers);
        return this;
    }

    public GroovyMachineBuilder whereBlockModifier(String c, IBlockState blockStates, String description, RecipeModifier... modifiers) {
        return whereBlockModifier(c, Collections.singletonList(blockStates), description, modifiers);
    }

    public GroovyMachineBuilder whereBlockModifier(String c, Iterable<IBlockState> blockStates, String description, RecipeModifier... modifiers) {
        where(c, () -> {
            List<IBlockStateDescriptor> stateDescriptorList = new ArrayList<>();
            for (IBlockState blockState : blockStates) {
                stateDescriptorList.add(new IBlockStateDescriptor(blockState));
            }
            singleBlockModifier(c.charAt(0), new BlockArray.BlockInformation(stateDescriptorList), description, modifiers);
        });
        return this;
    }

    /**
     * 添加多方块升级配方修改器。
     */
    public GroovyMachineBuilder multiBlockModifier(MultiBlockModifierReplacement multiBlockModifier) {
        if (multiBlockModifier != null) {
            machine.getMultiBlockModifiers().add(multiBlockModifier);
        }
        return this;
    }

    /**
     * 添加智能数据接口类型。
     *
     * @param type 类型
     */
    public GroovyMachineBuilder smartInterfaceType(SmartInterfaceType type) {
        if (!machine.hasSmartInterfaceType(type.getType())) {
            machine.addSmartInterfaceType(type);
        } else {
            GroovyLog.get().warn("DynamicMachine `" + machine.getRegistryName() + "` is already has SmartInterfaceType `" + type.getType() + "`!");
        }
        return this;
    }

    /**
     * 添加结构形成事件监听器。
     */
    public GroovyMachineBuilder structureFormedHandler(Closure<?> function) {
        machine.addMachineEventHandler(MachineStructureFormedEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 添加结构更新事件监听器。
     */
    public GroovyMachineBuilder structureUpdateHandler(Closure<?> function) {
        machine.addMachineEventHandler(MachineStructureUpdateEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 添加机器事件监听器。
     */
    public GroovyMachineBuilder tickHandler(Closure<?> function) {
        machine.addMachineEventHandler(MachineTickEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 添加控制器 GUI 渲染事件监听器。
     */
    public GroovyMachineBuilder guiRenderHandler(Closure<?> function) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return this;
        }
        machine.addMachineEventHandler(ControllerGUIRenderEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 添加控制器 GeckoLib 模型动画事件监听器。
     */
    @Optional.Method(modid = "geckolib3")
    public GroovyMachineBuilder controllerModelAnimationHandler(Closure<?> function) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return this;
        }
        machine.addMachineEventHandler(ControllerModelAnimationEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 添加控制器 GeckoLib 模型获取事件监听器。
     */
    @Optional.Method(modid = "geckolib3")
    public GroovyMachineBuilder controllerModelGetHandler(Closure<?> function) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return this;
        }
        machine.addMachineEventHandler(ControllerModelGetEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 添加智能数据接口更新事件监听器
     */
    public GroovyMachineBuilder smartInterfaceUpdateHandler(Closure<?> function) {
        machine.addMachineEventHandler(SmartInterfaceUpdateEvent.class, IEventHandler.of(function));
        return this;
    }

    /**
     * 控制器是否需要蓝图
     */
    public GroovyMachineBuilder requiresBlueprint(boolean requiresBlueprint) {
        this.machine.setRequiresBlueprint(requiresBlueprint);
        return this;
    }

    /**
     * 设置当此机器配方运行失败时的操作
     *
     * @param failureAction Action 可以通过 RecipeFailureActions.getFailureAction(String key) 获得
     */
    public GroovyMachineBuilder failureAction(RecipeFailureActions failureAction) {
        this.machine.setFailureAction(failureAction);
        return this;
    }

    /**
     * 设置机械颜色，该结构内的其他组件也将会变为此颜色。
     *
     * @param color 颜色，例如：0xFFFFFF
     */
    public GroovyMachineBuilder color(int color) {
        this.machine.setDefinedColor(color);
        return this;
    }

    /**
     * 设置此机械是否有工厂形式的控制器。
     *
     * @param hasFactory true 即为注册，false 即为不注册
     */
    public GroovyMachineBuilder hasFactory(boolean hasFactory) {
        this.machine.setHasFactory(hasFactory);
        return this;
    }

    /**
     * 设置此机械是否仅有工厂形式的控制器。
     *
     * @param factoryOnly true 即为仅工厂，false 即为普通机械和工厂
     */
    public GroovyMachineBuilder factoryOnly(boolean factoryOnly) {
        this.machine.setFactoryOnly(factoryOnly);
        return this;
    }

    /**
     * 设置此机械的工厂最大线程数。
     *
     * @param maxThreads 最大线程数
     */
    public GroovyMachineBuilder maxThreads(int maxThreads) {
        this.machine.setMaxThreads(maxThreads);
        return this;
    }

    public GroovyMachineBuilder coreThread(FactoryRecipeThread thread) {
        this.machine.addCoreThread(thread);
        return this;
    }

    /**
     * 注册此机械。
     */
    public Object build() {
        if(PRE_LOAD_MACHINES.containsKey(this.machine.getRegistryName())) {
            throw new IllegalStateException("Machine with name " + this.machine.getRegistryName().getPath() + " already exists!");
        }
        super.build();
        hellfirepvp.modularmachinery.common.integration.crafttweaker.MachineBuilder.WAIT_FOR_LOAD.add(this.machine);
        PRE_LOAD_MACHINES.put(this.machine.getRegistryName(), this.machine);
        return machine;
    }

    @Override
    protected void onAddBlock(char c, BlockPos pos, BlockArray.BlockInformation info) {
        super.onAddBlock(c, pos, info);
        List<SingleBlockModifierReplacement> modifiers = this.blockModifierMap.get(pos);
        if (modifiers != null) {
            modifiers.forEach(modifier -> modifier.setPos(pos));
            this.machine.getModifiers().computeIfAbsent(pos, k -> new ArrayList<>()).addAll(modifiers);
        }
        modifiers = this.blockModifierCharMap.get(c);
        if (modifiers != null) {
            modifiers.forEach(modifier -> modifier.setPos(pos));
            this.machine.getModifiers().computeIfAbsent(pos, k -> new ArrayList<>()).addAll(modifiers);
        }
    }

    private void singleBlockModifier(BlockPos pos, BlockArray.BlockInformation information, String description, RecipeModifier... modifiers) {
        this.lastInformation = information;
        this.lastChar = Character.MIN_VALUE;
        this.blockModifierMap.computeIfAbsent(pos, k -> new ArrayList<>())
                .add(new SingleBlockModifierReplacement(information, Arrays.asList(modifiers), description));
    }

    private void singleBlockModifier(char c, BlockArray.BlockInformation information, String description, RecipeModifier... modifiers) {
        this.lastInformation = information;
        this.lastChar = c;
        this.blockModifierCharMap.computeIfAbsent(c, k -> new ArrayList<>())
                .add(new SingleBlockModifierReplacement(information, Arrays.asList(modifiers), description));
    }

    public DynamicMachine getMachine() {
        return machine;
    }
}
