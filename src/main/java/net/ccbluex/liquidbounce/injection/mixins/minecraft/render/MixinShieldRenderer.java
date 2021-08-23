package net.ccbluex.liquidbounce.injection.mixins.minecraft.render;

import net.ccbluex.liquidbounce.features.module.modules.world.ModuleLiquidInteract;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShieldEntityModel.class)
public class MixinShieldRenderer {

    @Inject(method = "getTexturedModelData", at = @At("RETURN"), cancellable = true)
    private static void hookTexturedModelData(CallbackInfoReturnable<TexturedModelData> cir) {
        if (ModuleLiquidInteract.INSTANCE.getEnabled()) {
            cir.setReturnValue(TexturedModelData.of(new ModelData(), ModuleLiquidInteract.INSTANCE.getB(), ModuleLiquidInteract.INSTANCE.getC()));
        }
    }
}
