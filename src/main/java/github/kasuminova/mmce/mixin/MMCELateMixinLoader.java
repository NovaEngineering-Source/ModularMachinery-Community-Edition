package github.kasuminova.mmce.mixin;

import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class MMCELateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList(
            "mixins.mmce_jei_hacky.json",
            "mixins.mmce_ae2.json",
            "mixins.mmce_nae2.json"
        );
    }

    @Override
    public boolean shouldMixinConfigQueue(final String mixinConfig) {
        return switch (mixinConfig) {
            case "mixins.mmce_jei_hacky.json" -> Loader.isModLoaded("jei");
            case "mixins.mmce_ae2.json" -> Loader.isModLoaded("appliedenergistics2");
            case "mixins.mmce_nae2.json" -> Loader.isModLoaded("nae2");
            default -> true;
        };
    }
}
