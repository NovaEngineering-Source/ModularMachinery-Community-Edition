package hellfirepvp.modularmachinery.common.crafting.requirement;

import github.kasuminova.mmce.common.helper.AdvancedItemChecker;
import github.kasuminova.mmce.common.helper.AdvancedItemModifier;
import github.kasuminova.mmce.common.itemtype.ChancedIngredientStack;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.jei.JEIComponentIngredientArray;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeIngredientArray;
import hellfirepvp.modularmachinery.common.integration.ingredient.IngredientItemStack;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.IItemHandlerImpl;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequirementIngredientArray extends ComponentRequirement.MultiCompParallelizable<ItemStack, RequirementTypeIngredientArray>
        implements ComponentRequirement.ChancedRequirement {

    protected final List<ChancedIngredientStack> ingredients;
    public List<IngredientItemStack> cachedJEIIORequirementList = null;

    public float chance = 1.0F;

    /**
     * <p>物品组输入，仅消耗组内的其中一个</p>
     * <p>**仅限输入**</p>
     */
    public RequirementIngredientArray(List<ChancedIngredientStack> ingredients) {
        super(RequirementTypesMM.REQUIREMENT_INGREDIENT_ARRAY, IOType.INPUT);

        this.ingredients = ingredients;
    }

    public RequirementIngredientArray(List<ChancedIngredientStack> ingredients, IOType ioType) {
        super(RequirementTypesMM.REQUIREMENT_INGREDIENT_ARRAY, ioType);

        this.ingredients = ingredients;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.component();
        ComponentType cmpType = cmp.getComponentType();
        return (cmpType.equals(ComponentTypesMM.COMPONENT_ITEM) || cmpType.equals(ComponentTypesMM.COMPONENT_ITEM_FLUID)) 
               && cmp.ioType == actionType;
    }

    @Override
    public RequirementIngredientArray deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementIngredientArray deepCopyModified(List<RecipeModifier> modifiers) {
        ArrayList<ChancedIngredientStack> copiedIngredients = new ArrayList<>();

        ingredients.forEach(item -> {
            ChancedIngredientStack copied = item.copy();

            switch (copied.ingredientType) {
                case ITEMSTACK -> {
                    ItemStack itemStack = copied.itemStack;
                    int amt = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, actionType, itemStack.getCount(), false));
                    itemStack.setCount(amt);
                }
                case ORE_DICT -> copied.count = Math.round(RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, actionType, item.count, false));
            }
            copied.chance = RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, actionType, item.chance, true);

            copiedIngredients.add(copied);
        });

        RequirementIngredientArray requirement = new RequirementIngredientArray(copiedIngredients, getActionType());
        requirement.chance = RecipeModifier.applyModifiers(modifiers, RequirementTypesMM.REQUIREMENT_ITEM, actionType, chance, true);
        return requirement;
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        ResourceLocation compKey = RequirementTypesMM.KEY_REQUIREMENT_ITEM;
        return String.format("component.missing.%s.%s.%s",
                compKey.getNamespace(), compKey.getPath(), ioType.name().toLowerCase());
    }

    @Override
    public JEIComponent<ItemStack> provideJEIComponent() {
        return new JEIComponentIngredientArray(this);
    }

    @Override
    public void initializeJEIRequirements() {
        cachedJEIIORequirementList = asJEIIORequirementList();
    }

    public List<IngredientItemStack> asJEIIORequirementList() {
        List<IngredientItemStack> copiedIngredients = new ArrayList<>();
        for (ChancedIngredientStack ingredient : getIngredients()) {
            switch (ingredient.ingredientType) {
                case ITEMSTACK -> {
                    ItemStack itemStack = ingredient.itemStack;
                    ItemStack copiedStack = ItemUtils.copyStackWithSize(itemStack, itemStack.getCount());
                    if (ingredient.minCount != ingredient.maxCount) {
                        copiedStack.setCount(ingredient.maxCount);
                    }
                    copiedIngredients.add(ingredient.asIngredientItemStack(copiedStack));
                }
                case ORE_DICT -> {
                    NonNullList<ItemStack> stacks = OreDictionary.getOres(ingredient.oreDictName);
                    NonNullList<ItemStack> out = NonNullList.create();
                    for (ItemStack oreDictIn : stacks) {
                        if (oreDictIn.getItemDamage() == OreDictionary.WILDCARD_VALUE && !oreDictIn.isItemStackDamageable() && oreDictIn.getItem().getCreativeTab() != null) {
                            oreDictIn.getItem().getSubItems(oreDictIn.getItem().getCreativeTab(), out);
                        } else {
                            out.add(oreDictIn);
                        }
                    }

                    for (ItemStack itemStack : out) {
                        ItemStack copied = itemStack.copy();
                        if (ingredient.minCount != ingredient.maxCount) {
                            copied.setCount(ingredient.maxCount);
                        } else {
                            copied.setCount(ingredient.count);
                        }
                        copiedIngredients.add(ingredient.asIngredientItemStack(copied));
                    }
                }
            }
        }

        return copiedIngredients;
    }

    @Override
    public void setChance(float chance) {
        this.chance = chance;
    }

    @Override
    public void startCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        if (actionType == IOType.INPUT && chance.canWork(RecipeModifier.applyModifiers(context, RequirementTypesMM.REQUIREMENT_ITEM, actionType, this.chance, true))) {
            doItemIO(components, context, chance);
        }
    }

    @Override
    public void finishCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (actionType == IOType.OUTPUT && chance.canWork(RecipeModifier.applyModifiers(context, RequirementTypesMM.REQUIREMENT_ITEM, actionType, this.chance, true))) {
            doItemIO(components, context, chance);
        }
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        return doItemIO(components, context, ResultChance.GUARANTEED);
    }

    @Override
    public int getMaxParallelism(List<ProcessingComponent<?>> components, RecipeCraftingContext context, int maxParallelism) {
        if (ignoreOutputCheck && actionType == IOType.OUTPUT) {
            return maxParallelism;
        }
        if (parallelizeUnaffected) {
            if (doItemIOInternal(components, context, 1, ResultChance.GUARANTEED) >= 1) {
                return maxParallelism;
            }
            return 0;
        }
        return doItemIOInternal(components, context, maxParallelism, ResultChance.GUARANTEED);
    }

    @Nonnull
    @Override
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        return ItemUtils.copyItemHandlerComponents(components);
    }

    private CraftCheck doItemIO(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        int mul = doItemIOInternal(components, context, parallelism, chance);
        if (mul < parallelism) {
            return CraftCheck.failure("craftcheck.failure.item.input");
        }
        return CraftCheck.success();
    }

    private int doItemIOInternal(List<ProcessingComponent<?>> components, RecipeCraftingContext context, int maxMultiplier, ResultChance chance) {
        List<IItemHandlerImpl> handlers = new ArrayList<>();
        for (ProcessingComponent<?> component : components) {
            IItemHandlerImpl providedComponent = (IItemHandlerImpl) component.getProvidedComponent();
            handlers.add(providedComponent);
        }

        return switch (actionType) {
            case INPUT -> consumeAllItems(handlers, context, maxMultiplier, chance);
            case OUTPUT -> {
                if (ignoreOutputCheck) {
                    insertAllItems(handlers, context, maxMultiplier, chance);
                    yield maxMultiplier;
                }
                yield insertAllItems(handlers, context, maxMultiplier, chance);
            }
        };
    }

    public int consumeAllItems(final List<IItemHandlerImpl> handlers,
                               final RecipeCraftingContext context,
                               final int maxMultiplier,
                               final ResultChance chance)
    {
        int ingredientConsumed = 0;

        for (final ChancedIngredientStack ingredient : ingredients) {
            int toConsume = applyModifierAmount(context, ingredient.count, ingredient.minCount, ingredient.maxCount, chance != ResultChance.GUARANTEED);
            int maxConsume = toConsume * (maxMultiplier - ingredientConsumed);
            int consumed = 0;

            AdvancedItemChecker checker;

            switch (ingredient.ingredientType) {
                case ITEMSTACK -> {
                    checker = ingredient.itemChecker;
                    ItemStack stack = ItemUtils.copyStackWithSize(ingredient.itemStack, toConsume);

                    for (final IItemHandlerImpl handler : handlers) {
                        stack.setCount(maxConsume - consumed);
                        if (checker != null) {
                            consumed += ItemUtils.consumeAll(
                                    handler, stack, checker, context.getMachineController()) / toConsume;
                        } else {
                            consumed += ItemUtils.consumeAll(
                                    handler, stack, ingredient.tag);
                        }
                        if (consumed >= maxConsume) {
                            break;
                        }
                    }
                }
                case ORE_DICT -> {
                    checker = ingredient.itemChecker;

                    for (final IItemHandlerImpl handler : handlers) {
                        if (checker != null) {
                            consumed += ItemUtils.consumeAll(
                                    handler, ingredient.oreDictName, maxConsume - consumed, checker, context.getMachineController());
                        } else {
                            consumed += ItemUtils.consumeAll(
                                    handler, ingredient.oreDictName, maxConsume - consumed, ingredient.tag);
                        }
                        if (consumed >= maxConsume) {
                            break;
                        }
                    }
                }
            }

            ingredientConsumed += (consumed / toConsume);
        }

        return ingredientConsumed;
    }

    public int insertAllItems(final List<IItemHandlerImpl> handlers,
                              final RecipeCraftingContext context,
                              final int maxMultiplier,
                              final ResultChance chance)
    {
        ChancedIngredientStack selected = chance == ResultChance.GUARANTEED ? selectMaxCountStack() : selectRandomStack();
        if (selected == null) {
            return 0;
        }

        int inserted = 0;
        int toInsert = applyModifierAmount(context, selected.count, selected.minCount, selected.maxCount, chance != ResultChance.GUARANTEED);

        if (toInsert <= 0) {
            return maxMultiplier;
        }

        ItemStack stack;
        NBTTagCompound tag = selected.tag;
        switch (selected.ingredientType) {
            case ITEMSTACK -> stack = ItemUtils.copyStackWithSize(selected.itemStack, 1);
            case ORE_DICT -> stack = ItemUtils.getOredictItem(context, selected.oreDictName, tag);
            default -> {
                return 0;
            }
        }
        if (tag != null) {
            stack.setTagCompound(tag);
        }

        if (!selected.itemModifierList.isEmpty()) {
            for (final AdvancedItemModifier modifier : selected.itemModifierList) {
                stack = modifier.apply(context.getMachineController(), stack);
            }
            toInsert *= stack.getCount();
            if (toInsert <= 0) {
                return maxMultiplier;
            }
            stack.setCount(1);
        }

        int maxInsert = toInsert * maxMultiplier;
        for (final IItemHandlerModifiable handler : handlers) {
            synchronized (handler) {
                inserted += ItemUtils.insertAll(stack, handler, maxInsert - inserted);
            }
            if (inserted >= maxInsert) {
                break;
            }
        }

        return inserted / toInsert;
    }

    protected int applyModifierAmount(final RecipeCraftingContext context, int defaultCount, int minAmount, int maxAmount, boolean randomAmount) {
        if (randomAmount) {
            int amount = minAmount + RequirementItem.RD.nextInt((maxAmount - minAmount) + 1);
            return Math.round(RecipeModifier.applyModifiers(context, RequirementTypesMM.REQUIREMENT_ITEM, actionType, amount, false));
        } else {
            return Math.round(RecipeModifier.applyModifiers(context, RequirementTypesMM.REQUIREMENT_ITEM, actionType, maxAmount, false));
        }
    }

    protected ChancedIngredientStack selectRandomStack() {
        float totalChance = 0;
        for (final ChancedIngredientStack ingredient : ingredients) {
            totalChance += ingredient.chance;
        }
        float randomChance = RequirementItem.RD.nextFloat() * totalChance;

        float chanceCount = 0;
        ChancedIngredientStack selected = null;
        for (final ChancedIngredientStack ingredient : ingredients) {
            chanceCount += ingredient.chance;
            if (chanceCount >= randomChance) {
                selected = ingredient;
                break;
            }
        }

        if (selected == null) {
            ModularMachinery.log.warn("[MM-RequirementIngredientArray] Invalid selected stack! totalChance: " + totalChance + ", randomChance: " + randomChance);
        }

        return selected;
    }

    protected ChancedIngredientStack selectMaxCountStack() {
        ChancedIngredientStack selected = null;
        for (final ChancedIngredientStack ingredient : ingredients) {
            if (selected == null) {
                selected = ingredient;
                continue;
            }

            if (ingredient.maxCount > selected.maxCount) {
                selected = ingredient;
            }
        }
        return selected;
    }

    public List<ChancedIngredientStack> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }
}
