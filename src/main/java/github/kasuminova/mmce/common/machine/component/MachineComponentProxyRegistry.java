package github.kasuminova.mmce.common.machine.component;

import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MachineComponentProxyRegistry {
    public static final MachineComponentProxyRegistry         INSTANCE      = new MachineComponentProxyRegistry();
    private final       Map<String, MachineComponentProxy<?>> proxyRegistry = new HashMap<>();

    private MachineComponentProxyRegistry() {
    }

    public Map<String, MachineComponentProxy<?>> getRegistry() {
        return Collections.unmodifiableMap(proxyRegistry);
    }

    public <T extends MachineComponent<?>> MachineComponentProxy<T> register(String name, MachineComponentProxy<T> proxy) {
        if (proxyRegistry.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate MachineComponentProxy proxy key: " + name);
        }
        proxyRegistry.put(name, proxy);
        return proxy;
    }

    public void unregister(String name) {
        proxyRegistry.remove(name);
    }

    @Nullable
    public MachineComponent<?> proxy(TileEntity te) {
        for (final MachineComponentProxy<?> proxy : proxyRegistry.values()) {
            if (proxy.isSupported(te)) {
                return proxy.proxyComponent(te);
            }
        }
        return null;
    }
}
