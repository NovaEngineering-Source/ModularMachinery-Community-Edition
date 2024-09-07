package hellfirepvp.modularmachinery.common.crafting.requirement;

import github.kasuminova.mmce.common.util.IExtendedGasHandler;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.jei.JEIComponentGas;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeGas;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import hellfirepvp.modularmachinery.common.util.HybridFluidUtils;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import mekanism.api.gas.GasStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class RequirementGas extends ComponentRequirement.MultiCompParallelizable<Object, RequirementTypeGas>
        implements ComponentRequirement.ChancedRequirement, Asyncable {

    public final GasStack required;

    public float chance = 1F;

    public RequirementGas(IOType actionType, GasStack required) {
        super(RequirementTypesMM.REQUIREMENT_GAS, actionType);
        this.required = required.copy();
    }

    @Override
    public int getSortingWeight() {
        return PRIORITY_WEIGHT_FLUID;
    }

    @Override
    public RequirementGas deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementGas deepCopyModified(List<RecipeModifier> modifiers) {
        GasStack copied = this.required.copy();
        copied.amount = Math.round(RecipeModifier.applyModifiers(modifiers, this, copied.amount, false));
        RequirementGas fluid = new RequirementGas(this.actionType, copied);
        fluid.chance = RecipeModifier.applyModifiers(modifiers, this, this.chance, true);
        return fluid;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public JEIComponent provideJEIComponent() {
        return new JEIComponentGas(this);
    }

    @Override
    public void setChance(float chance) {
        this.chance = chance;
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        ResourceLocation compKey = this.requirementType.getRegistryName();
        return String.format("component.missing.%s.%s.%s",
                compKey.getNamespace(), compKey.getPath(), ioType.name().toLowerCase());
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.component();
        return (cmp.getContainerProvider() instanceof IExtendedGasHandler) && cmp.ioType == this.actionType;
    }

    @Override
    public void startCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        if (actionType == IOType.INPUT && chance.canWork(RecipeModifier.applyModifiers(context, this, this.chance, true))) {
            doGasIO(components, context);
        }
    }

    @Override
    public void finishCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (actionType == IOType.OUTPUT && chance.canWork(RecipeModifier.applyModifiers(context, this, this.chance, true))) {
            doGasIO(components, context);
        }
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context) {
        return doGasIO(components, context);
    }

    @Nonnull
    @Override
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        return HybridFluidUtils.copyGasHandlerComponents(components);
    }

    @Override
    public int getMaxParallelism(List<ProcessingComponent<?>> components, RecipeCraftingContext context, int maxParallelism) {
        if (ignoreOutputCheck && actionType == IOType.OUTPUT) {
            return maxParallelism;
        }
        if (parallelizeUnaffected) {
            if (doGasIOInternal(components, context, 1) >= 1) {
                return maxParallelism;
            }
            return 0;
        }
        return doGasIOInternal(components, context, maxParallelism);
    }

    private CraftCheck doGasIO(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        int mul = doGasIOInternal(components, context, parallelism);
        if (mul < parallelism) {
            return switch (actionType) {
                case INPUT -> CraftCheck.failure("craftcheck.failure.gas.input");
                case OUTPUT -> {
                    if (ignoreOutputCheck) {
                        yield CraftCheck.success();
                    }
                    yield CraftCheck.failure("craftcheck.failure.gas.output.space");
                }
            };
        }
        return CraftCheck.success();
    }

    private int doGasIOInternal(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxMultiplier) {
        List<IExtendedGasHandler> gasHandlers = HybridFluidUtils.castGasHandlerComponents(components);

        long required = Math.round(RecipeModifier.applyModifiers(context, this, (double) this.required.amount, false));
        long maxRequired = required * maxMultiplier;

        GasStack stack = this.required.copy();
        long totalIO = HybridFluidUtils.doSimulateDrainOrFill(stack, gasHandlers, maxRequired, actionType);

        if (totalIO < required) {
            return 0;
        }

        HybridFluidUtils.doDrainOrFill(stack, totalIO, gasHandlers, actionType);

        return (int) (totalIO / required);
    }

}