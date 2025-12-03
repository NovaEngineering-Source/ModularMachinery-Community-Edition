/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import github.kasuminova.mmce.common.concurrent.Sync;
import github.kasuminova.mmce.common.event.Phase;
import github.kasuminova.mmce.common.event.recipe.ResultChanceCreateEvent;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.command.ControllerCommandSender;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.modifier.SingleBlockModifierReplacement;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeCraftingContext
 * Created by HellFirePvP
 * Date: 28.06.2017 / 12:23
 */
public class RecipeCraftingContext {
    private static final Random RAND = new Random();

    private final int reloadCounter;

    private final Map<RequirementType<?, ?>, List<RecipeModifier>> modifiers = new ConcurrentHashMap<>();
    private final Map<RequirementType<?, ?>, RecipeModifier.ModifierApplier> modifierAppliers = new ConcurrentHashMap<>();
    private final Map<RequirementType<?, ?>, RecipeModifier.ModifierApplier> chanceModifierAppliers = new ConcurrentHashMap<>();

    private final List<RecipeModifier> permanentModifierList = new ArrayList<>();

    private final List<ComponentOutputRestrictor> currentRestrictions = new ArrayList<>();

    private final List<ComponentRequirement<?, ?>> requirements = new ArrayList<>();
    private final Map<Long, List<RequirementComponents>> requirementComponents = new ConcurrentHashMap<>();
    private boolean isCrafting = false;
    private long groupId = 0;
    private final long[][] groupKeys = new long[2][];

    private ActiveMachineRecipe activeRecipe;
    private TileMultiblockMachineController controller = null;
    private ControllerCommandSender commandSender = null;

    private final Map<Long, Collection<ProcessingComponent<?>>> typeComponents = new ConcurrentHashMap<>();

    private int currentIOTickIndex = 0;

    public RecipeCraftingContext(final int reloadCounter,
                                 final ActiveMachineRecipe activeRecipe,
                                 final TileMultiblockMachineController controller) {
        this.reloadCounter = reloadCounter;
        this.activeRecipe = activeRecipe;
        for (ComponentRequirement<?, ?> requirement : getParentRecipe().getCraftingRequirements()) {
            this.requirements.add(this.requirements.size(), requirement.deepCopy().postDeepCopy(requirement));
        }

        init(activeRecipe, controller);
    }

    public void setCrafting(boolean crafting) {
        isCrafting = crafting;
    }

    private static List<ProcessingComponent<?>> getCopiedRequirementComponents(
        final ReqCompMap reqCompMap,
        final TaggedReqCompMap taggedReqCompMap,
        final ComponentRequirement<?, ?> req, final List<ProcessingComponent<?>> compList) {
        List<ProcessingComponent<?>> copiedCompList;
        if (req.tag != null) {
            copiedCompList = taggedReqCompMap.computeIfAbsent(
                req.actionType, reqTypeMap -> new Object2ObjectArrayMap<>()).computeIfAbsent(
                req.requirementType, tagMap -> new Object2ObjectOpenHashMap<>()).computeIfAbsent(
                req.tag, comp -> ((ComponentRequirement.MultiComponent) req).copyComponents(compList));
        } else {
            copiedCompList = reqCompMap.computeIfAbsent(
                req.actionType, reqTypeMap -> new Object2ObjectArrayMap<>()).computeIfAbsent(
                req.requirementType, comp -> ((ComponentRequirement.MultiComponent) req).copyComponents(compList));
        }
        return copiedCompList;
    }

    public RecipeCraftingContext reset() {
        this.modifiers.clear();
        this.modifierAppliers.clear();
        this.chanceModifierAppliers.clear();
        this.permanentModifierList.clear();
        this.currentRestrictions.clear();
        this.isCrafting = false;

        this.currentIOTickIndex = 0;
        return this;
    }

    public RecipeCraftingContext resetAll() {
        setParallelism(1);
        this.activeRecipe = null;
        this.controller = null;
        this.commandSender = null;
        this.typeComponents.clear();
        this.requirementComponents.clear();

        return reset();
    }

