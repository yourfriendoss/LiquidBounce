package net.ccbluex.liquidbounce.injection.mixins.minecraft.block;

import net.ccbluex.liquidbounce.features.module.modules.movement.ModuleNoSlow;
import net.minecraft.block.BlockState;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PowderSnowBlock.class)
public class MixinPowderSnowBlock {

    @Inject(method = "onEntityCollision", at = @At("RETURN"), cancellable = true)
    private void hookEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (ModuleNoSlow.INSTANCE.getEnabled() && ModuleNoSlow.PowderSnow.INSTANCE.getEnabled()) {
            ci.cancel();
            entity.setVelocity(new Vec3d(ModuleNoSlow.PowderSnow.INSTANCE.getMultiplier(), 1.5D, ModuleNoSlow.PowderSnow.INSTANCE.getMultiplier()));
        }
    }
}
