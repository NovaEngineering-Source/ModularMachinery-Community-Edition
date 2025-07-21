package hellfirepvp.modularmachinery.common.crafting.adapter.draconicevolution;

import com.brandon3055.draconicevolution.api.fusioncrafting.FusionRecipeAPI;
import com.brandon3055.draconicevolution.api.fusioncrafting.IFusionRecipe;
import com.brandon3055.draconicevolution.api.OreDictHelper;
import crafttweaker.util.IEventHandler;
import github.kasuminova.mmce.common.event.recipe.RecipeEvent;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentSelectorTag;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * 龙之研究基础聚合合成适配器 (Tier 0 - Basic)
 */
public class AdapterDEBasicFusionCrafting extends RecipeAdapter {

    private static final int BASE_ENERGY_PER_TICK = 800;

    public AdapterDEBasicFusionCrafting() {
        super(new ResourceLocation("draconicevolution", "fusion_crafting_basic"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName,
                                                      List<RecipeModifier> modifiers,
                                                      List<ComponentRequirement<?, ?>> additionalRequirements,
                                                      Map<Class<?>, List<IEventHandler<RecipeEvent>>> eventHandlers,
                                                      List<String> recipeTooltips) {

        List<MachineRecipe> recipes = new ArrayList<>();
        List<IFusionRecipe> fusionRecipes = FusionRecipeAPI.getRecipes();
        
        if (fusionRecipes.isEmpty()) {
            return recipes;
        }

        for (IFusionRecipe fusionRecipe : fusionRecipes) {
            try {
                // 只处理基础等级 (Tier 0)
                if (fusionRecipe.getRecipeTier() != 0) {
                    continue;
                }
                
                MachineRecipe machineRecipe = createMachineRecipe(fusionRecipe, owningMachineName, modifiers);
                if (machineRecipe != null) {
                    // 添加额外的需求
                    for (ComponentRequirement<?, ?> requirement : additionalRequirements) {
                        machineRecipe.addRequirement(requirement);
                    }

                    // 添加事件处理器
                    for (Map.Entry<Class<?>, List<IEventHandler<RecipeEvent>>> entry : eventHandlers.entrySet()) {
                        for (IEventHandler<RecipeEvent> handler : entry.getValue()) {
                            machineRecipe.addRecipeEventHandler(entry.getKey(), handler);
                        }
                    }

                    // 添加提示文本
                    for (String tooltip : recipeTooltips) {
                        machineRecipe.addTooltip(tooltip);
                    }

                    recipes.add(machineRecipe);
                }
            } catch (Exception e) {
                // 跳过有问题的配方，避免崩溃
                continue;
            }
        }

        return recipes;
    }

    /**
     * 将IFusionRecipe转换为MachineRecipe
     */
    private MachineRecipe createMachineRecipe(IFusionRecipe fusionRecipe, ResourceLocation owningMachineName, List<RecipeModifier> modifiers) {
        ItemStack catalyst = fusionRecipe.getRecipeCatalyst();
        ItemStack output = fusionRecipe.getRecipeOutput(null);
        List<?> ingredients = fusionRecipe.getRecipeIngredients();
        long energyCost = fusionRecipe.getIngredientEnergyCost();
        int tier = fusionRecipe.getRecipeTier();

        // 检查必要的组件
        if (catalyst.isEmpty() || output.isEmpty() || ingredients.isEmpty()) {
            return null;
        }

        // 计算处理时间 (基础等级相对较快)
        int processingTime = calculateProcessingTime(energyCost, ingredients.size(), tier, modifiers);
        
        MachineRecipe recipe = createRecipeShell(
                new ResourceLocation("draconicevolution", "fusion_basic_" + incId),
                owningMachineName,
                processingTime,
                incId++,
                false
        );

        // 添加能量需求 (基础等级能耗较低)
        int energyPerTick = calculateEnergyPerTick(energyCost, ingredients.size(), tier, modifiers);
        if (energyPerTick > 0) {
            recipe.addRequirement(new RequirementEnergy(IOType.INPUT, energyPerTick));
        }

        // 添加催化剂输入 (不会被消耗)
        int catalystAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, catalyst.getCount(), false));
        if (catalystAmount > 0) {
            RequirementItem catalystReq = new RequirementItem(IOType.INPUT, ItemUtils.copyStackWithSize(catalyst, catalystAmount));
            catalystReq.setTag(new ComponentSelectorTag("catalyst")); // 标记为催化剂
            recipe.addRequirement(catalystReq);
        }

        // 添加材料输入
        for (Object ingredient : ingredients) {
            ItemStack ingredientStack = resolveIngredient(ingredient);
            if (!ingredientStack.isEmpty()) {
                int ingredientAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.INPUT, ingredientStack.getCount(), false));
                if (ingredientAmount > 0) {
                    recipe.addRequirement(new RequirementItem(IOType.INPUT, ItemUtils.copyStackWithSize(ingredientStack, ingredientAmount)));
                }
            }
        }

        // 添加输出
        int outputAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, IOType.OUTPUT, output.getCount(), false));
        if (outputAmount > 0) {
            recipe.addRequirement(new RequirementItem(IOType.OUTPUT, ItemUtils.copyStackWithSize(output, outputAmount)));
        }

        return recipe;
    }

    /**
     * 解析配方成分（支持ore dictionary）
     */
    private ItemStack resolveIngredient(Object ingredient) {
        if (ingredient instanceof ItemStack) {
            return (ItemStack) ingredient;
        } else if (ingredient instanceof String) {
            // Ore dictionary
            List<ItemStack> ores = OreDictionary.getOres((String) ingredient);
            if (!ores.isEmpty()) {
                return ores.get(0);
            }
        } else {
            // 尝试使用DE的OreDictHelper
            ItemStack resolved = OreDictHelper.resolveObject(ingredient);
            if (!resolved.isEmpty()) {
                return resolved;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 计算处理时间 (基础等级)
     */
    private int calculateProcessingTime(long energyCost, int ingredientCount, int tier, List<RecipeModifier> modifiers) {
        // 基础等级处理时间较短
        float baseTime = (energyCost / 2000.0f + ingredientCount * 5.0f);
        
        // 限制最小和最大时间
        baseTime = Math.max(10, Math.min(baseTime, 600)); // 0.5秒到30秒
        
        return Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_DURATION, IOType.INPUT, baseTime, false));
    }

    /**
     * 计算每tick能耗 (基础等级)
     */
    private int calculateEnergyPerTick(long energyCost, int ingredientCount, int tier, List<RecipeModifier> modifiers) {
        // 总能耗 = 每个材料的能耗 * 材料数量
        long totalEnergy = energyCost * ingredientCount;
        
        // 基础等级能耗较低
        int baseEnergyPerTick = BASE_ENERGY_PER_TICK;
        
        // 根据总能耗调整
        if (totalEnergy > 500000) { // 如果总能耗很高，增加每tick消耗
            baseEnergyPerTick = (int) Math.min(baseEnergyPerTick * 1.5, totalEnergy / 150);
        }
        
        return Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ENERGY, IOType.INPUT, baseEnergyPerTick, false));
    }
}
