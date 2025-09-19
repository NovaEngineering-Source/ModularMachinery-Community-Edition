package github.kasuminova.mmce.common.machine.pattern;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SpecialItemBlockProxyRegistry {
    public static final SpecialItemBlockProxyRegistry      INSTANCE      = new SpecialItemBlockProxyRegistry();
    private final       Map<String, SpecialItemBlockProxy> proxyRegistry = new HashMap<>();

    private SpecialItemBlockProxyRegistry() {
    }

    public Map<String, SpecialItemBlockProxy> getRegistry() {
        return Collections.unmodifiableMap(proxyRegistry);
    }

    public SpecialItemBlockProxy register(String name, SpecialItemBlockProxy proxy) {
        if (proxyRegistry.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate SpecialTileEntityProxy proxy key: " + name);
        }
        proxyRegistry.put(name, proxy);
        return proxy;
    }

    public void unregister(String name) {
        proxyRegistry.remove(name);
    }

    @Nullable
    public SpecialItemBlockProxy getValidProxy(ItemStack te) {
        for (final SpecialItemBlockProxy proxy : proxyRegistry.values()) {
            if (proxy.isValid(te)) {
                return proxy;
            }
        }
        return null;
    }
}
