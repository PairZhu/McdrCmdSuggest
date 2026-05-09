package com.pairzhu.mcdrcmdsuggest;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.pairzhu.mcdrcmdsuggest.core.CommandTreeNode;
import com.pairzhu.mcdrcmdsuggest.core.SuggestProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class Node {
    private final CommandTreeNode node;
    private final SuggestProvider suggestProvider;

    public Node(CommandTreeNode node, SuggestProvider suggestProvider) {
        this.node = node;
        this.suggestProvider = suggestProvider;
    }

    public ArgumentBuilder<ServerCommandSource, ?> toBrigadierNode() {
        ArgumentBuilder<ServerCommandSource, ?> argumentBuilder = createArgumentBuilder();

        if (node.needsSuggestion()) {
            @SuppressWarnings("unchecked")
            RequiredArgumentBuilder<ServerCommandSource, ?> requiredArgBuilder =
                    (RequiredArgumentBuilder<ServerCommandSource, ?>) argumentBuilder;
            requiredArgBuilder.suggests((context, builder) -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player == null) {
                    return CommandSource.suggestMatching(new String[0], builder);
                }
                String input = builder.getInput();
                String command = input.startsWith("/") ? input.substring(1) : input;
                String[] suggestions = suggestProvider.getSuggestions(player.getName().getString(), command);
                return CommandSource.suggestMatching(suggestions, builder);
            });
            argumentBuilder = requiredArgBuilder;
        }

        argumentBuilder.executes(context -> {
            ServerCommandSource source = context.getSource();
            String input = context.getInput();
            String command = input.startsWith("/") ? input.substring(1) : input;
            String name = source != null ? source.getName() : "unknown";
            McdrCmdSuggest.LOGGER.info("<{}> {}", name, command);
            return Command.SINGLE_SUCCESS;
        });

        for (CommandTreeNode child : node.children) {
            argumentBuilder.then(new Node(child, suggestProvider).toBrigadierNode());
        }
        return argumentBuilder;
    }

    private ArgumentBuilder<ServerCommandSource, ?> createArgumentBuilder() {
        switch (node.type) {
            case "LITERAL":
                return CommandManager.literal(node.name);
            case "INTEGER":
                return CommandManager.argument(node.name, IntegerArgumentType.integer());
            case "FLOAT":
            case "NUMBER":
                return CommandManager.argument(node.name, DoubleArgumentType.doubleArg());
            case "BOOLEAN":
                return CommandManager.argument(node.name, BoolArgumentType.bool());
            case "TEXT":
            case "ENUMERATION":
                return CommandManager.argument(node.name, StringArgumentType.word());
            case "QUOTABLE_TEXT":
                return CommandManager.argument(node.name, StringArgumentType.string());
            case "GREEDY_TEXT":
                return CommandManager.argument(node.name, StringArgumentType.greedyString());
            default:
                return CommandManager.argument(node.name, StringArgumentType.word());
        }
    }
}
