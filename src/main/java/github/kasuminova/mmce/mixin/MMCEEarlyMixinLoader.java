package github.kasuminova.mmce.mixin;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

@SuppressWarnings("unused")
public class MMCEEarlyMixinLoader implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(final Map<String, Object> data) {
        Mixins.addConfiguration("mixins.mmce_minecraft.json");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
