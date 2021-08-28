package net.ccbluex.liquidbounce.injection.mixins.minecraft.gui;

import net.ccbluex.liquidbounce.features.module.modules.player.ModuleChestStealer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericContainerScreen.class)
public class MixinGenericContainerScreen {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void hookRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (ModuleChestStealer.INSTANCE.getEnabled() && mc.currentScreen instanceof GenericContainerScreen) {
            mc.windowFocused = true;
            mc.mouse.lockCursor();
            ci.cancel();
        }
    }

}