    public void destroy() {
        resetAll();
        this.requirements.clear();
    }

    public RecipeCraftingContext init(final ActiveMachineRecipe activeRecipe,
                                      final TileMultiblockMachineController ctrl) {
        this.controller = ctrl;
        if (this.activeRecipe == null || this.activeRecipe.getRecipe() != activeRecipe.getRecipe()) {
            this.activeRecipe = activeRecipe;
            this.requirements.clear();
            for (ComponentRequirement<?, ?> requirement : getParentRecipe().getCraftingRequirements()) {
                this.requirements.add(this.requirements.size(), requirement.deepCopy().postDeepCopy(requirement));
            }
        } else {
            this.activeRecipe = activeRecipe;
        }
        this.commandSender = new ControllerCommandSender(this.controller);

        reset();
        updateComponents(ctrl.getFoundComponents());
        return this;
    }

    public int getReloadCounter() {
        return reloadCounter;
    }

    public TileMultiblockMachineController getMachineController() {
        return controller;
    }

    public MachineRecipe getParentRecipe() {
        return activeRecipe.getRecipe();
    }

    public ActiveMachineRecipe getActiveRecipe() {
        return activeRecipe;
    }

    @Nonnull
    public List<RecipeModifier> getModifiers(RequirementType<?, ?> target) {
        return modifiers.computeIfAbsent(target, t -> new CopyOnWriteArrayList<>());
    }

    @Nonnull
    public RecipeModifier.ModifierApplier getModifierApplier(RequirementType<?, ?> target, boolean isChance) {
        return isChance
            ? chanceModifierAppliers.getOrDefault(target, RecipeModifier.ModifierApplier.DEFAULT_APPLIER)
            : modifierAppliers.getOrDefault(target, RecipeModifier.ModifierApplier.DEFAULT_APPLIER);
    }

    public float getDurationMultiplier() {
        if (!Config.enableDurationMultiplier) {
            return 1f;
        }
        float dur = this.getParentRecipe().getRecipeTotalTickTime();
        float result = RecipeModifier.applyModifiers(this, RequirementTypesMM.REQUIREMENT_DURATION, null, dur, false);
        return dur / result;
    }

    public void addRestriction(ComponentOutputRestrictor restrictor) {
        this.currentRestrictions.add(restrictor);
    }

    private static final Map<Long, List<ProcessingComponent<?>>> emptyComponents
        = Collections.singletonMap(0L, Collections.emptyList());

    private Map<Long, List<ProcessingComponent<?>>> getComponentsFor(ComponentRequirement<?, ?> requirement, @Nullable ComponentSelectorTag tag) {
        Map<Long, List<ProcessingComponent<?>>> validComponents = new HashMap<>();
        for (Map.Entry<Long, Collection<ProcessingComponent<?>>> set : this.typeComponents.entrySet()) {
            final Long groupId = set.getKey();
            for (var typeComponent : set.getValue()) {
                if (!requirement.isValidComponent(typeComponent, this)) {
                    continue;
                }

                if (tag != null) {
                    if (tag.equals(typeComponent.getTag())) {
                        validComponents
                            .computeIfAbsent(groupId, i -> new ObjectArrayList<>())
                            .add(typeComponent);
                    }
                } else {
                    validComponents
                        .computeIfAbsent(groupId, i -> new ObjectArrayList<>())
                        .add(typeComponent);
                }
            }
        }

        return validComponents.isEmpty() ? emptyComponents : validComponents;
    }

