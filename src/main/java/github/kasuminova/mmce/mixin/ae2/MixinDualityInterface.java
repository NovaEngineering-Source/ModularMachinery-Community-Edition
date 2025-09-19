package github.kasuminova.mmce.mixin.ae2;

import appeng.api.config.Upgrades;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngInternalInventory;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DualityInterface.class, remap = false)
public class MixinDualityInterface {

    @Shadow
    @Final
    private IInterfaceHost iHost;

    @Shadow
    @Final
    @Mutable
    private AppEngInternalInventory patterns;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(AENetworkProxy networkProxy, IInterfaceHost ih, CallbackInfo ci) {
        if (ih instanceof MEPatternProvider mep) {
            this.patterns = mep.getPatterns();
        }
    }

    @Inject(method = "getInstalledUpgrades", at = @At("RETURN"), cancellable = true)
    public void getInstalledUpgradesMixin(Upgrades u, CallbackInfoReturnable<Integer> cir) {
        if (iHost instanceof MEPatternProvider) {
            cir.setReturnValue(3);
        }
    }

    @Inject(method = "getTermName", at = @At(value = "INVOKE", target = "Lappeng/helpers/IInterfaceHost;getTargets()Ljava/util/EnumSet;"), cancellable = true)
    public void getTermNameMixin(CallbackInfoReturnable<String> cir) {
        TileEntity hostTile = this.iHost.getTileEntity();
        if (hostTile instanceof MEPatternProvider mep) {
            cir.setReturnValue(mep.getMachineName());
        }
    }
}
