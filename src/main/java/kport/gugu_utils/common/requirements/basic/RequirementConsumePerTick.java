package kport.gugu_utils.common.requirements.basic;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentOutputRestrictor;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import kport.gugu_utils.common.requirements.types.RequirementTypeAdapter;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class RequirementConsumePerTick<T, V extends IResourceToken> extends ComponentRequirementAdapter.PerTick<T> {
    protected V checkToken;

    protected V perTickToken;
    private int totalTick;

    public RequirementConsumePerTick(RequirementTypeAdapter<T> requirementType, IOType actionType) {
        super(requirementType, actionType);
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        if (!this.isCorrectHatch(component.getComponent())) {
            return false;
        }
        return RequirementUtils.isValidComponent(component, ctx, getActionType());

    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean startCrafting(ProcessingComponent<?> processingComponent, RecipeCraftingContext recipeCraftingContext, ResultChance resultChance) {
        boolean canStart = canStartCrafting(processingComponent, recipeCraftingContext, Lists.newArrayList()).isSuccess();
        if(canStart) {
            ICraftingResourceHolder<V> handler = (ICraftingResourceHolder<V>) processingComponent.getProvidedComponent();
            handler.startCrafting(checkToken);
        }

        return false;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public CraftCheck finishCrafting(ProcessingComponent<?> processingComponent, RecipeCraftingContext recipeCraftingContext, ResultChance resultChance) {
        if (processingComponent.getProvidedComponent() instanceof ICraftingResourceHolder) {
            ICraftingResourceHolder<V> handler = (ICraftingResourceHolder<V>) processingComponent.getProvidedComponent();
            handler.finishCrafting(checkToken);
        }
        return CraftCheck.partialSuccess();
    }

    protected String getMissingInput() {
        return "craftcheck.failure." + getRequirementType().getRegistryName() + ".input";
    }

    protected String getMissingOutput() {
        return "craftcheck.failure." + getRequirementType().getRegistryName() + ".output";
    }

    protected String getMiscProblem() {
        return "craftcheck.failure.misc";
    }

    public V getPerTickToken() {
        return perTickToken;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> list) {
        if (!isCorrectHatch(component.getComponent())) return CraftCheck.skipComponent();
        ICraftingResourceHolder<V> handler = (ICraftingResourceHolder<V>) component.getProvidedComponent();
        switch (getActionType()) {
            case INPUT:
                if (!handler.canConsume()) {
                    return CraftCheck.skipComponent();
                }
                boolean didConsume = handler.consume(checkToken, false);
                if (!didConsume) {
                    return CraftCheck.failure(handler.getInputProblem(checkToken));
                } else if (checkToken.isEmpty()) {
                    return CraftCheck.success();
                } else {
                    return CraftCheck.partialSuccess();
                }
            case OUTPUT:
                if (!handler.canGenerate()) {
                    return CraftCheck.skipComponent();
                }
                boolean didGenerate = handler.generate(checkToken, false);
                if (!didGenerate) {
                    return CraftCheck.failure(handler.getOutputProblem(checkToken));
                } else if (checkToken.isEmpty()) {
                    return CraftCheck.success();
                } else {
                    return CraftCheck.partialSuccess();
                }
        }
        return CraftCheck.failure(getMiscProblem());
    }

    @Override
    public void startRequirementCheck(ResultChance chance, RecipeCraftingContext context) {
        checkToken = emitConsumptionToken(context);
        checkToken.applyModifiers(context.getModifiers(getRequirementType()), getRequirementType(), getActionType());
    }

    @Override
    public void endRequirementCheck() {
        checkToken = emitConsumptionToken(null);
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        switch (ioType) {
            case INPUT:
                return "component.missing." + getRequirementType().getRegistryName() + ".input";
            case OUTPUT:
                return "component.missing." + getRequirementType().getRegistryName() + ".output";
            default:
                return "";
        }
    }

    @Override
    public CraftCheck resetIOTick(RecipeCraftingContext context) {
        boolean enough = perTickToken != null && perTickToken.isEmpty();
        String error = perTickToken == null ? null : perTickToken.getError();
        this.perTickToken = emitConsumptionToken(context);
        if(!Strings.isNullOrEmpty(error)){
            return CraftCheck.failure(error);
        }
        else if (enough)
            return CraftCheck.success();
        else if (getActionType() == IOType.INPUT)
            return CraftCheck.failure(getMissingInput());
        else
            return CraftCheck.failure(getMissingOutput());
    }

    @Override
    public void startIOTick(RecipeCraftingContext context, float durationMultiplier) {
        perTickToken.applyModifiers(context.getModifiers(getRequirementType()), getRequirementType(), getActionType(), durationMultiplier);
    }

    protected abstract V emitConsumptionToken(RecipeCraftingContext context);

    protected abstract boolean isCorrectHatch(MachineComponent component);

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public CraftCheck doIOTick(ProcessingComponent<?> component, RecipeCraftingContext context) {


        if (!isCorrectHatch(component.getComponent())) return CraftCheck.skipComponent();
        ICraftingResourceHolder<V> handler = (ICraftingResourceHolder<V>) component.getProvidedComponent();
        switch (getActionType()) {
            case INPUT:
                if (!handler.canConsume()) {
                    return CraftCheck.skipComponent();
                }
                boolean didConsume = handler.consume(perTickToken, true);
                if (!didConsume) {
                    return CraftCheck.failure(handler.getInputProblem(perTickToken));
                } else if (perTickToken.isEmpty()) {
                    return CraftCheck.success();
                } else {
                    return CraftCheck.partialSuccess();
                }

            case OUTPUT:
                if (!handler.canGenerate()) {
                    return CraftCheck.skipComponent();
                }
                boolean didGenerate = handler.generate(perTickToken, true);
                if (!didGenerate) {
                    return CraftCheck.failure(handler.getOutputProblem(perTickToken));
                } else if (perTickToken.isEmpty()) {
                    return CraftCheck.success();
                } else {
                    return CraftCheck.partialSuccess();
                }
        }
        //This is neither input nor output? when do we actually end up in this case down here?
        return CraftCheck.failure(getMiscProblem());
    }

    protected int getTotalTick() {
        return this.totalTick;
    }

    public void setTotalTick(int totalTick) {
        this.totalTick = totalTick;
    }
}
