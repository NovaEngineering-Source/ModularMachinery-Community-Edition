package github.kasuminova.mmce.mixin.minecraft;

import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"MethodMayBeStatic", "StaticVariableMayNotBeInitialized"})
@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {

    @Shadow
    public static TileEntityRendererDispatcher instance;

    /**
     * Prevents MBD from hiding the controller's TESR.
     */
    @Inject(
        method = "getRenderer(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;",
        at = @At("HEAD"),
        cancellable = true
    )
    private <T extends TileEntity> void injectGetRenderer(TileEntity te, CallbackInfoReturnable<TileEntitySpecialRenderer<T>> cir) {
        if (te instanceof TileMultiblockMachineController) {
            cir.setReturnValue(instance.getRenderer(te.getClass()));
        }
    }

}
