package kport.gugu_utils.common.embers;

import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum EmbersHatchVariant implements IStringSerializable {
    TINY,
    SMALL,
    MEDIUM,
    REINFORCED,
    BIG,
    HUGE,
    LUDICROUS,
    EXTREME;

    public static final EmbersHatchVariant[] VAULES = EmbersHatchVariant.values();

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public int getEmberMaxStorage() {
        switch (this) {
            case TINY:
            default:
                return 400;
            case SMALL:
                return 800;
            case MEDIUM:
                return 1600;
            case REINFORCED:
                return 3200;
            case BIG:
                return 6400;
            case HUGE:
                return 9600;
            case LUDICROUS:
                return 12800;
            case EXTREME:
                return 16000;
        }
    }
}
