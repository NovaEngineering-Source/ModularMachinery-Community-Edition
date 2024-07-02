/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirement;

import github.kasuminova.mmce.common.util.IExtendedGasHandler;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.jei.JEIComponentHybridFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeFluid;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluid;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluidGas;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import hellfirepvp.modularmachinery.common.util.HybridFluidUtils;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import mekanism.api.gas.GasStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementFluid
 * Created by HellFirePvP
 * Date: 24.02.2018 / 12:28
 * TODO: Split FluidStack and GasStack into two different Requirements, combining the two makes for terrible code.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RequirementFluid extends ComponentRequirement.MultiCompParallelizable<Object, RequirementTypeFluid>
        implements ComponentRequirement.ChancedRequirement, Asyncable {

    public final HybridFluid required;

    public float chance = 1F;

    private NBTTagCompound tagMatch = null, tagDisplay = null;

    public RequirementFluid(IOType ioType, FluidStack fluid) {
        this(RequirementTypesMM.REQUIREMENT_FLUID, ioType, new HybridFluid(fluid));
    }

    private RequirementFluid(RequirementTypeFluid type, IOType ioType, HybridFluid required) {
        super(type, ioType);
        this.required = required.copy();
    }

    @Optional.Method(modid = "mekanism")
    public static RequirementFluid createMekanismGasRequirement(RequirementTypeFluid type, IOType ioType, GasStack gasStack) {
        return new RequirementFluid(type, ioType, new HybridFluidGas(gasStack));
    }

    @Override
    public int getSortingWeight() {
        return PRIORITY_WEIGHT_FLUID;
    }

    @Override
    public RequirementFluid deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementFluid deepCopyModified(List<RecipeModifier> modifiers) {
        HybridFluid hybrid = this.required.copy();
        hybrid.setAmount((int) Math.round(RecipeModifier.applyModifiers(modifiers, this, (double) hybrid.getAmount(), false)));
        RequirementFluid fluid = new RequirementFluid(this.requirementType, this.actionType, hybrid);
        fluid.chance = RecipeModifier.applyModifiers(modifiers, this, this.chance, true);
        fluid.tagMatch = getTagMatch();
        fluid.tagDisplay = getTagDisplay();
        return fluid;
    }

    @Override
    public JEIComponent provideJEIComponent() {
        return new JEIComponentHybridFluid(this);
    }

    public void setMatchNBTTag(@Nullable NBTTagCompound tag) {
        this.tagMatch = tag;
    }

    @Nullable
    public NBTTagCompound getTagMatch() {
        if (tagMatch == null) {
            return null;
        }
        return tagMatch.copy();
    }

    public void setDisplayNBTTag(@Nullable NBTTagCompound tag) {
        this.tagDisplay = tag;
    }

    @Nullable
    public NBTTagCompound getTagDisplay() {
        if (tagDisplay == null) {
            return null;
        }
        return tagDisplay.copy();
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
        ComponentType cmpType = cmp.getComponentType();
        if (Mods.MEKANISM.isPresent() && required instanceof HybridFluidGas) {
            return (cmpType.equals(ComponentTypesMM.COMPONENT_GAS) || cmpType.equals(ComponentTypesMM.COMPONENT_ITEM_FLUID_GAS))
                    && cmp.ioType == this.actionType;
        } else {
            return (cmpType.equals(ComponentTypesMM.COMPONENT_FLUID) || cmpType.equals(ComponentTypesMM.COMPONENT_ITEM_FLUID_GAS))
                   && cmp.ioType == actionType;
        }
    }

    @Override
    public void startCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        if (actionType == IOType.INPUT && chance.canWork(RecipeModifier.applyModifiers(context, this, this.chance, true))) {
            doFluidGasIO(components, context);
        }
    }

    @Override
    public void finishCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (actionType == IOType.OUTPUT && chance.canWork(RecipeModifier.applyModifiers(context, this, this.chance, true))) {
            doFluidGasIO(components, context);
        }
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        return doFluidGasIO(components, context);
    }

    @Nonnull
    @Override
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        if (Mods.MEKANISM.isPresent() && this.required instanceof HybridFluidGas) {
            return HybridFluidUtils.copyGasHandlerComponents(components);
        }
        return HybridFluidUtils.copyFluidHandlerComponents(components);
    }

    @Override
    public int getMaxParallelism(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxParallelism) {
        if (ignoreOutputCheck && actionType == IOType.OUTPUT) {
            return maxParallelism;
        }
        if (parallelizeUnaffected) {
            if (doFluidGasIOInternal(components, context, 1) >= 1) {
                return maxParallelism;
            }
            return 0;
        }
        return doFluidGasIOInternal(components, context, maxParallelism);
    }

    private CraftCheck doFluidGasIO(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        int mul = doFluidGasIOInternal(components, context, parallelism);
        if (mul < parallelism) {
            return switch (actionType) {
                case INPUT -> CraftCheck.failure("craftcheck.failure.fluid.input");
                case OUTPUT -> {
                    if (ignoreOutputCheck) {
                        yield CraftCheck.success();
                    }
                    yield CraftCheck.failure("craftcheck.failure.fluid.output.space");
                }
            };
        }
        return CraftCheck.success();
    }

    private int doFluidGasIOInternal(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxMultiplier) {
        if (Mods.MEKANISM.isPresent() && this.required instanceof HybridFluidGas) {
            return doGasIOInternal(components, context, maxMultiplier);
        } else {
            return doFluidIOInternal(components, context, maxMultiplier);
        }
    }

    @Optional.Method(modid = "mekanism")
    private int doGasIOInternal(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxMultiplier) {
        List<IExtendedGasHandler> fluidHandlers = HybridFluidUtils.castGasHandlerComponents(components);

        long required = Math.round(RecipeModifier.applyModifiers(context, this, (double) this.required.getAmount(), false));
        long maxRequired = required * maxMultiplier;

        GasStack stack = ((HybridFluidGas) this.required).asGasStack().copy();
        long totalIO = HybridFluidUtils.doSimulateDrainOrFill(stack, fluidHandlers, maxRequired, actionType);

        if (totalIO < required) {
            return 0;
        }

        HybridFluidUtils.doDrainOrFill(stack, totalIO, fluidHandlers, actionType);

        return (int) (totalIO / required);
    }

    private int doFluidIOInternal(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxMultiplier) {
        List<IFluidHandler> fluidHandlers = HybridFluidUtils.castFluidHandlerComponents(components);

        long required = Math.round(RecipeModifier.applyModifiers(context, this, (double) this.required.getAmount(), false));
        long maxRequired = required * maxMultiplier;

        FluidStack stack = this.required.asFluidStack().copy();
        long totalIO = HybridFluidUtils.doSimulateDrainOrFill(stack, fluidHandlers, maxRequired, actionType);

        if (totalIO < required) {
            return 0;
        }

        HybridFluidUtils.doDrainOrFill(stack, totalIO, fluidHandlers, actionType);

        return (int) (totalIO / required);
    }

}
