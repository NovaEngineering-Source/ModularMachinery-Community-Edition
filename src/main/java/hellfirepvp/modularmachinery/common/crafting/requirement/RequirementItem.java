/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirement;

import com.google.common.collect.Lists;
import github.kasuminova.mmce.common.concurrent.Sync;
import github.kasuminova.mmce.common.helper.AdvancedItemChecker;
import github.kasuminova.mmce.common.helper.AdvancedItemModifier;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.jei.JEIComponentItem;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeItem;
import hellfirepvp.modularmachinery.common.integration.ingredient.IngredientItemStack;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import hellfirepvp.modularmachinery.common.util.FuelItemHelper;
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
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementItem
 * Created by HellFirePvP
 * Date: 24.02.2018 / 12:35
 */
public class RequirementItem extends ComponentRequirement.MultiCompParallelizable<ItemStack, RequirementTypeItem>
    implements ComponentRequirement.ChancedRequirement, ComponentRequirement.Parallelizable, Asyncable {
    public static final Random RD = new Random();

    public final ItemRequirementType requirementType;

    public final ItemStack required;

    public final String oreDictName;
    public final int    oreDictItemAmount;

    public final int fuelBurntime;

    public final List<ItemStack> previewItemStacks = new ArrayList<>();

    public final List<AdvancedItemModifier> itemModifierList = new ArrayList<>();

    public List<IngredientItemStack> cachedJEIIORequirementList = null;

    public NBTTagCompound tag               = null;
    public NBTTagCompound previewDisplayTag = null;

    public AdvancedItemChecker itemChecker = null;
    public float               chance      = 1F;

    public int minAmount = 1;
    public int maxAmount = 1;

    public int consumeDurability = 0;

    public RequirementItem(IOType ioType, ItemStack item) {
        super(RequirementTypesMM.REQUIREMENT_ITEM, ioType);
        this.requirementType = ItemRequirementType.ITEMSTACKS;
        this.required = item.copy();
        this.oreDictName = null;
        this.oreDictItemAmount = 0;
        this.fuelBurntime = 0;
        this.minAmount = item.getCount();
        this.maxAmount = item.getCount();
    }

    public RequirementItem(IOType ioType, String oreDictName, int oreDictAmount) {
        super(RequirementTypesMM.REQUIREMENT_ITEM, ioType);
        this.requirementType = ItemRequirementType.OREDICT;
        this.oreDictName = oreDictName;
        this.oreDictItemAmount = oreDictAmount;
        this.required = ItemStack.EMPTY;
        this.fuelBurntime = 0;
        this.minAmount = oreDictAmount;
        this.maxAmount = oreDictAmount;
    }

    @Deprecated
    public RequirementItem(IOType actionType, int fuelBurntime) {
        super(RequirementTypesMM.REQUIREMENT_ITEM, actionType);
        this.requirementType = ItemRequirementType.FUEL;
        this.fuelBurntime = fuelBurntime;
        this.oreDictName = null;
        this.oreDictItemAmount = 0;
        this.required = ItemStack.EMPTY;
    }

    public void setItemChecker(AdvancedItemChecker itemChecker) {
        this.itemChecker = itemChecker;
    }

    public void addItemModifier(AdvancedItemModifier itemModifier) {
        this.itemModifierList.add(itemModifier);
    }

    public void setConsumeDurability(final int durability) {
        this.consumeDurability = Math.max(0, durability);
    }

    public boolean supportsDurability() {
        return switch (this.requirementType) {
            case ITEMSTACKS -> !this.required.isEmpty() && this.required.isItemStackDamageable();
            case OREDICT -> ItemUtils.hasDamageableEntry(this.oreDictName);
            default -> false;
        };
    }

    @Override
    public int getSortingWeight() {
        return PRIORITY_WEIGHT_ITEM;
    }

    @Override
    public RequirementItem deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementItem deepCopyModified(List<RecipeModifier> modifiers) {
        float modValue = RecipeModifier.applyModifiers(modifiers, this, 1F, false);

        RequirementItem item = switch (this.requirementType) {
            case OREDICT -> new RequirementItem(this.actionType, this.oreDictName, Math.round(this.oreDictItemAmount * modValue));
            case FUEL -> new RequirementItem(this.actionType, Math.round(this.fuelBurntime * modValue));
            default -> {
                ItemStack inReq = this.required.copy();
                inReq.setCount(Math.round(inReq.getCount() * modValue));
                yield new RequirementItem(this.actionType, inReq);
            }
        };

        item.minAmount = Math.round(minAmount * modValue);
        item.maxAmount = Math.round(maxAmount * modValue);
        item.chance = this.chance;
        if (this.itemChecker != null) {
            item.itemChecker = this.itemChecker;
        } else if (this.tag != null) {
            item.tag = this.tag.copy();
        }
        if (!this.itemModifierList.isEmpty()) {
            item.itemModifierList.addAll(this.itemModifierList);
        }
        if (this.previewDisplayTag != null) {
            item.previewDisplayTag = this.previewDisplayTag.copy();
        }
        item.consumeDurability = this.consumeDurability;
        return item;
    }

    @Override
    public JEIComponent<ItemStack> provideJEIComponent() {
        return new JEIComponentItem(this);
    }

    @Override
    public void initializeJEIRequirements() {
        cachedJEIIORequirementList = asJEIIORequirementList();
    }

    public List<IngredientItemStack> asJEIIORequirementList() {
        switch (requirementType) {
            case ITEMSTACKS -> {
                ItemStack stack = ItemUtils.copyStackWithSize(required, required.getCount());
                if (previewDisplayTag != null) {
                    stack.setTagCompound(previewDisplayTag);
                } else if (tag != null) {
                    previewDisplayTag = tag.copy();
                    stack.setTagCompound(previewDisplayTag.copy());
                }
                if (minAmount != maxAmount) {
                    stack.setCount(maxAmount);
                }
                return Collections.singletonList(asIngredientItemStack(stack));
            }
            case OREDICT -> {
                NonNullList<ItemStack> stacks = OreDictionary.getOres(oreDictName);
                NonNullList<ItemStack> out = NonNullList.create();
                for (ItemStack oreDictIn : stacks) {
                    if (oreDictIn.getItemDamage() == OreDictionary.WILDCARD_VALUE && !oreDictIn.isItemStackDamageable() && oreDictIn.getItem().getCreativeTab() != null) {
                        oreDictIn.getItem().getSubItems(oreDictIn.getItem().getCreativeTab(), out);
                    } else {
                        out.add(oreDictIn);
                    }
                }
                NonNullList<IngredientItemStack> stacksOut = NonNullList.create();
                for (ItemStack itemStack : out) {
                    ItemStack copy = itemStack.copy();
                    if (minAmount != maxAmount) {
                        copy.setCount(maxAmount);
                    } else {
                        copy.setCount(oreDictItemAmount);
                    }
                    stacksOut.add(asIngredientItemStack(copy));
                }
                return stacksOut;
            }
            case FUEL -> {
                return Lists.transform(FuelItemHelper.getFuelItems(), JEIComponentItem.IngredientItemStackTransformer.INSTANCE);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void setChance(float chance) {
        this.chance = chance;
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        ResourceLocation compKey = this.getRequirementType().getRegistryName();
        return String.format("component.missing.%s.%s.%s",
            compKey.getNamespace(), compKey.getPath(), ioType.name().toLowerCase());
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.component();
        ComponentType cmpType = cmp.getComponentType();
        return (cmpType.equals(ComponentTypesMM.COMPONENT_ITEM) || cmpType.equals(ComponentTypesMM.COMPONENT_ITEM_FLUID_GAS))
            && cmp.ioType == actionType;
    }

    @Override
    public void startCrafting(List<ProcessingComponent<?>> components, RecipeCraftingContext context, ResultChance chance) {
        if (actionType == IOType.INPUT && chance.canWork(RecipeModifier.applyModifiers(context, this, this.chance, true))) {
            doItemIO(components, context, itemModifierList, chance);
        }
    }

    @Override
    public void finishCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        if (actionType == IOType.OUTPUT && chance.canWork(RecipeModifier.applyModifiers(context, this, this.chance, true))) {
            doItemIO(components, context, itemModifierList, chance);
        }
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        return doItemIO(components, context, Collections.emptyList(), ResultChance.GUARANTEED);
    }

    @Override
    public int getMaxParallelism(List<ProcessingComponent<?>> components, RecipeCraftingContext context, int maxParallelism) {
        if (ignoreOutputCheck && actionType == IOType.OUTPUT) {
            return maxParallelism;
        }
        if (parallelizeUnaffected) {
            if (doItemIOInternal(components, context, 1, Collections.emptyList(), ResultChance.GUARANTEED) >= 1) {
                return maxParallelism;
            }
            return 0;
        }
        return doItemIOInternal(components, context, maxParallelism, Collections.emptyList(), ResultChance.GUARANTEED);
    }

    private CraftCheck doItemIO(List<ProcessingComponent<?>> components, RecipeCraftingContext context, List<AdvancedItemModifier> itemModifiers, ResultChance chance) {
        int mul = doItemIOInternal(components, context, parallelism, itemModifiers, chance);
        if (mul < parallelism) {
            return switch (actionType) {
                case INPUT -> CraftCheck.failure("craftcheck.failure.item.input");
                case OUTPUT -> CraftCheck.failure("craftcheck.failure.item.output.space");
            };
        }
        return CraftCheck.success();
    }

    private int doItemIOInternal(final List<ProcessingComponent<?>> components,
                                 final RecipeCraftingContext context,
                                 final int maxMultiplier,
                                 final List<AdvancedItemModifier> itemModifiers,
                                 final ResultChance chance) {
        List<IItemHandlerModifiable> handlers = new ArrayList<>();
        for (ProcessingComponent<?> component : components) {
            IItemHandlerModifiable providedComponent = (IItemHandlerModifiable) component.getProvidedComponent();
            handlers.add(providedComponent);
        }

        return switch (actionType) {
            case INPUT -> consumeAllItems(handlers, context, maxMultiplier, itemModifiers, chance);
            case OUTPUT -> {
                if (ignoreOutputCheck) {
                    insertAllItems(handlers, context, maxMultiplier, itemModifiers, chance);
                    yield maxMultiplier;
                }
                yield insertAllItems(handlers, context, maxMultiplier, itemModifiers, chance);
            }
        };
    }

    @Nonnull
    @Override
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        return ItemUtils.copyItemHandlerComponents(components);
    }

    public int consumeAllItems(final List<IItemHandlerModifiable> handlers,
                               final RecipeCraftingContext context,
                               final int maxMultiplier,
                               final List<AdvancedItemModifier> itemModifiers,
                               final ResultChance chance) {
        final AtomicInteger consumed = new AtomicInteger();
        int toConsume = applyModifierAmount(context, chance != ResultChance.GUARANTEED || minAmount != maxAmount);

        int maxConsume = toConsume * maxMultiplier;

        if (toConsume <= 0) {
            return maxMultiplier;
        }

        ItemStack stack = ItemStack.EMPTY;

        switch (this.requirementType) {
            case ITEMSTACKS -> {
                stack = required.copy();
                if (tag != null) {
                    stack.setTagCompound(tag);
                }

                if (!itemModifiers.isEmpty()) {
                    stack.setCount(toConsume);

                    for (final AdvancedItemModifier modifier : itemModifiers) {
                        stack = modifier.apply(context.getMachineController(), stack);
                    }

                    toConsume = stack.getCount();
                    maxConsume = toConsume * maxMultiplier;
                }
            }
            case OREDICT -> {
                if (!itemModifiers.isEmpty()) {
                    stack = ItemUtils.getOredictItem(context, oreDictName, tag);
                    if (stack.isEmpty()) {
                        return maxMultiplier;
                    }

                    stack.setCount(toConsume);

                    if (tag != null) {
                        stack.setTagCompound(tag);
                    }

                    for (final AdvancedItemModifier modifier : itemModifiers) {
                        stack = modifier.apply(context.getMachineController(), stack);
                    }

                    toConsume = stack.getCount();
                    maxConsume = toConsume * maxMultiplier;
                }
            }
        }

        if (toConsume <= 0) {
            return maxMultiplier;
        }

        final ItemStack finalStack = stack;
        final int finalMaxConsume = maxConsume;
        if (consumeDurability > 0 && getActionType() == IOType.INPUT) {
            final ItemStack referenceStack = finalStack.copy();
            switch (this.requirementType) {
                case ITEMSTACKS -> {
                    for (final IItemHandlerModifiable handler : handlers) {
                        Sync.executeSyncIfPresent(handler, () -> {
                            final int remaining = finalMaxConsume - consumed.get();
                            if (remaining <= 0) {
                                return;
                            }
                            final int damaged;
                            if (itemChecker != null) {
                                damaged = ItemUtils.damageAll(handler, referenceStack, remaining, consumeDurability, itemChecker, context.getMachineController());
                            } else {
                                damaged = ItemUtils.damageAll(handler, referenceStack, remaining, consumeDurability, tag);
                            }
                            consumed.addAndGet(damaged);
                        });
                        if (consumed.get() >= maxConsume) {
                            break;
                        }
                    }
                }
                case OREDICT -> {
                    for (final IItemHandlerModifiable handler : handlers) {
                        Sync.executeSyncIfPresent(handler, () -> {
                            final int remaining = finalMaxConsume - consumed.get();
                            if (remaining <= 0) {
                                return;
                            }
                            final int damaged;
                            if (itemChecker != null) {
                                damaged = ItemUtils.damageAll(handler, oreDictName, remaining, consumeDurability, itemChecker, context.getMachineController());
                            } else {
                                damaged = ItemUtils.damageAll(handler, oreDictName, remaining, consumeDurability, tag);
                            }
                            consumed.addAndGet(damaged);
                        });
                        if (consumed.get() >= maxConsume) {
                            break;
                        }
                    }
                }
                default -> {
                }
            }
            return consumed.get() / toConsume;
        }
        switch (this.requirementType) {
            case ITEMSTACKS -> {
                for (final IItemHandlerModifiable handler : handlers) {
                    stack.setCount(maxConsume - consumed.get());
                    Sync.executeSyncIfPresent(handler, () -> {
                        if (itemChecker != null) {
                            consumed.addAndGet(ItemUtils.consumeAll(handler, finalStack, itemChecker, context.getMachineController()));
                        } else {
                            consumed.addAndGet(ItemUtils.consumeAll(handler, finalStack, tag));
                        }
                    });
                    if (consumed.get() >= maxConsume) {
                        break;
                    }
                }
            }
            case OREDICT -> {
                for (final IItemHandlerModifiable handler : handlers) {
                    Sync.executeSyncIfPresent(handler, () -> {
                        if (itemChecker != null) {
                            consumed.addAndGet(ItemUtils.consumeAll(handler, oreDictName, finalMaxConsume - consumed.get(), itemChecker, context.getMachineController()));
                        } else {
                            consumed.addAndGet(ItemUtils.consumeAll(handler, oreDictName, finalMaxConsume - consumed.get(), tag));
                        }
                    });
                    if (consumed.get() >= maxConsume) {
                        break;
                    }
                }
            }
        }

        return consumed.get() / toConsume;
    }

    public int insertAllItems(final List<IItemHandlerModifiable> handlers,
                              final RecipeCraftingContext context,
                              final int maxMultiplier,
                              final List<AdvancedItemModifier> itemModifiers,
                              final ResultChance chance) {
        if (fuelBurntime > 0 && oreDictName == null && required.isEmpty()) {
            throw new IllegalStateException("Invalid item output!");
        }

        final AtomicInteger inserted = new AtomicInteger();
        int toInsert = applyModifierAmount(context, chance != ResultChance.GUARANTEED || minAmount != maxAmount);

        if (toInsert <= 0) {
            return maxMultiplier;
        }

        ItemStack stack;
        switch (this.requirementType) {
            case ITEMSTACKS -> stack = ItemUtils.copyStackWithSize(required, 1);
            case OREDICT -> stack = ItemUtils.getOredictItem(context, oreDictName, tag);
            default -> {
                return 0;
            }
        }
        if (tag != null) {
            stack.setTagCompound(tag);
        }

        if (!itemModifiers.isEmpty()) {
            for (final AdvancedItemModifier modifier : itemModifiers) {
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
            final ItemStack finalStack = stack;
            Sync.executeSyncIfPresent(handler, () -> inserted.addAndGet(ItemUtils.insertAll(finalStack, handler, maxInsert - inserted.get())));
            if (inserted.get() >= maxInsert) {
                break;
            }
        }

        return inserted.get() / toInsert;
    }

    public IngredientItemStack asIngredientItemStack(ItemStack stack) {
        return new IngredientItemStack(stack, minAmount, maxAmount, chance);
    }

    protected int applyModifierAmount(final RecipeCraftingContext context, boolean randomAmount) {
        if (randomAmount) {
            int amount = minAmount + RD.nextInt((maxAmount - minAmount) + 1);
            return Math.round(RecipeModifier.applyModifiers(context, this, amount, false));
        } else {
            return Math.round(RecipeModifier.applyModifiers(context, this, maxAmount, false));
        }
    }

    public enum ItemRequirementType {
        ITEMSTACKS,
        OREDICT,
        FUEL,
        ITEMSTACK_ARRAY

    }

}
