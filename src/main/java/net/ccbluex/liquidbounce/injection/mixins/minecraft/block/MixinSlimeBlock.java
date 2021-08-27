package net.ccbluex.liquidbounce.injection.mixins.minecraft.block;

import net.ccbluex.liquidbounce.features.module.modules.movement.ModuleNoSlow;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeBlock.class)
public class MixinSlimeBlock {

    @Inject(method = "onSteppedOn", at = @At("HEAD"), cancellable = true)
    private void hookStep(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (ModuleNoSlow.INSTANCE.getEnabled() && ModuleNoSlow.Slime.INSTANCE.getEnabled()) {
            ci.cancel();
        }
    }
}
