package github.kasuminova.mmce.common.world;

import com.github.bsideup.jabel.Desugar;
import com.mojang.realmsclient.util.Pair;
import github.kasuminova.mmce.common.tile.MEPatternMirrorImage;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import github.kasuminova.mmce.common.util.concurrent.ExecuteGroup;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MachineComponentManager {
    public static final MachineComponentManager                  INSTANCE     = new MachineComponentManager();
    private final       Map<World, Map<BlockPos, ComponentInfo>> componentMap = new ConcurrentHashMap<>();

    private MachineComponentManager() {
    }

    @Optional.Method(modid = "appliedenergistics2")
    private static Pair<BlockPos, TileEntity> getResult(TileEntity component, World world) {
        BlockPos pos = component.getPos();
        TileEntity te = component;

        if (component instanceof MEPatternMirrorImage mepi) {
            if (mepi.providerPos != null) {
                TileEntity tileEntity = world.getTileEntity(mepi.providerPos);
                if (tileEntity instanceof MEPatternProvider mep) {
                    te = mep;
                    pos = mep.getPos();
                }
            }
        }
        return Pair.of(pos, te);
    }

    public void addWorld(World world) {
        componentMap.put(world, new ConcurrentHashMap<>());
    }

    public void removeWorld(World world) {
        Map<BlockPos, ComponentInfo> removed = componentMap.remove(world);
        if (removed == null) {
            return;
        }
        for (final ComponentInfo info : removed.values()) {
            info.owners.clear();
        }
        removed.clear();
    }

    public void checkComponentShared(TileEntity component, TileMultiblockMachineController ctrl) {
        World world = component.getWorld();
        BlockPos pos;
        TileEntity te;

        if (Loader.isModLoaded("appliedenergistics2")) {
            Pair<BlockPos, TileEntity> result = getResult(component, world);
            pos = result.first();
            te = result.second();
        } else {
            te = component;
            pos = component.getPos();
        }

        Map<BlockPos, ComponentInfo> posComponentMap = componentMap.computeIfAbsent(world, v -> new ConcurrentHashMap<>());

        synchronized (te) {
            ComponentInfo info = posComponentMap.computeIfAbsent(pos, v -> new ComponentInfo(
                te, pos, ReferenceSets.synchronize(new ReferenceOpenHashSet<>(Collections.singleton(ctrl)))));

            if (!info.areTileEntityEquals(te)) {
                ComponentInfo newInfo = new ComponentInfo(te, pos, ReferenceSets.synchronize(new ReferenceOpenHashSet<>(Collections.singleton(ctrl))));
                posComponentMap.put(pos, newInfo);
                return;
            }

            Set<TileMultiblockMachineController> owners = info.owners;
            if (owners.contains(ctrl)) {
                return;
            }
            owners.add(ctrl);
            if (owners.size() <= 1) {
                return;
            }

            long groupId = owners.stream()
                                 .filter(owner -> owner.getExecuteGroupId() != -1)
                                 .findFirst()
                                 .map(TileMultiblockMachineController::getExecuteGroupId)
                                 .orElse(ExecuteGroup.newGroupId());

            owners.forEach(owner -> owner.setExecuteGroupId(groupId));
        }
    }

    public void removeOwner(TileEntity component, TileMultiblockMachineController ctrl) {
        World world = component.getWorld();
        BlockPos pos;
        TileEntity te;

        if (Loader.isModLoaded("appliedenergistics2")) {
            Pair<BlockPos, TileEntity> result = getResult(component, world);
            pos = result.first();
            te = result.second();
        } else {
            te = component;
            pos = component.getPos();
        }

        Map<BlockPos, ComponentInfo> posComponentMap = componentMap.computeIfAbsent(world, v -> new ConcurrentHashMap<>());

        ComponentInfo info = posComponentMap.get(pos);
        if (info == null) {
            return;
        }

        if (!info.areTileEntityEquals(te)) {
            ComponentInfo newInfo = new ComponentInfo(te, pos, new ObjectArraySet<>());
            posComponentMap.put(pos, newInfo);
        } else {
            Set<TileMultiblockMachineController> owners = info.owners;
            synchronized (owners) {
                owners.remove(ctrl);
            }
        }
    }

    @Desugar
    public record ComponentInfo(TileEntity te, BlockPos pos, Set<TileMultiblockMachineController> owners) {

        public boolean areTileEntityEquals(TileEntity te) {
            return this.te == te;
        }

        @Override
        public int hashCode() {
            return pos.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof ComponentInfo componentInfo) {
                return pos.equals(componentInfo.pos);
            }
            return false;
        }
    }
}
