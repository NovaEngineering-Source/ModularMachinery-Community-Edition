/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentRequirement
 * Created by HellFirePvP
 * Date: 28.06.2017 / 10:34
 */
public abstract class ComponentRequirement<T, V extends RequirementType<T, ? extends ComponentRequirement<T, V>>> {

    public static final int PRIORITY_WEIGHT_ENERGY = 50_000_000;
    public static final int PRIORITY_WEIGHT_FLUID  = 100;
    public static final int PRIORITY_WEIGHT_ITEM   = 50_000;

    public final    V                    requirementType;
    protected final IOType               actionType;
    protected       ComponentSelectorTag tag = null;

    protected boolean triggered         = false;
    protected boolean triggerRepeatable = false;
    protected int     triggerTime       = 0;

    protected boolean ignoreOutputCheck = false;

    public ComponentRequirement(V requirementType, IOType actionType) {
        this.requirementType = requirementType;
        this.actionType = actionType;
    }

    public final V getRequirementType() {
        return requirementType;
    }

    public final IOType getActionType() {
        return actionType;
    }

    public final ComponentSelectorTag getTag() {
        return tag;
    }

    public final void setTag(ComponentSelectorTag tag) {
        this.tag = tag;
    }

    public int getTriggerTime() {
        return triggerTime;
    }

    /**
     * <p>
     * 设置该需求只会在特定时间触发。<br/>
     * 在此之前 {@link #startCrafting(ProcessingComponent, RecipeCraftingContext, ResultChance)} 不会被触发。<br/>
     * </p>
     * <p>
     * Set the requirement to be triggered only at a specific time.<br/>
     * Until then {@link #startCrafting(ProcessingComponent, RecipeCraftingContext, ResultChance)} will not be triggered.<br/>
     * </p>
     *
     * @param tickTime 触发时间，如果存在修改时间的配方修改器，则最终触发时间也会被修改器影响。<br/>
     *                 Trigger time, if a recipe modifier exists that modifies the time,
     *                 the final trigger time will also be affected by the modifier.
     */
    public void setTriggerTime(int tickTime) {
        triggerTime = tickTime;
    }

    public boolean isTriggerRepeatable() {
        return triggerRepeatable;
    }

    /**
     * <p>
     * 通常情况下每个需求在每次工作中只能触发一次，但是如果该方法提供的形参为 true 时，则可以被触发多次。
     * </p>
     * <p>
     * Normally each requirement can only be triggered once per job,
     * but can be triggered multiple times if the method is supplied with a true formal parameter.
     * </p>
     */
    public void setTriggerRepeatable(boolean triggerRepeatable) {
        this.triggerRepeatable = triggerRepeatable;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered(final boolean triggered) {
        this.triggered = triggered;
    }

    public int getSortingWeight() {
        return 0;
    }

    /**
     * <p>
     * Return true here to indicate the passed {@link ProcessingComponent} is valid for the methods:<br/>
     * - {@link #startCrafting(ProcessingComponent, RecipeCraftingContext, ResultChance)}<br/>
     * - {@link #finishCrafting(ProcessingComponent, RecipeCraftingContext, ResultChance)}<br/>
     * - {@link #canStartCrafting(ProcessingComponent, RecipeCraftingContext, List)}<br/>
     * <p>
     * and for {@link PerTick} instances:<br/>
     * - {@link PerTick#doIOTick(ProcessingComponent, RecipeCraftingContext)}
     * </p>
     *
     * @param component The component to test
     * @param ctx       The context to test in
     * @return true, if the component is valid for further processing by the specified methods, false otherwise
     */
    public abstract boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx);

