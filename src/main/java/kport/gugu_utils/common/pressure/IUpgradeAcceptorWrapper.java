package kport.gugu_utils.common.pressure;

import kport.gugu_utils.common.tile.TilePressureHatch;
import static kport.gugu_utils.tools.ResourceUtils.j;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import net.minecraft.item.Item;

import java.util.Set;

public class IUpgradeAcceptorWrapper implements IUpgradeAcceptor {
    private final TilePressureHatch hatch;

    public IUpgradeAcceptorWrapper(TilePressureHatch hatch) {
        this.hatch = hatch;
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        return hatch.getApplicableUpgrades();
    }

    @Override
    public String getName() {
        return j(hatch.getName(), "name");
    }
}
