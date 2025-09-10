package com.pairzhu.mcdrcmdsuggest;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 处理Minecraft命令的注册和注销的处理器类
 */
public class RegisterCommandHandler implements Command<ServerCommandSource>, AutoCloseable {

    // 使用HashSet提高查找效率
    private final Set<String> registeredCommands = new HashSet<>();

    /**
     * 命令数据类，用于JSON序列化和反序列化
     */
    public enum Mode {
        @JSONField(name = "stdio")
        STDIO, @JSONField(name = "http")
        HTTP
    }

    public static class CommandData {
        @JSONField(name = "mode", required = true)
        public final Mode mode;

        @JSONField(name = "host")
        public final String host;

        @JSONField(name = "port")
        public final int port;

        @JSONField(name = "nodes")
        public final List<JSONObject> commandNodes;

        public CommandData(Mode mode, String host, int port, List<JSONObject> commandNodes) {
            this.mode = mode;
            this.host = host == null ? "localhost" : host;
            this.port = port;
            this.commandNodes = commandNodes;
        }
    }

    private SuggestImplement suggestImplement;


    private static class CommandFields {
        private static final Field rootCommandNode;
        private static final Field commandNodeChildren;
        private static final Field commandNodeLiterals;

        static {
            try {
                rootCommandNode = CommandDispatcher.class.getDeclaredField("root");
                commandNodeChildren = CommandNode.class.getDeclaredField("children");
                commandNodeLiterals = CommandNode.class.getDeclaredField("literals");

                rootCommandNode.setAccessible(true);
                commandNodeChildren.setAccessible(true);
                commandNodeLiterals.setAccessible(true);
            } catch (NoSuchFieldException e) {
                String message = "Failed to initialize reflection fields for command handling";
                McdrCmdSuggest.LOGGER.error(message + "{}", e);
                throw new CommandInitializationException(message, e);
            }
        }
    }


    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer minecraftServer = context.getSource().getServer();
        CommandDispatcher<ServerCommandSource> commandDispatcher =
                Objects.requireNonNull(minecraftServer).getCommandManager().getDispatcher();

        try {
            unregisterExistingCommands(commandDispatcher);

            registerNewCommands(context, commandDispatcher);

            updatePlayerCommandTrees(minecraftServer);

            return Command.SINGLE_SUCCESS;
        } catch (CommandOperationException e) {
            McdrCmdSuggest.LOGGER.error("Command operation failed: {}", e);
            throw new SimpleCommandExceptionType(() -> e.getMessage()).create();
        }
    }


    private void unregisterExistingCommands(CommandDispatcher<ServerCommandSource> dispatcher)
            throws CommandOperationException {
        if (registeredCommands.isEmpty()) {
            return;
        }

        try {
            var rootNode = CommandFields.rootCommandNode.get(dispatcher);
            @SuppressWarnings("unchecked")
            var children =
                    (Map<String, CommandNode<ServerCommandSource>>) CommandFields.commandNodeChildren
                            .get(rootNode);
            @SuppressWarnings("unchecked")
            var literals =
                    (Map<String, LiteralCommandNode<ServerCommandSource>>) CommandFields.commandNodeLiterals
                            .get(rootNode);

            for (String commandName : registeredCommands) {
                children.remove(commandName);
                literals.remove(commandName);
                McdrCmdSuggest.LOGGER.debug("Unregistered command: {}", commandName);
            }

            registeredCommands.clear();
        } catch (IllegalAccessException e) {
            throw new CommandOperationException("Failed to unregister commands", e);
        }
    }

    private void registerNewCommands(CommandContext<ServerCommandSource> context,
            CommandDispatcher<ServerCommandSource> dispatcher) throws CommandOperationException {
        try {
            String jsonData = StringArgumentType.getString(context, "json_data");
            CommandData commandData = parseCommandData(jsonData);

            McdrCmdSuggest.LOGGER.info("Processing commands in mode: {} with port: {}",
                    commandData.mode, commandData.port);

            for (JSONObject nodeJson : commandData.commandNodes) {
                registerSingleCommand(nodeJson, dispatcher);
            }
        } catch (Exception e) {
            throw new CommandOperationException("Failed to register commands", e);
        }
    }

    private CommandData parseCommandData(String jsonData) throws CommandValidationException {
        CommandData commandData = JSON.parseObject(jsonData, CommandData.class);
        if (commandData == null || commandData.commandNodes == null) {
            throw new CommandValidationException(
                    "Invalid command data format: missing or empty data");
        }
        switch (commandData.mode) {
            case STDIO:
                suggestImplement = new StdioSuggestImplement();
                break;
            case HTTP:
                if (commandData.port < 1 || commandData.port > 65535) {
                    throw new CommandValidationException(
                            "Invalid port number: must be between 1 and 65535");
                }
                suggestImplement = new HttpSuggestImplement(commandData.host, commandData.port);
                break;
            default:
                throw new CommandValidationException("Unsupported mode: " + commandData.mode);
        }
        return commandData;
    }

    private void registerSingleCommand(JSONObject nodeJson,
            CommandDispatcher<ServerCommandSource> dispatcher) throws CommandOperationException {
        try {
            Node node = new Node(nodeJson, suggestImplement);
            @SuppressWarnings("unchecked")
            var brigadierNode =
                    (LiteralArgumentBuilder<ServerCommandSource>) node.toBrigadierNode();
            dispatcher.register(brigadierNode);
            registeredCommands.add(node.name);
            McdrCmdSuggest.LOGGER.debug("Registered command: {}", node.name);
        } catch (Exception e) {
            throw new CommandOperationException(
                    String.format("Failed to register command from node: %s", nodeJson), e);
        }
    }

    private void updatePlayerCommandTrees(MinecraftServer server) throws CommandOperationException {
        try {
            server.getPlayerManager().getPlayerList().forEach(player -> Objects
                    .requireNonNull(player.getServer()).getPlayerManager().sendCommandTree(player));
        } catch (Exception e) {
            throw new CommandOperationException("Failed to update command tree for players", e);
        }
    }

    @Override
    public void close() {
        registeredCommands.clear();
    }
}


class CommandInitializationException extends RuntimeException {
    public CommandInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}


class CommandOperationException extends Exception {
    public CommandOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}


class CommandValidationException extends Exception {
    public CommandValidationException(String message) {
        super(message);
    }
}