    public CraftingCheckResult ioTick(int currentTick) {
        ResultChance chance = new ResultChance(RAND.nextLong());
        CraftingCheckResult checkResult = new CraftingCheckResult();
        float durMultiplier = this.getDurationMultiplier();

        final List<RequirementComponents> components = getCurrentComponents();

        //Input / Output tick
        for (int i = currentIOTickIndex; i < components.size(); i++) {
            final RequirementComponents reqComponent = components.get(i);

            ComponentRequirement<?, ?> requirement = reqComponent.requirement();
            if (!(requirement instanceof ComponentRequirement.PerTick<?, ?> perTickReq)) {
                if (requirement.getTriggerTime() <= 0) {
                    checkAndTriggerRequirement(checkResult, currentTick, chance, reqComponent);
                    if (checkResult.isFailure()) {
                        currentIOTickIndex = i;
                        return checkResult;
                    }
                }
                continue;
            }

            if (perTickReq instanceof ComponentRequirement.PerTickMultiComponent<?, ?> reqMultiComp) {
                CraftCheck result = reqMultiComp.doIOTick(reqComponent.components(), this, durMultiplier);
                if (!result.isSuccess()) {
                    currentIOTickIndex = i;
                    checkResult.addError(result.getUnlocalizedMessage());
                    return checkResult;
                }
                continue;
            }

            perTickReq.resetIOTick(this);
            perTickReq.startIOTick(this, durMultiplier);

            for (ProcessingComponent<?> component : reqComponent.components()) {
                AtomicReference<CraftCheck> result = new AtomicReference<>();
                if (perTickReq instanceof Asyncable) {
                    result.set(perTickReq.doIOTick(component, this));
                } else {
                    Sync.doSyncAction(() -> result.set(perTickReq.doIOTick(component, this)));
                }
                if (result.get().isSuccess()) {
                    break;
                }
            }

            CraftCheck result = perTickReq.resetIOTick(this);
            if (!result.isSuccess()) {
                currentIOTickIndex = i;
                checkResult.addError(result.getUnlocalizedMessage());
                return checkResult;
            }
        }
        currentIOTickIndex = 0;

        this.getParentRecipe().getCommandContainer().runTickCommands(this.commandSender, currentTick);

        return CraftingCheckResult.SUCCESS;
    }

    public List<ComponentRequirement<?, ?>> getRequirementBy(RequirementType<?, ?> type) {
        return requirements.stream()
                           .filter(req -> req.getRequirementType().equals(type))
                           .collect(Collectors.toList());
    }

    public List<ComponentRequirement<?, ?>> getRequirementBy(RequirementType<?, ?> type, IOType ioType) {
        return requirements.stream()
                           .filter(req -> req.getRequirementType().equals(type) && req.getActionType() == ioType)
                           .collect(Collectors.toList());
    }

    private void checkAndTriggerRequirement(final CraftingCheckResult res,
                                            final int currentTick,
                                            final ResultChance chance,
                                            final RequirementComponents reqComponent) {
        ComponentRequirement<?, ?> req = reqComponent.requirement();
        int triggerTime = req.getTriggerTime() * Math.round(RecipeModifier.applyModifiers(
            this, RequirementTypesMM.REQUIREMENT_DURATION, null, 1, false));
        if (triggerTime <= 0 || triggerTime != currentTick || (req.isTriggered() && !req.isTriggerRepeatable())) {
            return;
        }

        if (canStartCrafting(res, reqComponent, new ReqCompMap(), new TaggedReqCompMap())) {
            startCrafting(chance, reqComponent);
            req.setTriggered(true);
        }
    }

    public List<RequirementComponents> getCurrentComponents() {
        List<RequirementComponents> components;
        var g = requirementComponents.get(groupId);
        if (g == null) {
            for (Long l : this.requirementComponents.keySet()) {
                if (l >= 0) {
                    setGroupId(l);
                    break;
                }
            }
            components = requirementComponents.get(groupId);
        } else {
            components = g;
        }
        return components;
    }

    public void startCrafting() {
        startCrafting(RAND.nextLong());
    }

    public void startCrafting(long seed) {
        ResultChance chance = new ResultChance(seed);

        for (RequirementComponents reqComponents : getCurrentComponents()) {
            if (reqComponents.requirement().getTriggerTime() <= 0) {
                startCrafting(chance, reqComponents);
            }
        }

        this.getParentRecipe().getCommandContainer().runStartCommands(this.commandSender);
    }

