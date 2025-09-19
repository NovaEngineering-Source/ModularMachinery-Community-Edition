package github.kasuminova.mmce.common.capability;

import github.kasuminova.mmce.common.upgrade.DynamicMachineUpgrade;
import github.kasuminova.mmce.common.upgrade.MachineUpgrade;
import github.kasuminova.mmce.common.upgrade.registry.RegistryUpgrade;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CapabilityUpgrade {
    public static final  ResourceLocation              CAPABILITY_NAME            = new ResourceLocation(ModularMachinery.MODID, "upgrade_cap");
    private static final NBTTagCompound                EMPTY_TAG_COMPOUND         = new NBTTagCompound();
    @SuppressWarnings("NonConstantFieldWithUpperCaseName")
    @CapabilityInject(CapabilityUpgrade.class)
    public static        Capability<CapabilityUpgrade> MACHINE_UPGRADE_CAPABILITY = null;
    private final        List<MachineUpgrade>          upgrades                   = new ArrayList<>();

    public static void register() {
        CapabilityManager.INSTANCE.register(CapabilityUpgrade.class, new Capability.IStorage<>() {
            @Nullable
            @Override
            public NBTBase writeNBT(final Capability<CapabilityUpgrade> capability, final CapabilityUpgrade instance, final EnumFacing side) {
                throw new UnsupportedOperationException("Deprecated");
            }

            @Override
            public void readNBT(final Capability<CapabilityUpgrade> capability, final CapabilityUpgrade instance, final EnumFacing side, final NBTBase nbt) {
                throw new UnsupportedOperationException("Deprecated");
            }
        }, CapabilityUpgrade::new);
    }

    public List<MachineUpgrade> getUpgrades() {
        return upgrades;
    }

    public void readNBT(NBTTagCompound tag) {
        upgrades.clear();
        for (final String upgradeType : tag.getKeySet()) {
            MachineUpgrade upgrade = RegistryUpgrade.getUpgrade(upgradeType);
            if (upgrade == null) {
                continue;
            }

            upgrade = upgrade.copy(ItemStack.EMPTY);
            upgrades.add(upgrade);
            if (upgrade instanceof DynamicMachineUpgrade) {
                NBTTagCompound upgradeTag = tag.getCompoundTag(upgradeType);
                ((DynamicMachineUpgrade) upgrade).readItemNBT(upgradeTag);
            }
        }
    }

    public NBTTagCompound writeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        for (final MachineUpgrade upgrade : upgrades) {
            if (upgrade instanceof DynamicMachineUpgrade) {
                tag.setTag(upgrade.getType().getName(), ((DynamicMachineUpgrade) upgrade).writeItemNBT());
            } else {
                tag.setTag(upgrade.getType().getName(), EMPTY_TAG_COMPOUND);
            }
        }

        return tag;
    }
}
