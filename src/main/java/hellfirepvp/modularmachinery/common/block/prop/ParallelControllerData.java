package hellfirepvp.modularmachinery.common.block.prop;

import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;

public enum ParallelControllerData implements IStringSerializable {
    NORMAL(4),
    REINFORCED(16),
    ELITE(64),
    SUPER(256),
    ULTIMATE(512),
    ;

    private final int defaultMaxParallelism;
    private       int maxParallelism;

    ParallelControllerData(int defaultMaxParallelism) {
        this.defaultMaxParallelism = defaultMaxParallelism;
    }

    public static void loadFromConfig(Configuration cfg) {
        for (ParallelControllerData data : values()) {
            data.maxParallelism = cfg.getInt("max-parallelism", "parallel-controller." + data.getName(),
                data.defaultMaxParallelism, 1, Integer.MAX_VALUE,
                "Defines the max parallelism for the parallel controller.");
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return name().toLowerCase();
    }

    public int getMaxParallelism() {
        return maxParallelism;
    }
}
