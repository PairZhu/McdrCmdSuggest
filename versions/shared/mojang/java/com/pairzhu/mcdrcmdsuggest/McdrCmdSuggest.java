package com.pairzhu.mcdrcmdsuggest;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McdrCmdSuggest implements ModInitializer {
    public static final String MOD_ID = "mcdrcmdsuggest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
                .register(Commands
                        .literal("__mcdrcmdsuggest_register")
                        .requires(source -> source.getEntity() == null)
                        .then(Commands
                                .argument("json_data", StringArgumentType.greedyString())
                                .executes(new RegisterCommandHandler()))));
        LOGGER.info("$$McdrCmdSuggest initialized$$");
    }
}
