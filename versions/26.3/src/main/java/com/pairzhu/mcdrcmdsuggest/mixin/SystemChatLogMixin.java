package com.pairzhu.mcdrcmdsuggest.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Preserve the console message format consumed by existing MCDR plugins. */
@Mixin(MinecraftServer.class)
abstract class SystemChatLogMixin {
    @ModifyConstant(method = "sendSystemMessage", constant = @Constant(stringValue = "System chat: {}"), require = 1)
    private String mcdrcmdsuggest$keepCompatibleConsoleFormat(String format) {
        return "{}";
    }
}
