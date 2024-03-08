package kport.gugu_utils.common.hotair;

import net.minecraft.util.IStringSerializable;

public enum HotAirHatchState implements IStringSerializable {
    OFF("off"),
    ON("on");

    // Optimization
    public static final HotAirHatchState[] VALUES = HotAirHatchState.values();

    private final String name;

    HotAirHatchState(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
