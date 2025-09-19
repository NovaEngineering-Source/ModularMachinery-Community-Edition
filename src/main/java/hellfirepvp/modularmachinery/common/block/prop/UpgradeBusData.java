package hellfirepvp.modularmachinery.common.block.prop;

import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;

public enum UpgradeBusData implements IStringSerializable {
    NORMAL(3),
    REINFORCED(6),
    ELITE(9),
    SUPER(12),
    ULTIMATE(18),
    ;

    private final int defaultMaxUpgradeSlot;
    private       int maxUpgradeSlot;

    UpgradeBusData(int defaultMaxUpgradeSlot) {
        this.defaultMaxUpgradeSlot = defaultMaxUpgradeSlot;
    }

    public static void loadFromConfig(Configuration cfg) {
        for (UpgradeBusData data : values()) {
            data.maxUpgradeSlot = cfg.getInt("max-upgrade_slot", "upgrade-bus." + data.getName(),
                data.defaultMaxUpgradeSlot, 1, 18,
                "Defines the max upgrade slot for the upgrade bus.");
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return name().toLowerCase();
    }

    public int getMaxUpgradeSlot() {
        return maxUpgradeSlot;
    }
}
