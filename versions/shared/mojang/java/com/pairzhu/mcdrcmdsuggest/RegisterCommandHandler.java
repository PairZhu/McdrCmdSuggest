package com.pairzhu.mcdrcmdsuggest;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.pairzhu.mcdrcmdsuggest.core.CommandParser;
import com.pairzhu.mcdrcmdsuggest.core.CommandTreeNode;
import com.pairzhu.mcdrcmdsuggest.core.ParsedCommandData;
import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;

public class RegisterCommandHandler implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MinecraftServer minecraftServer = context.getSource().getServer();
        CommandDispatcher<CommandSourceStack> dispatcher =
                Objects.requireNonNull(minecraftServer).getCommands().getDispatcher();

        try {
            ParsedCommandData commandData =
                    CommandParser.parse(StringArgumentType.getString(context, "json_data"));
            McdrCmdSuggest.LOGGER.info("Processing commands in mode: {} with port: {}",
                    commandData.mode, commandData.port);

            for (CommandTreeNode node : commandData.nodes) {
                @SuppressWarnings("unchecked")
                LiteralArgumentBuilder<CommandSourceStack> brigadierNode =
                        (LiteralArgumentBuilder<CommandSourceStack>) new Node(node, commandData.suggestProvider)
                                .toBrigadierNode();
                dispatcher.register(brigadierNode);
            }

            PlayerList manager = minecraftServer.getPlayerList();
            manager.getPlayers().forEach(manager::sendPlayerPermissionLevel);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            McdrCmdSuggest.LOGGER.error("Command operation failed: {}", e);
            throw new SimpleCommandExceptionType(() -> e.getMessage()).create();
        }
    }
}
