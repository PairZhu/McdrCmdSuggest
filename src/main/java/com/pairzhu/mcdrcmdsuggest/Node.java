package com.pairzhu.mcdrcmdsuggest;

import java.util.ArrayList;
import com.alibaba.fastjson2.JSONObject;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public class Node {
    public final String name;
    private final String type;
    private final boolean suggestible;
    private final ArrayList<Node> children = new ArrayList<>();
    private final SuggestImplement suggestImplement;

    public Node(JSONObject json, SuggestImplement suggestImplement) {
        this.name = json.getString("name");
        this.type = json.getString("type");
        this.suggestible = json.getBooleanValue("suggestible");
        this.suggestImplement = suggestImplement;
        if (json.containsKey("children")) {
            for (JSONObject childJson : json.getJSONArray("children").toArray(JSONObject.class)) {
                this.children.add(new Node(childJson, suggestImplement));
            }
        }
    }

    public ArgumentBuilder<ServerCommandSource, ?> toBrigadierNode() {
        ArgumentBuilder<ServerCommandSource, ?> argumentBuilder = createArgumentBuilder();

        if (needsSuggestion()) {
            if (suggestImplement == null) {
                throw new IllegalStateException(
                        "suggestImplement cannot be null when suggestions are needed");
            }
            @SuppressWarnings("unchecked")
            RequiredArgumentBuilder<ServerCommandSource, ?> requiredArgBuilder =
                    (RequiredArgumentBuilder<ServerCommandSource, ?>) argumentBuilder;
            requiredArgBuilder.suggests((context, builder) -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player == null) {
                    return CommandSource.suggestMatching(new String[0], builder);
                }
                String command = builder.getInput().substring(1);
                return CommandSource
                        .suggestMatching(suggestImplement.getSuggestions(player, command), builder);
            });
            argumentBuilder = requiredArgBuilder;
        }

        argumentBuilder.executes(context -> {
            ServerCommandSource source = context.getSource();
            String command = context.getInput().substring(1);
            String name = source != null ? source.getName() : "unknown";
            McdrCmdSuggest.LOGGER.info("<{}> {}", name, command);
            return Command.SINGLE_SUCCESS;
        });

        if (children != null) {
            for (Node child : children) {
                argumentBuilder.then(child.toBrigadierNode());
            }
        }
        return argumentBuilder;
    }

    private ArgumentBuilder<ServerCommandSource, ?> createArgumentBuilder() {
        switch (type) {
            case "LITERAL":
                return CommandManager.literal(name);
            case "INTEGER":
                return CommandManager.argument(name, IntegerArgumentType.integer());
            case "FLOAT":
                return CommandManager.argument(name, DoubleArgumentType.doubleArg());
            case "QUOTABLE_TEXT":
                return CommandManager.argument(name, StringArgumentType.string());
            case "GREEDY_TEXT":
                return CommandManager.argument(name, StringArgumentType.greedyString());
            default:
                // NUMBER, TEXT, BOOLEAN, ENUMERATION, etc...
                return CommandManager.argument(name, StringArgumentType.word());
        }
    }

    private boolean needsSuggestion() {
        return !"LITERAL".equals(type) && suggestible && suggestImplement != null;
    }
}
