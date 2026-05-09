package com.pairzhu.mcdrcmdsuggest;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class McdrCmdSuggest implements ModInitializer {
    public static final String MOD_ID = "mcdrcmdsuggest";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
                .register(CommandManager
                        .literal("__mcdrcmdsuggest_register")
                        .requires(source -> source.getEntity() == null)
                        .then(CommandManager
                                .argument("json_data", StringArgumentType.greedyString())
                                .executes(new RegisterCommandHandler()))));
        LOGGER.info("$$McdrCmdSuggest initialized$$");
    }
}
