package github.kasuminova.mmce.common.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IRegistryDelegate;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OredictCache {
    private static final Map<Integer, Map<Integer, int[]>> ORE_ID_CACHE_MAP = new ConcurrentHashMap<>();

    public static int[] getOreIDsFast(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return new int[0];
        }

        IRegistryDelegate<Item> delegate = stack.getItem().delegate;
        if (delegate.name() == null) {
            FMLLog.log.debug("Attempted to find the oreIDs for an unregistered object ({}). This won't work very well.", stack);
            return new int[0];
        }

        int id = Item.REGISTRY.getIDForObject(delegate.get());
        int damageOffset = id | ((stack.getItemDamage() + 1) << 16);

        Map<Integer, int[]> map = ORE_ID_CACHE_MAP.get(id);
        if (map == null) {
            synchronized (ORE_ID_CACHE_MAP) {
                map = ORE_ID_CACHE_MAP.computeIfAbsent(id, k -> new ConcurrentHashMap<>());
            }
        }

        int[] oreIDs = map.get(damageOffset);
        if (oreIDs == null) {
            synchronized (map) {
                oreIDs = map.get(damageOffset);
                if (oreIDs == null) {
                    map.put(id, oreIDs = OreDictionary.getOreIDs(stack));
                }
            }
        }

        return oreIDs;
    }
}
