package github.kasuminova.mmce.common.upgrade;

import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UpgradeType {
    private final Set<DynamicMachine> compatibleMachines   = new HashSet<>();
    private final Set<DynamicMachine> incompatibleMachines = new HashSet<>();
    private final String              name;
    private final String              localizedName;
    private final float               level;
    private final int                 maxStackSize;

    public UpgradeType(final String name, final String localizedName, final float level, final int maxStackSize) {
        this.name = name;
        this.localizedName = localizedName;
        this.level = level;
        this.maxStackSize = maxStackSize;
    }

    public Set<DynamicMachine> getCompatibleMachines() {
        return Collections.unmodifiableSet(compatibleMachines);
    }

    public void addCompatibleMachine(final DynamicMachine machine) {
        if (incompatibleMachines.contains(machine)) {
            throw new IllegalArgumentException("Already set this machine in the list of compatible machines, cannot add this machine to the list of incompatible machines!");
        }
        compatibleMachines.add(machine);
    }

    public void addIncompatibleMachine(final DynamicMachine machine) {
        if (compatibleMachines.contains(machine)) {
            throw new IllegalArgumentException("Already set this machine in the list of incompatible machines, cannot add this machine to the list of compatible machines!");
        }
        incompatibleMachines.add(machine);
    }

    public Set<DynamicMachine> getIncompatibleMachines() {
        return Collections.unmodifiableSet(incompatibleMachines);
    }

    public boolean isCompatible(final DynamicMachine machine) {
        if (!compatibleMachines.isEmpty()) {
            return compatibleMachines.contains(machine);
        }
        if (!incompatibleMachines.isEmpty()) {
            return !incompatibleMachines.contains(machine);
        }
        return true;
    }

    public String getName() {
        return name;
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        return I18n.hasKey(localizedName) ? I18n.format(localizedName) : localizedName;
    }

    public float getLevel() {
        return level;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof UpgradeType)) {
            return false;
        }
        return name.equals(((UpgradeType) obj).name);
    }
}
