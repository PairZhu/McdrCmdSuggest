package com.pairzhu.mcdrcmdsuggest;

import net.fabricmc.api.ModInitializer;
// @formatter:off
//#if MC >= 11900
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//#else
//#if MC >= 11600
//$$ import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
//#else
//$$ import net.fabricmc.fabric.api.registry.CommandRegistry;
//#endif
//#endif
// @formatter:on
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.arguments.StringArgumentType;

public class McdrCmdSuggest implements ModInitializer {
	public static final String MOD_ID = "mcdrcmdsuggest";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// @formatter:off
		//#if MC >= 11600
		CommandRegistrationCallback.EVENT
		//#else
		//$$ CommandRegistry.INSTANCE
		//#endif
				//#if MC >= 11900
				.register((dispatcher, registryAccess, environment) -> dispatcher
				//#else
				//#if MC >= 11600
				//$$ .register((dispatcher, dedicated) -> dispatcher
				//#else
				//$$ .register(true, dispatcher -> dispatcher
				//#endif
				//#endif
		// @formatter:on
						.register(CommandManager
								.literal("__mcdrcmdsuggest_register")
								.requires(source -> source.getEntity() == null)
								.then(CommandManager
										.argument("json_data", StringArgumentType.greedyString())
										.executes(new RegisterCommandHandler()))));
	}
}

