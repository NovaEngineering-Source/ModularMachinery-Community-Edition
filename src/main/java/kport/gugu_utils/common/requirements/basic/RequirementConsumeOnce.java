package kport.gugu_utils.common.requirements.basic;

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

public abstract class RequirementConsumeOnce<T, V extends IResourceToken> extends ComponentRequirementAdapter<T> {

    V checkToken;
    V outputToken;

    public RequirementConsumeOnce(RequirementTypeAdapter<T> componentType, IOType actionType) {
        super(componentType, actionType);
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
    public boolean startCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        ICraftingResourceHolder<V> handler = (ICraftingResourceHolder<V>) component.getComponent().getContainerProvider();
        handler.startCrafting(checkToken);
        if (handler.isFulfilled()) {
            return false;
        }
        switch (getActionType()) {
            case INPUT:
                handler.consume(outputToken, true);
                if (outputToken.isEmpty()) {
                    handler.setFulfilled(true);
                }
        }
        return false;

    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public CraftCheck finishCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        ICraftingResourceHolder<V> handler = (ICraftingResourceHolder<V>) component.getComponent().getContainerProvider();
        handler.finishCrafting(outputToken);
        if (handler.isFulfilled()) {
            return CraftCheck.partialSuccess();
        }
        switch (getActionType()) {
            case OUTPUT:
                handler.generate(outputToken, true);
                if (outputToken.isEmpty()) {
                    handler.setFulfilled(true);
                }
        }
        return CraftCheck.partialSuccess();
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

    protected String getMissingInput() {
        return "craftcheck.failure." + getRequirementType().getRegistryName() + ".input";
    }

    protected String getMissingOutput() {
        return "craftcheck.failure." + getRequirementType().getRegistryName() + ".output.space";
    }

    protected String getMiscProblem() {
        return "craftcheck.failure.misc";
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
        ICraftingResourceHolder<V> handler = (ICraftingResourceHolder<V>) component.getComponent().getContainerProvider();
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
                    return CraftCheck.failure(getMissingInput());
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
                    return CraftCheck.failure(getMissingOutput());
                }
        }
        return CraftCheck.failure(getMiscProblem());
    }

    @Override
    public void startRequirementCheck(ResultChance chance, RecipeCraftingContext context) {
        checkToken = emitConsumptionToken(context);
        checkToken.applyModifiers(context.getModifiers(getRequirementType()), getRequirementType(), getActionType());
        outputToken = emitConsumptionToken(context);
        outputToken.applyModifiers(context.getModifiers(getRequirementType()), getRequirementType(), getActionType());
    }

    @Override
    public void endRequirementCheck() {
        checkToken = emitConsumptionToken(null);
        outputToken = emitConsumptionToken(null);
    }

    protected abstract boolean isCorrectHatch(MachineComponent component);

    protected abstract V emitConsumptionToken(RecipeCraftingContext context);
}