    private void startCrafting(final ResultChance chance, final RequirementComponents reqComponents) {
        ComponentRequirement<?, ?> requirement = reqComponents.requirement();

        if (requirement instanceof ComponentRequirement.MultiComponent req) {
            req.startCrafting(reqComponents.components(), this, chance);
            return;
        }

        requirement.startRequirementCheck(chance, this);
        for (ProcessingComponent<?> component : reqComponents.components()) {
            AtomicBoolean success = new AtomicBoolean(false);
            if (requirement instanceof Asyncable) {
                success.set(requirement.startCrafting(component, this, chance));
            } else {
                Sync.doSyncAction(() -> success.set(requirement.startCrafting(component, this, chance)));
            }
            if (success.get()) {
                requirement.endRequirementCheck();
                return;
            }
        }
        requirement.endRequirementCheck();
    }

    public void finishCrafting() {
        finishCrafting(RAND.nextLong());
    }

    public void finishCrafting(long seed) {
        ResultChanceCreateEvent event = new ResultChanceCreateEvent(
            controller, this, new ResultChance(seed), Phase.END);
        event.postEvent();
        ResultChance chance = event.getResultChance();

        for (RequirementComponents reqComponents : getCurrentComponents()) {
            ComponentRequirement<?, ?> requirement = reqComponents.requirement();
            List<ProcessingComponent<?>> components = reqComponents.components();

            if (requirement instanceof ComponentRequirement.MultiComponent reqMulti) {
                reqMulti.finishCrafting(components, this, chance);
                continue;
            }

            requirement.startRequirementCheck(chance, this);
            for (ProcessingComponent<?> component : components) {
                AtomicReference<CraftCheck> check = new AtomicReference<>();
                if (requirement instanceof Asyncable) {
                    check.set(requirement.finishCrafting(component, this, chance));
                } else {
                    Sync.doSyncAction(() -> check.set(requirement.finishCrafting(component, this, chance)));
                }
                if (check.get().isSuccess()) {
                    break;
                }
            }
            requirement.endRequirementCheck();
        }

        this.getParentRecipe().getCommandContainer().runFinishCommands(this.commandSender);
    }

    public Collection<RequirementComponents> getAllParallelizableComponents() {
        Collection<RequirementComponents> list = new ObjectArrayList<>();
        for (RequirementComponents reqComponent : getCurrentComponents()) {
            if (reqComponent.requirement() instanceof ComponentRequirement.Parallelizable parallelizable
                && !parallelizable.isParallelizeUnaffected()) {
                list.add(reqComponent);
            }
        }
        return list;
    }

    public int getMaxParallelism(Collection<RequirementComponents> parallelizable) {
        int maxParallelism = this.activeRecipe.getMaxParallelism();

        ReqCompMap typeCopiedComp = new ReqCompMap();
        TaggedReqCompMap taggedTypeCopiedComp = new TaggedReqCompMap();

        int reqMaxParallelism = maxParallelism;
        for (RequirementComponents reqComponent : parallelizable) {
            ComponentRequirement<?, ?> req = reqComponent.requirement();
            List<ProcessingComponent<?>> compList = reqComponent.components();
            List<ProcessingComponent<?>> copiedCompList = getCopiedRequirementComponents(typeCopiedComp, taggedTypeCopiedComp, req, compList);

            ComponentRequirement.Parallelizable requirement = (ComponentRequirement.Parallelizable) req;
            reqMaxParallelism = Math.min(reqMaxParallelism, requirement.getMaxParallelism(copiedCompList, this, reqMaxParallelism));

            if (reqMaxParallelism <= 0) {
                return 0;
            }
        }

        return reqMaxParallelism;
    }

