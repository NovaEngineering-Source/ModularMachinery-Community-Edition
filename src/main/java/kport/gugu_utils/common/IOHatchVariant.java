package kport.gugu_utils.common;

import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum IOHatchVariant implements IStringSerializable {
    INPUT,
    OUTPUT,
    ;
    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
