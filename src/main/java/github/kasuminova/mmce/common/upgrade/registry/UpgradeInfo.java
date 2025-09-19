package github.kasuminova.mmce.common.upgrade.registry;

import github.kasuminova.mmce.common.upgrade.MachineUpgrade;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UpgradeInfo {
    private final List<ItemStack>      matches  = new ArrayList<>();
    private final List<MachineUpgrade> upgrades = new ArrayList<>();

    public UpgradeInfo(List<ItemStack> matches) {
        this.matches.addAll(matches);
    }

    public UpgradeInfo() {

    }

    public boolean matches(ItemStack stack) {
        for (final ItemStack match : matches) {
            if (ItemStack.areItemsEqual(match, stack)) {
                return true;
            }
        }

        return false;
    }

    public UpgradeInfo addMatch(ItemStack match) {
        matches.add(match);
        return this;
    }

    public UpgradeInfo addUpgrade(MachineUpgrade upgrade) {
        upgrades.add(upgrade);
        return this;
    }

    public List<ItemStack> getMatches() {
        return matches;
    }

    public List<MachineUpgrade> getUpgrades() {
        return upgrades;
    }
}
