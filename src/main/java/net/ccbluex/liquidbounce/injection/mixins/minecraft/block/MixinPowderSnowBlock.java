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

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    private void hookEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (ModuleNoSlow.INSTANCE.getEnabled() && ModuleNoSlow.PowderSnow.INSTANCE.getEnabled()) {
            entity.slowMovement(state, new Vec3d(0.0, 0.0, 0.0));
            if (ModuleNoSlow.PowderSnow.INSTANCE.getA()) entity.setVelocity(ModuleNoSlow.PowderSnow.INSTANCE.getMultiplier(), entity.getVelocity().y, ModuleNoSlow.PowderSnow.INSTANCE.getMultiplier());
        }
    }
}
