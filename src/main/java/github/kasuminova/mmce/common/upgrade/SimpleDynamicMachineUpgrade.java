package github.kasuminova.mmce.common.upgrade;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.helper.IFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenSetter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ZenRegister
@ZenClass("mods.modularmachinery.SimpleDynamicMachineUpgrade")
public class SimpleDynamicMachineUpgrade extends DynamicMachineUpgrade {
    private IFunction<SimpleDynamicMachineUpgrade, String[]> descriptionHandler       = null;
    private IFunction<SimpleDynamicMachineUpgrade, String[]> busGuiDescriptionHandler = null;

    private NBTTagCompound itemData   = new NBTTagCompound();
    private NBTTagCompound customData = new NBTTagCompound();

    public SimpleDynamicMachineUpgrade(final UpgradeType type) {
        super(type);
    }

    /**
     * 获取物品所储存的自定义 NBT。
     * NBT 会被储存在物品上而不是升级总线上。
     * 用法与 IMachineController 中的 customData 相同。
     *
     * @return IData，永不为空。
     */
    @ZenGetter("itemData")
    public IData getItemData() {
        return CraftTweakerMC.getIDataModifyable(itemData);
    }

    /**
     * 储存 IData 至物品所储存的自定义 NBT。
     * NBT 会被储存在物品上而不是升级总线上。
     * 用法与 IMachineController 中的 customData 相同。
     *
     * @param itemData IData，不能为空。
     */
    @ZenSetter("itemData")
    public void setItemData(final IData itemData) {
        this.itemData = CraftTweakerMC.getNBTCompound(itemData);
    }

    /**
     * 获取升级总线所储存的对应升级的自定义 NBT。
     * NBT 会被储存在升级总线上而不是物品上。
     * 用法与 IMachineController 中的 customData 相同。
     *
     * @return IData，永不为空。
     */
    @ZenGetter("customData")
    public IData getCustomData() {
        return CraftTweakerMC.getIDataModifyable(customData);
    }

    /**
     * 储存 IData 至升级总线所储存的对应升级的自定义 NBT。
     * NBT 会被储存在升级总线上而不是物品上。
     * 用法与 IMachineController 中的 customData 相同。
     *
     * @param customData IData，不能为空。
     */
    @ZenSetter("customData")
    public void setCustomData(final IData customData) {
        this.customData = CraftTweakerMC.getNBTCompound(customData);
    }

    @ZenGetter("parentStack")
    public IItemStack getParentStackCT() {
        return CraftTweakerMC.getIItemStack(parentStack);
    }

    @ZenMethod
    public void decrementItemDurability(final int durability) {
        if (valid && parentBus != null && busInventoryIndex != -1 && !parentStack.isEmpty() && parentStack.isItemStackDamageable()) {
            int maxDamage = parentStack.getMaxDamage();
            int itemDamage = parentStack.getItemDamage();

            if (itemDamage + durability >= maxDamage) {
                parentBus.getInventory().setStackInSlot(busInventoryIndex, ItemStack.EMPTY);
            } else {
                ItemStack copied = parentStack.copy();
                copied.setItemDamage(itemDamage + durability);
                parentBus.getInventory().setStackInSlot(busInventoryIndex, copied);
            }
        }
    }

    public void setDescriptionHandler(final IFunction<SimpleDynamicMachineUpgrade, String[]> handler) {
        this.descriptionHandler = handler;
    }

    public void setBusGUIDescriptionHandler(final IFunction<SimpleDynamicMachineUpgrade, String[]> handler) {
        this.busGuiDescriptionHandler = handler;
    }

    @Override
    public void readItemNBT(final NBTTagCompound tag) {
        itemData = tag;
    }

    @Override
    public NBTTagCompound writeItemNBT() {
        return itemData;
    }

    @Override
    public void readNBT(final NBTTagCompound tag) {
        super.readNBT(tag);
        customData = tag;
    }

    @Override
    public NBTTagCompound writeNBT() {
        return customData;
    }

    @Override
    public List<String> getDescriptions() {
        return descriptionHandler == null ? Collections.emptyList() : Arrays.asList(descriptionHandler.apply(this));
    }

    @Override
    public List<String> getBusGUIDescriptions() {
        return busGuiDescriptionHandler == null ? Collections.emptyList() : Arrays.asList(busGuiDescriptionHandler.apply(this));
    }

    @Override
    public SimpleDynamicMachineUpgrade copy(ItemStack owner) {
        SimpleDynamicMachineUpgrade upgrade = new SimpleDynamicMachineUpgrade(getType());
        upgrade.descriptionHandler = descriptionHandler;
        upgrade.busGuiDescriptionHandler = busGuiDescriptionHandler;
        upgrade.eventProcessor.putAll(eventProcessor);
        upgrade.parentStack = owner;
        return upgrade;
    }

    public boolean upgradeEquals(final Object obj) {
        if (!(obj instanceof SimpleDynamicMachineUpgrade)) {
            return false;
        }
        return type.equals(((SimpleDynamicMachineUpgrade) obj).type);
    }

    @Override
    public boolean equals(final Object obj) {
        return false;
    }
}