    /**
     * True if the requirement could be fulfilled by the given component.
     */
    public boolean startCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        return true;
    }

    /**
     * <p>
     * 完成配方时首先会调用 {@link #canStartCrafting(ProcessingComponent, RecipeCraftingContext, List)} 方法，
     * 只有所有需求都当满足条件时，才会调用此方法来完成配方工作。
     * </p>
     * <p>
     * The {@link #canStartCrafting(ProcessingComponent, RecipeCraftingContext, List)} method will be called first to complete the recipe,
     * and will only be called if all requirements are met.
     * </p>
     */
    @Nonnull
    public CraftCheck finishCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        return CraftCheck.success();
    }

    /**
     * <p>
     * 此方法会在配方开始前和配方结束前检查需求是否满足要求，如果有任何一个需求无法满足要求，
     * 则不会完成配方的后续操作，并返回一个错误。
     * </p>
     * <p>
     * This method checks that the requirements are met before the recipe starts and before the recipe ends,
     * and if any of the requirements cannot be met.
     * then subsequent operations of the recipe will not be completed and an error will be returned.
     * </p>
     */
    @Nonnull
    public abstract CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions);

    /**
     * <p>复制一份需求，其内容与原内容相等。<br/>
     * <strong>在 MMCE 中必须实现此方法，不得返回自身。</strong></p>
     *
     * <p>Make a copy of the Requirement with contents equal to the original.<br/>
     * <strong>In MMCE it must be implemented and must not return itself.</strong></p>
     */
    public abstract ComponentRequirement<T, V> deepCopy();

    /**
     * <p>
     * 其实现原理和 {@link #deepCopy} 相同，但是新增了 modifiers 形参，通常被 {@link RecipeAdapter} 或其他功能调用。<br/>
     * 可以被忽略，或直接返回 {@link #deepCopy} 的实现。<br/>
     * </p>
     * <p>
     * The implementation is the same as {@link #deepCopy}, but with the addition of modifiers,
     * which are usually called by {@link RecipeAdapter} or other functions.<br/>
     * Can be ignored or just return an implementation of {@link #deepCopy}.<br/>
     * </p>
     */
    public abstract ComponentRequirement<T, V> deepCopyModified(List<RecipeModifier> modifiers);

    public ComponentRequirement<T, V> postDeepCopy(ComponentRequirement<?, ?> another) {
        this.tag = another.tag;
        this.triggerTime = another.triggerTime;
        this.triggerRepeatable = another.triggerRepeatable;
        this.ignoreOutputCheck = another.ignoreOutputCheck;
        return this;
    }

    /**
     * <p>
     * 通常用于初始化需求的检查，允许在这个阶段初始化某些检查数据。<br/>
     * 可以不实现。<br/>
     * </p>
     * <p>
     * Typically used to initialize Requirements checks, allowing certain check data to be initialized at this stage.<br/>
     * It can be unimplemented.<br/>
     * </p>
     */
    public void startRequirementCheck(ResultChance contextChance, RecipeCraftingContext context) {

    }

    /**
     * <p>
     * 通常用于重置需求的检查，通常在需求检查完毕后被调用。<br/>
     * 可以不实现。<br/>
     * </p>
     * <p>
     * Typically used to reset a requirement check, and is usually called after the requirement check is complete.<br/>
     * It can be unimplemented.<br/>
     * </p>
     */
    public void endRequirementCheck() {

    }

    /**
     * <p>
     * Previously in ComponentType.getMissingComponentErrorMessage.<br/>
     * Should return an unlocalized error message to display if no component for the given io-type was found.<br/>
     * i.e. a recipe has an item output, but there's no item output bus on the machine at all.<br/>
     * Overwrite this if necessary at all.<br/>
     * </p>
     */
    @Nonnull
    public abstract String getMissingComponentErrorMessage(IOType ioType);

    /**
     * <p>
     * Be sure, that if you specify a new object here as type that you register that along with a helper and renderer
     * in the JEI Integration! Otherwise JEI will complain about not having proper handling for this.<br/>
     * Also, be sure that this generic T is the <strong>only one</strong> with that type otherwise internally stuff might break...<br/>
     * </p>
     */
    public abstract JEIComponent<T> provideJEIComponent();

    public void initializeJEIRequirements() {
    }

    public boolean isIgnoreOutputCheck() {
        return ignoreOutputCheck;
    }

    /**
     * <p>
     * 设置该需求是否忽略输出检查。
     * 通常情况下，如果设置为 true，则需求不应返回 {@link CraftCheck#failure(String)} 错误。
     * 注意：应当仅忽略输出检查，真实的输出操作应当正常执行（即便可能会溢出丢失等情况）。
     * </p>
     * <p>
     * Sets whether the requirement ignores output checks.
     * In general, if set to true, the requirement should not return {@link CraftCheck#failure(String)} errors.
     * Note: Only output checks should be ignored,
     * and real output operations should be executed normally (even if they may overflow and be lost, etc.).
     * </p>
     */
    public void setIgnoreOutputCheck(final boolean ignoreOutputCheck) {
        this.ignoreOutputCheck = ignoreOutputCheck;
    }

    public interface ChancedRequirement {

        void setChance(float chance);

    }

    /**
     * <p>Parallelizable（可并行）</p>
     * <p>这个接口由 {@link ComponentRequirement} 及其子类实现，用于实现内置的并行功能。</p>
     */
    public interface Parallelizable extends MultiComponent {
        /**
         * <p>获取当前需求的最大可并行数量。</p>
         * <p>Get the maximum number of parallels available for the current requirement</p>
         *
         * @param components     被复制后的组件，通常情况下可以自由修改其内容。<br/>
         *                       Components that have been copied are, in general, free to modify their contents.
         * @param maxParallelism 最大并行数，返回值不应超出该数值。<br/>
         *                       The maximum number of parallelism, the return value should not exceed this value.
         * @return max parallelism
         */
        int getMaxParallelism(final List<ProcessingComponent<?>> components,
                              final RecipeCraftingContext context,
                              final int maxParallelism);

        int getParallelism();

        /**
         * <p>
         * 设置该需求的并行数，检查需求或工作时应当严格遵守该并行数量，不得自行增加或减少并行数。
         * </p>
         * <p>
         * Set the number of parallels for that requirement,
         * and that number of parallels should be strictly adhered to when checking the requirement or the work,
         * and you should not increase or decrease the number of parallels on your own.
         * </p>
         */
        void setParallelism(int parallelism);

        boolean isParallelizeUnaffected();

        /**
         * <p>设置该并行类型是否不受并行数影响（即并行数始终为 1）。</p>
         * <p>Sets whether this parallel type is independent of the number of parallels (i.e., the number of parallels is always 1).</p>
         */
        void setParallelizeUnaffected(boolean unaffected);
    }

    /**
     * <p>
     * 实现此接口的 ComponentRequirement 应当重写以下方法：<br/>
     * A ComponentRequirement that implements this interface should override the following methods:
     * </p>
     * <p>
     * {@link #startCrafting(List, RecipeCraftingContext, ResultChance)}<br/>
     * {@link #finishCrafting(List, RecipeCraftingContext, ResultChance)}<br/>
     * {@link #canStartCrafting(List, RecipeCraftingContext)}<br/>
     * {@link #copyComponents(List)}<br/>
     * </p>
     * <p>
     * 注意：必须严格遵守这些方法的实现需求，否则可能会出现预料之外的问题。<br/>
     * Note: The implementation requirements for these methods must be strictly adhered to,
     * otherwise unanticipated problems may occur.<br/>
     * </p>
     */
    public interface MultiComponent {
        /**
         * <p>
         * 为所有匹配的组件都提供一份复制副本，通常用于并行检查和配方检查。<br/>
         * 通常情况下可以不实现，但是如果实现了接口 {@link MultiComponent} 则必须实现此方法。<br/>
         * 注意：如果需要实现此方法，<strong>非必要情况下请不要返回组件自身</strong>，否则可能会出现包括但不限于物品复制等问题。<br/>
         * </p>
         * <p>
         * Provides a duplicate copy for all matching components, typically used for parallel checking and recipe checking.<br/>
         * This method can normally be left out, but must be implemented if the interface {@link MultiComponent} is implemented.<br/>
         * WARN: If this method needs to be implemented, <strong>do not return to the component itself unless absolutely necessary</strong>,
         * otherwise issues including, but not limited to, item duplication may occur.<br/>
         * </p>
         */
        @Nonnull
        List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components);

        /**
         * <p>
         * 此方法为 {@link #canStartCrafting(ProcessingComponent, RecipeCraftingContext, List)} 的增强实现。<br/>
         * 该方法直接一次性提供所有可用的组件以供需求完成操作。<br/>
         * 通常情况下可以不实现，但是如果实现了接口 {@link MultiComponent} 则必须实现此方法。<br/>
         * 注意：当所有的 ComponentRequirement 都实现了 Parallelizable 接口，则检查配方时会忽略 canStartCrafting 的调用过程（也就是说完全不会调用此方法）。<br/>
         * </p>
         * <p>
         * This method is an enhanced implementation of {@link #canStartCrafting(ProcessingComponent, RecipeCraftingContext, List)}}.<br/>
         * This will directly provides all available components at once for the required completion of the operation.<br/>
         * This method can normally be left out, but must be implemented if the interface {@link MultiComponent} is implemented.<br/>
         * Note: When all ComponentRequirements implement the Parallelizable interface, the recipe check ignores the canStartCrafting call (i.e., the method is not called at all).<br/>
         * </p>
         *
         * @param components 被复制后的组件，通常情况下可以自由修改其内容。<br/>
         *                   Components that have been copied are, in general, free to modify their contents.
         */
        @Nonnull
        CraftCheck canStartCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context);

        /**
         * <p>
         * 此方法为 {@link #startCrafting(ProcessingComponent, RecipeCraftingContext, ResultChance)} 的增强实现。<br/>
         * 该方法直接一次性提供所有可用的组件以供需求完成操作。<br/>
         * 通常情况下可以不实现，但是如果实现了接口 {@link MultiComponent} 则必须实现此方法。<br/>
         * </p>
         * <p>
         * This method is an enhanced implementation of {@link #startCrafting(ProcessingComponent, RecipeCraftingContext, ResultChance)}.<br/>
         * This will directly provides all available components at once for the required completion of the operation.<br/>
         * This method can normally be left out, but must be implemented if the interface {@link MultiComponent} is implemented.<br/>
         * </p>
         *
         * @param components 组件列表（非复制副本）<br/>
         *                   List of components (not duplicated copies)
         */
        default void startCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        }

        /**
         * <p>
         * 此方法为 {@link #finishCrafting(ProcessingComponent, RecipeCraftingContext, ResultChance)} 的增强实现。<br/>
         * 该方法直接一次性提供所有可用的组件以供需求完成操作。<br/>
         * 通常情况下可以不实现，但是如果实现了接口 {@link MultiComponent} 则必须实现此方法。<br/>
         * </p>
         * <p>
         * This method is an enhanced implementation of {@link #finishCrafting(ProcessingComponent, RecipeCraftingContext, ResultChance)}.<br/>
         * This will directly provides all available components at once for the required completion of the operation.<br/>
         * This method can normally be left out, but must be implemented if the interface {@link MultiComponent} is implemented.<br/>
         * </p>
         *
         * @param components 组件列表（非复制副本）<br/>
         *                   List of components (not duplicated copies)
         */
        default void finishCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        }
    }

    public abstract static class JEIComponent<T> {

        public abstract Class<T> getJEIRequirementClass();

        public abstract List<T> getJEIIORequirements();

        @SideOnly(Side.CLIENT)
        public abstract RecipeLayoutPart<T> getLayoutPart(Point offset);

        @SideOnly(Side.CLIENT)
        public RecipeLayoutPart<T> getTemplateLayout() {
            return this.getLayoutPart(new Point(0, 0));
        }

        @SideOnly(Side.CLIENT)
        public abstract void onJEIHoverTooltip(int slotIndex, boolean input, T ingredient, List<String> tooltip);
    }

    public abstract static class MultiComponentRequirement<T, V extends RequirementType<T, ? extends ComponentRequirement<T, V>>>
        extends ComponentRequirement<T, V>
        implements MultiComponent {

        public MultiComponentRequirement(final V requirementType, final IOType actionType) {
            super(requirementType, actionType);
        }

        @Nonnull
        @Override
        public final CraftCheck canStartCrafting(final ProcessingComponent<?> component, final RecipeCraftingContext context, final List<ComponentOutputRestrictor> restrictions) {
            return CraftCheck.success();
        }
    }

    public abstract static class MultiCompParallelizable<T, V extends RequirementType<T, ? extends ComponentRequirement<T, V>>>
        extends MultiComponentRequirement<T, V>
        implements Parallelizable {

        protected int     parallelism           = 1;
        protected boolean parallelizeUnaffected = false;

        public MultiCompParallelizable(final V requirementType, final IOType actionType) {
            super(requirementType, actionType);
        }

        @Override
        public ComponentRequirement<T, V> postDeepCopy(ComponentRequirement<?, ?> another) {
            super.postDeepCopy(another);
            if (another instanceof ComponentRequirement.MultiCompParallelizable<?, ?> parallelizable) {
                this.parallelizeUnaffected = parallelizable.parallelizeUnaffected;
            }
            return this;
        }

        @Override
        public boolean isParallelizeUnaffected() {
            return parallelizeUnaffected;
        }

        @Override
        public void setParallelizeUnaffected(boolean unaffected) {
            this.parallelizeUnaffected = unaffected;
            if (parallelizeUnaffected) {
                this.parallelism = 1;
            }
        }

        @Override
        public int getParallelism() {
            return parallelism;
        }

        @Override
        public void setParallelism(int parallelism) {
            if (!parallelizeUnaffected) {
                this.parallelism = parallelism;
            }
        }
    }

    public abstract static class PerTick<T, V extends RequirementType<T, ? extends PerTick<T, V>>>
        extends ComponentRequirement<T, V> {

        public PerTick(V requirementType, IOType actionType) {
            super(requirementType, actionType);
        }

        @Nonnull
        @Override
        public CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
            return CraftCheck.success();
        }

        //Multiplier is passed into this to adjust 'production' or 'consumption' accordingly if the recipe has a longer or shorter duration
        public void startIOTick(RecipeCraftingContext context, float durationMultiplier) {

        }

        // Returns the actual result of the IOTick-check after a sufficient amount of components has been checked for the requirement
        // Supply a failure message if invalid!
        @Nonnull
        public CraftCheck resetIOTick(RecipeCraftingContext context) {
            return CraftCheck.success();
        }

        // Returns either success, partial success or skip component
        // Return value indicates whether the IO tick requirement was already successful
        // or if more components need to be checked.
        @Nonnull
        public abstract CraftCheck doIOTick(ProcessingComponent<?> component, RecipeCraftingContext context);
    }

    public abstract static class PerTickMultiComponent<T, V extends RequirementType<T, ? extends PerTick<T, V>>>
        extends PerTick<T, V>
        implements MultiComponent {

        public PerTickMultiComponent(final V requirementType, final IOType actionType) {
            super(requirementType, actionType);
        }

        @Nonnull
        @Override
        public final CraftCheck doIOTick(final ProcessingComponent<?> component, final RecipeCraftingContext context) {
            return CraftCheck.success();
        }

        /**
         * <p>
         * 此方法为 {@link #doIOTick(ProcessingComponent, RecipeCraftingContext)} 的增强实现。<br/>
         * 该方法直接一次性提供所有可用的组件以供需求完成操作。<br/>
         * 通常情况下可以不实现，但是如果实现了接口 {@link MultiComponent} 则必须实现此方法。<br/>
         * </p>
         * <p>
         * This method is an enhanced implementation of {@link #doIOTick(ProcessingComponent, RecipeCraftingContext)}.<br/>
         * This will directly provides all available components at once for the required completion of the operation.<br/>
         * This method can normally be left out, but must be implemented if the interface {@link MultiComponent} is implemented.<br/>
         * </p>
         *
         * @param components 组件列表（非复制副本）<br/>
         *                   List of components (not duplicated copies)
         */
        public abstract CraftCheck doIOTick(List<ProcessingComponent<?>> components, RecipeCraftingContext context, float durationMultiplier);
    }

    public abstract static class PerTickParallelizable<T, V extends RequirementType<T, ? extends PerTick<T, V>>>
        extends PerTickMultiComponent<T, V>
        implements Parallelizable {

        protected int     parallelism           = 1;
        protected boolean parallelizeUnaffected = false;

        public PerTickParallelizable(final V requirementType, final IOType actionType) {
            super(requirementType, actionType);
        }

        @Override
        public ComponentRequirement<T, V> postDeepCopy(ComponentRequirement<?, ?> another) {
            super.postDeepCopy(another);
            if (another instanceof PerTickParallelizable<?, ?> parallelizable) {
                this.parallelizeUnaffected = parallelizable.parallelizeUnaffected;
            }
            return this;
        }

        @Override
        public boolean isParallelizeUnaffected() {
            return parallelizeUnaffected;
        }

        @Override
        public void setParallelizeUnaffected(boolean unaffected) {
            this.parallelizeUnaffected = unaffected;
            if (parallelizeUnaffected) {
                this.parallelism = 1;
            }
        }

        @Override
        public int getParallelism() {
            return parallelism;
        }

        @Override
        public void setParallelism(int parallelism) {
            if (!parallelizeUnaffected) {
                this.parallelism = parallelism;
            }
        }
    }
}
