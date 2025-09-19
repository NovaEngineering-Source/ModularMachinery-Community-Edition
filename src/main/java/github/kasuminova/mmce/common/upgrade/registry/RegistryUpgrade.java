package github.kasuminova.mmce.common.upgrade.registry;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.upgrade.MachineUpgrade;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ZenRegister
@ZenClass("mods.modularmachinery.RegistryUpgrade")
public class RegistryUpgrade {

    public static final HashMap<String, MachineUpgrade> UPGRADES      = new HashMap<>();
    public static final Map<Item, UpgradeInfo>          ITEM_UPGRADES = new Reference2ObjectOpenHashMap<>();

    public static void clearAll() {
        RegistryUpgrade.UPGRADES.clear();
        RegistryUpgrade.ITEM_UPGRADES.clear();
    }

    @Nullable
    public static List<MachineUpgrade> getItemUpgradeList(ItemStack item) {
        UpgradeInfo upgradeInfo = ITEM_UPGRADES.get(item.getItem());
        if (upgradeInfo == null) {
            return null;
        }
        return upgradeInfo.getUpgrades();
    }

    public static boolean supportsUpgrade(ItemStack stack) {
        UpgradeInfo upgradeInfo = ITEM_UPGRADES.get(stack.getItem());
        if (upgradeInfo == null) {
            return false;
        }
        return upgradeInfo.matches(stack);
    }

    public static void addFixedUpgrade(ItemStack stack, MachineUpgrade upgrade) {
        ITEM_UPGRADES.computeIfAbsent(stack.getItem(), v -> new UpgradeInfo(Collections.singletonList(stack))).addUpgrade(upgrade);
    }

    public static void addSupportedItem(ItemStack stack) {
        ITEM_UPGRADES.computeIfAbsent(stack.getItem(), v -> new UpgradeInfo()).addMatch(stack);
    }

    public static void registerUpgrade(String type, MachineUpgrade upgrade) {
        UPGRADES.put(type, upgrade);
    }

    public static MachineUpgrade getUpgrade(String type) {
        return UPGRADES.get(type);
    }
}