    public void setParallelism(int parallelism) {
        for (RequirementComponents obj : getCurrentComponents()) {
            if (obj.requirement() instanceof ComponentRequirement.Parallelizable p) {
                p.setParallelism(parallelism);
            }
        }
        activeRecipe.setParallelism(parallelism);
    }

    public CraftingCheckResult canStartCrafting() {
        permanentModifierList.clear();
        if (getParentRecipe().isParallelized() && activeRecipe.getMaxParallelism() > 1) {
            Collection<RequirementComponents> parallelizable = getAllParallelizableComponents();
            int maxParallelism = getMaxParallelism(parallelizable);
            setParallelism(Math.max(1, maxParallelism));

            if (maxParallelism > 0 && parallelizable.size() >= getCurrentComponents().size()) {
                return CraftingCheckResult.SUCCESS;
            }
        }
        return canStartCrafting(true);
    }

    public CraftingCheckResult canRestartCrafting() {
        permanentModifierList.clear();
        int currentParallelism = activeRecipe.getParallelism();
        int maxParallelism = activeRecipe.getMaxParallelism();

        if (currentParallelism > maxParallelism) {
            setParallelism(maxParallelism);
            CraftingCheckResult result = canStartCrafting(true);
            if (!result.isSuccess()) {
                setParallelism(1);
            } else {
                return CraftingCheckResult.SUCCESS;
            }
        }

        return canStartCrafting();
    }

    public CraftingCheckResult canFinishCrafting() {
        return this.canStartCrafting(false);
    }

    private CraftingCheckResult canStartCrafting(boolean input) {
        currentRestrictions.clear();

        CraftingCheckResult result = CraftingCheckResult.FAILURE;
        float successfulRequirements = 0;
        boolean success = false;
        long fkey = 0;

        for (var key : groupKeys[!isCrafting ? 0 : 1]) {
            if (key < 0) continue;
            List<RequirementComponents> components = this.requirementComponents.get(key);
            List<RequirementComponents> requirements = input ? components
                : components.stream()
                          .filter(r -> r.requirement().actionType == IOType.OUTPUT)
                          .collect(Collectors.toCollection(ObjectArrayList::new));

            result = new CraftingCheckResult();
            ReqCompMap typeCopiedComp = new ReqCompMap();
            TaggedReqCompMap taggedTypeCopiedComp = new TaggedReqCompMap();
            for (RequirementComponents reqEntry : requirements) {
                if (canStartCrafting(result, reqEntry, typeCopiedComp, taggedTypeCopiedComp)) {
                    ++successfulRequirements;
                }
            }
            final float validity = successfulRequirements / requirements.size();
            result.setValidity(validity);

            if (!input) {
                success = true;
                break;
            }

            if (result.isSuccess()) {
                success = true;
                setGroupId(key);
                this.isCrafting = true;
                break;
            } else {
                fkey = key;
                successfulRequirements = 0;
            }
        }

        if (!success) setGroupId(fkey);

        currentRestrictions.clear();
        return result;
    }

    public final RecipeCraftingContext setGroupId(long i) {
        if (!isCrafting) {
            groupId = i;
            if (groupKeys[1] == null)
                groupKeys[1] = new long[]{groupId};
            else if (groupKeys[1][0] != groupId) groupKeys[1][0] = groupId;
        }
        return this;
    }

    public long getGroupId() {
        return groupId;
    }

