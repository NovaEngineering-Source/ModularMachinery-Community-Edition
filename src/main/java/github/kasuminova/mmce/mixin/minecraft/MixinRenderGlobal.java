package github.kasuminova.mmce.mixin.minecraft;

import github.kasuminova.mmce.client.renderer.ControllerModelRenderManager;
import hellfirepvp.modularmachinery.common.base.Mods;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Inject(method = "renderEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;drawBatch(I)V",
                    shift = At.Shift.AFTER,
                    remap = false
            )
    )
    private void hookTESRComplete(final Entity renderViewEntity, final ICamera camera, final float partialTicks, final CallbackInfo ci) {
        if (Mods.GECKOLIB.isPresent()) {
            ControllerModelRenderManager.INSTANCE.draw();
        }
    }

}
