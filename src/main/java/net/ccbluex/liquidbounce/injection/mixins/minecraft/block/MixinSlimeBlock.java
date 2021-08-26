package net.ccbluex.liquidbounce.injection.mixins.minecraft.block;

import net.ccbluex.liquidbounce.features.module.modules.movement.ModuleNoSlow;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeBlock.class)
public class MixinSlimeBlock {

    @Inject(method = "bounce", at = @At("RETURN"), cancellable = true)
    private void hookBounce(Entity entity, CallbackInfo ci) {
        if (ModuleNoSlow.INSTANCE.getEnabled() && ModuleNoSlow.Slime.INSTANCE.getEnabled()) {
            ci.cancel();
        }
    }
}