    private boolean canStartCrafting(final CraftingCheckResult result,
                                     final RequirementComponents reqComponent,
                                     final ReqCompMap reqCompMap,
                                     TaggedReqCompMap taggedReqCompMap) {
        ComponentRequirement<?, ?> req = reqComponent.requirement();
        req.startRequirementCheck(ResultChance.GUARANTEED, this);

        List<ProcessingComponent<?>> compList = reqComponent.components();
        if (!compList.isEmpty()) {
            if (req instanceof ComponentRequirement.MultiComponent reqMulti) {
                List<ProcessingComponent<?>> copiedCompList = getCopiedRequirementComponents(reqCompMap, taggedReqCompMap, req, compList);
                CraftCheck check = reqMulti.canStartCrafting(copiedCompList, this);
                if (check.isSuccess()) {
                    return true;
                }
                result.addError(check.getUnlocalizedMessage());
                return false;
            }

            List<String> errorMessages = new ArrayList<>();
            for (ProcessingComponent<?> component : compList) {
                CraftCheck check = req.canStartCrafting(component, this, this.currentRestrictions);

                if (check.isSuccess()) {
                    req.endRequirementCheck();
                    return true;
                }

                if (!check.isInvalid() && !check.getUnlocalizedMessage().isEmpty()) {
                    errorMessages.add(check.getUnlocalizedMessage());
                }
            }
            errorMessages.forEach(result::addError);
        } else {
            // No component found that would apply for the given req
            result.addError(req.getMissingComponentErrorMessage(req.actionType));
        }

        req.endRequirementCheck();
        return false;
    }

    public void updateComponents(Map<Long, Map<TileEntity, ProcessingComponent<?>>> components) {
        this.typeComponents.clear();
        components.forEach((groupId, map)
            -> this.typeComponents.put(groupId, map.values()));
        updateRequirementComponents();
    }

    public void updateRequirementComponents() {
        requirementComponents.clear();
        requirements.forEach(req ->
            getComponentsFor(req, req.tag)
                .forEach((groupId, list)
                    -> requirementComponents
                    .computeIfAbsent(groupId, i -> new ObjectArrayList<>())
                    .add(new RequirementComponents(req, list))));

        if (this.requirementComponents.isEmpty()) {
            this.requirementComponents.put(0L, ObjectLists.emptyList());
            setGroupId(0);
        } else {
            for (Long l : this.requirementComponents.keySet()) {
                if (l >= 0) {
                    setGroupId(l);
                    break;
                }
            }
        }

        groupKeys[0] = this.requirementComponents.keySet().stream().mapToLong(Long::longValue).toArray();
        Arrays.sort(groupKeys[0]);
        if (groupKeys[1] == null) groupKeys[1] = new long[]{groupId};
        else if (groupKeys[1][0] != groupId) groupKeys[1][0] = groupId;
    }

    public void addModifier(SingleBlockModifierReplacement replacement) {
        addModifier(replacement.getModifiers());
    }

    public void addModifier(RecipeModifier modifier) {
        if (modifier != null) {
            RequirementType<?, ?> target = modifier.getTarget();
            if (target == null) {
                target = RequirementTypesMM.REQUIREMENT_DURATION;
            }
            this.modifiers.computeIfAbsent(target, t -> new CopyOnWriteArrayList<>()).add(modifier);
            updateModifierApplier(target);
        }
    }

    public void addModifier(Collection<RecipeModifier> modifiers) {
        Set<RequirementType<?, ?>> changed = new HashSet<>();

        for (RecipeModifier modifier : modifiers) {
            RequirementType<?, ?> target = modifier.getTarget();
            if (target == null) {
                target = RequirementTypesMM.REQUIREMENT_DURATION;
            }
            this.modifiers.computeIfAbsent(target, t -> new CopyOnWriteArrayList<>()).add(modifier);
            changed.add(target);
        }

        changed.forEach(this::updateModifierApplier);
    }

    public void addModifier(List<RecipeModifier> modifiers) {
        if (modifiers.isEmpty()) {
            return;
        }
        if (modifiers.size() == 1) {
            addModifier(modifiers.get(0));
            return;
        }

        addModifier((Collection<RecipeModifier>) modifiers);
    }

    public void addPermanentModifier(RecipeModifier modifier) {
        if (modifier != null) {
            this.permanentModifierList.add(modifier);
            addModifier(modifier);
        }
    }

    public void updateModifierApplier(RequirementType<?, ?> reqType) {
        addModifierApplier(reqType, modifiers.computeIfAbsent(reqType, v -> new CopyOnWriteArrayList<>()));
    }

