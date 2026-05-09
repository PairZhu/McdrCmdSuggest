package com.pairzhu.mcdrcmdsuggest;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.pairzhu.mcdrcmdsuggest.core.CommandTreeNode;
import com.pairzhu.mcdrcmdsuggest.core.SuggestProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

public class Node {
    private final CommandTreeNode node;
    private final SuggestProvider suggestProvider;

    public Node(CommandTreeNode node, SuggestProvider suggestProvider) {
        this.node = node;
        this.suggestProvider = suggestProvider;
    }

    public ArgumentBuilder<CommandSourceStack, ?> toBrigadierNode() {
        ArgumentBuilder<CommandSourceStack, ?> argumentBuilder = createArgumentBuilder();

        if (node.needsSuggestion()) {
            @SuppressWarnings("unchecked")
            RequiredArgumentBuilder<CommandSourceStack, ?> requiredArgBuilder =
                    (RequiredArgumentBuilder<CommandSourceStack, ?>) argumentBuilder;
            requiredArgBuilder.suggests((context, builder) -> {
                ServerPlayer player = context.getSource().getPlayer();
                if (player == null) {
                    return SharedSuggestionProvider.suggest(new String[0], builder);
                }
                String input = builder.getInput();
                String command = input.startsWith("/") ? input.substring(1) : input;
                String[] suggestions = suggestProvider.getSuggestions(player.getName().getString(), command);
                return SharedSuggestionProvider.suggest(suggestions, builder);
            });
            argumentBuilder = requiredArgBuilder;
        }

        argumentBuilder.executes(context -> {
            CommandSourceStack source = context.getSource();
            String input = context.getInput();
            String command = input.startsWith("/") ? input.substring(1) : input;
            String name = source != null ? source.getTextName() : "unknown";
            McdrCmdSuggest.LOGGER.info("<{}> {}", name, command);
            return Command.SINGLE_SUCCESS;
        });

        for (CommandTreeNode child : node.children) {
            argumentBuilder.then(new Node(child, suggestProvider).toBrigadierNode());
        }
        return argumentBuilder;
    }

    private ArgumentBuilder<CommandSourceStack, ?> createArgumentBuilder() {
        switch (node.type) {
            case "LITERAL":
                return Commands.literal(node.name);
            case "INTEGER":
                return Commands.argument(node.name, IntegerArgumentType.integer());
            case "FLOAT":
                return Commands.argument(node.name, DoubleArgumentType.doubleArg());
            case "QUOTABLE_TEXT":
                return Commands.argument(node.name, StringArgumentType.string());
            case "GREEDY_TEXT":
                return Commands.argument(node.name, StringArgumentType.greedyString());
            default:
                return Commands.argument(node.name, StringArgumentType.word());
        }
    }
}
