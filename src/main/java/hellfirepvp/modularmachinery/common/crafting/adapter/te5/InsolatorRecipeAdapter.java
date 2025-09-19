package hellfirepvp.modularmachinery.common.crafting.adapter.te5;

import cofh.thermalexpansion.util.managers.machine.InsolatorManager;
import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import github.kasuminova.mmce.common.util.HashedItemStack;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InsolatorRecipeAdapter extends RecipeAdapter {

    public static final int ENERGY_PER_TICK = 20;

    private final boolean tree;

    public InsolatorRecipeAdapter(final boolean tree) {
        super(new ResourceLocation("thermalexpansion", tree ? "insolator_tree" : "insolator"));
        this.tree = tree;
    }

    private static boolean isFertilizer(final ItemStack primaryInput) {
        return InsolatorManager.isItemFertilizer(primaryInput) || primaryInput.getItem() == Items.GLOWSTONE_DUST; // AE2 Seeds
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(final ResourceLocation owningMachineName,
                                                      final List<RecipeModifier> modifiers,
                                                      final List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      final Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      final List<String> recipeTooltips) {
        List<MachineRecipe> recipes = new ArrayList<>();
        InsolatorManager.InsolatorRecipe[] insolatorRecipes = InsolatorManager.getRecipeList();

        // Sort by output amount and secondary output chance, smaller is first.
        Arrays.sort(insolatorRecipes, (o1, o2) -> {
            ItemStack primaryOutput = o1.getPrimaryOutput();
            ItemStack primaryOutput2 = o2.getPrimaryOutput();
            int result = Integer.compare(primaryOutput.getCount(), primaryOutput2.getCount());
            if (result != 0) {
                return result;
            }
            return Integer.compare(o1.getSecondaryOutputChance(), o2.getSecondaryOutputChance());
        });

        Set<HashedItemStack> inputs = new ObjectOpenHashSet<>();
        for (final InsolatorManager.InsolatorRecipe insolatorRecipe : insolatorRecipes) {
            boolean isTree = insolatorRecipe.getType() == InsolatorManager.Type.TREE;
            if (this.tree != isTree) {
                continue;
            }

            ItemStack primaryInput = insolatorRecipe.getPrimaryInput();
            ItemStack secondaryInput = insolatorRecipe.getSecondaryInput();
            boolean hasFertilizer = insolatorRecipe.hasFertilizer();

            // Don't add fertilizers as input.
            ItemStack input = hasFertilizer
                ? isFertilizer(primaryInput) ? secondaryInput : primaryInput
                : primaryInput;
            // Skip duplicated inputs.
            HashedItemStack hashed = HashedItemStack.ofUnsafe(input);
            if (inputs.contains(hashed)) {
                continue;
            } else {
                inputs.add(hashed.copy());
            }

            ItemStack primaryOutput = insolatorRecipe.getPrimaryOutput();
            ItemStack secondaryOutput = insolatorRecipe.getSecondaryOutput();
            int secondaryOutputChance = insolatorRecipe.getSecondaryOutputChance();
            int water = insolatorRecipe.getWater();
            int energy = insolatorRecipe.getEnergy();

            MachineRecipe recipe = createRecipeShell(new ResourceLocation("thermalexpansion", (tree ? "insolator_tree_" : "insolator_") + incId),
                owningMachineName, Math.round(RecipeModifier.applyModifiers(
                    modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, (float) energy / ENERGY_PER_TICK, false)),
                incId, false
            );
            incId++;

            int energyPerTick = Math.round(RecipeModifier.applyModifiers(
                modifiers, RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT, ENERGY_PER_TICK, false)
            );
            // Energy
            if (energyPerTick > 0) {
                recipe.addRequirement(new RequirementEnergy(IOType.INPUT, energyPerTick));
            }

            int primaryInputAmount = Math.round(RecipeModifier.applyModifiers(
                modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, input.getCount(), false
            ));
            int secondaryInputAmount = hasFertilizer ? 0 : Math.round(RecipeModifier.applyModifiers(
                modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, secondaryInput.getCount(), false
            ));
            int waterAmount = Math.round(RecipeModifier.applyModifiers(
                modifiers, RequirementTypesMM.REQUIREMENT_FLUID, IOType.INPUT, water, false
            ));
            int primaryOutputAmount = Math.round(RecipeModifier.applyModifiers(
                modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, primaryOutput.getCount(), false
            ));
            int secondaryOutputAmount = Math.round(RecipeModifier.applyModifiers(
                modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, secondaryOutput.getCount(), false
            ));
            float secondaryOutputChanceModified = RecipeModifier.applyModifiers(
                modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, (float) secondaryOutputChance / 100, true
            );

            // Primary Input
            if (primaryInputAmount > 0) {
                recipe.addRequirement(new RequirementItem(IOType.INPUT, ItemUtils.copyStackWithSize(input, primaryInputAmount)));
            }
            // Secondary Input
            if (secondaryInputAmount > 0) {
                recipe.addRequirement(new RequirementItem(IOType.INPUT, ItemUtils.copyStackWithSize(secondaryInput, secondaryInputAmount)));
            }
            // Water Input
            if (waterAmount > 0) {
                recipe.addRequirement(new RequirementFluid(IOType.INPUT, new FluidStack(FluidRegistry.WATER, waterAmount)));
            }
            // Primary Output
            if (primaryOutputAmount > 0) {
                recipe.addRequirement(new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(primaryOutput, primaryOutputAmount)));
            }
            // Secondary Output
            if (secondaryOutputAmount > 0) {
                if (secondaryOutputChanceModified > 1) {
                    // When chance >= 100%
                    int secOutputAmt = secondaryOutputAmount * (int) secondaryOutputChanceModified;
                    recipe.addRequirement(new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(secondaryOutput, secOutputAmt)));
                    float thirdOutputChance = secondaryOutputChanceModified - (int) secondaryOutputChanceModified;
                    if (thirdOutputChance > 0) {
                        RequirementItem thirdOutput = new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(secondaryOutput, secondaryOutputAmount));
                        thirdOutput.setChance(thirdOutputChance);
                        recipe.addRequirement(thirdOutput);
                    }
                } else {
                    // When chance < 100%
                    RequirementItem secondOutput = new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(secondaryOutput, secondaryOutputAmount));
                    secondOutput.setChance(secondaryOutputChanceModified);
                    recipe.addRequirement(secondOutput);
                }
            }
            recipes.add(recipe);
        }

        return recipes;
    }

}