    public void addModifierApplier(final RequirementType<?, ?> reqType, final List<RecipeModifier> recipeModifiers) {
        RecipeModifier.ModifierApplier applier = new RecipeModifier.ModifierApplier();
        RecipeModifier.ModifierApplier chancedApplier = new RecipeModifier.ModifierApplier();

        recipeModifiers.forEach(mod -> RecipeModifier.applyValueToApplier(mod.affectsChance() ? chancedApplier : applier, mod));

        if (!applier.isDefault()) {
            modifierAppliers.put(reqType, applier);
        }
        if (!chancedApplier.isDefault()) {
            chanceModifierAppliers.put(reqType, chancedApplier);
        }
    }

    public void overrideModifier(Collection<RecipeModifier> modifiers) {
        this.modifiers.clear();
        this.modifierAppliers.clear();
        this.chanceModifierAppliers.clear();
        addModifier(modifiers);
        addModifier(permanentModifierList);
    }

    public static class CraftingCheckResult {
        private static final CraftingCheckResult SUCCESS = new CraftingCheckResult() {
            @Override
            public void addError(final String ignored) {
                throw new IllegalStateException("Cannot add error on SUCCESS result!");
            }

            @Override
            public void overrideError(final String ignored) {
                throw new IllegalStateException("Cannot add error on SUCCESS result!");
            }

            @Override
            public boolean isFailure() {
                return false;
            }

            @Override
            public boolean isSuccess() {
                return true;
            }
        };

        private static final CraftingCheckResult FAILURE = new CraftingCheckResult() {

            @Override
            public boolean isFailure() {
                return true;
            }

            @Override
            public boolean isSuccess() {
                return false;
            }

        };

        private final Object2IntMap<String> unlocErrorMessagesMap = new Object2IntOpenHashMap<>();
        public float validity = 0F;

        public void addError(String unlocError) {
            if (!unlocError.isEmpty()) {
                int count = this.unlocErrorMessagesMap.getInt(unlocError);
                count++;
                this.unlocErrorMessagesMap.put(unlocError, count);
            }
        }

        public void overrideError(String unlocError) {
            this.unlocErrorMessagesMap.clear();
            addError(unlocError);
        }

        public float getValidity() {
            return validity;
        }

        private void setValidity(float validity) {
            this.validity = validity;
        }

        public List<String> getUnlocalizedErrorMessages() {
            List<Map.Entry<String, Integer>> toSort = new ArrayList<>(this.unlocErrorMessagesMap.entrySet());
            toSort.sort(Map.Entry.comparingByValue());
            List<String> list = new ArrayList<>();
            for (Map.Entry<String, Integer> stringIntegerEntry : toSort) {
                String key = stringIntegerEntry.getKey();
                list.add(key);
            }
            return list;
        }

        public String getFirstErrorMessage(String defaultMessage) {
            List<String> unlocalizedErrorMessages = getUnlocalizedErrorMessages();
            return unlocalizedErrorMessages.isEmpty() ? defaultMessage : unlocalizedErrorMessages.get(0);
        }

        public boolean isFailure() {
            return !this.unlocErrorMessagesMap.isEmpty();
        }

        public boolean isSuccess() {
            return this.unlocErrorMessagesMap.isEmpty();
        }

    }

    public static class TaggedReqCompMap
        extends EnumMap<IOType, Object2ObjectArrayMap<RequirementType<?, ?>, Map<ComponentSelectorTag, List<ProcessingComponent<?>>>>> {
        public TaggedReqCompMap() {
            super(IOType.class);
        }

        @Override
        public final TaggedReqCompMap clone() throws AssertionError {
            throw new AssertionError();
        }
    }

    public static class ReqCompMap
        extends EnumMap<IOType, Object2ObjectArrayMap<RequirementType<?, ?>, List<ProcessingComponent<?>>>> {
        public ReqCompMap() {
            super(IOType.class);
        }

        @Override
        public final ReqCompMap clone() throws AssertionError {
            throw new AssertionError();
        }
    }
}
