package net.ccbluex.liquidbounce.injection.mixins.minecraft.gui;

import net.ccbluex.liquidbounce.features.module.modules.player.ModuleChestStealer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GenericContainerScreenHandler.class)
public class MixinGenericContainerScreenHandler {
    /**
     * @author mems01
     */
    @Overwrite
    public void close(PlayerEntity player) {
        if (ModuleChestStealer.INSTANCE.getEnabled()) {
            MinecraftClient.getInstance().currentScreen = null;
        }
    }
}
