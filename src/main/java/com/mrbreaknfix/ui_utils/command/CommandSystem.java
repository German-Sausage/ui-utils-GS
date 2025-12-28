/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file CommandSystem.java
 */
package com.mrbreaknfix.ui_utils.command;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blocks.*;
import com.mrbreaknfix.ui_utils.command.block.blocks.events.OnStartBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blocks.events.ScreenEventBlockProvider;
import com.mrbreaknfix.ui_utils.command.commands.*;
import com.mrbreaknfix.ui_utils.pIIC.PIICClient;
import com.mrbreaknfix.ui_utils.pIIC.PIICLauncher;

import static com.mrbreaknfix.ui_utils.UiUtils.webSocketCommandServer;

public class CommandSystem {
    public static CommandSystem commandSystem;
    public static final Map<String, Command> commands = new TreeMap<>();
    public static final Map<String, IBlockProvider> blockProviders = new TreeMap<>();

    private static final ScheduledExecutorService piicReconnectScheduler =
            Executors.newSingleThreadScheduledExecutor();
    private static final AtomicBoolean isReconnecting = new AtomicBoolean(false);

    public static void init() {
        connectAndInitializePIIC();

        commandSystem = new CommandSystem();

        commandSystem.registerCommand("account", new AccountCommand());
        registerBlockProvider("account", new AccountBlockProvider());

        commandSystem.registerCommand("at", new AtCommand());
        registerBlockProvider("at", new AtBlockProvider());

        commandSystem.registerCommand("button", new ButtonCommand());
        registerBlockProvider("button", new ButtonBlockProvider());

        commandSystem.registerCommand("chat", new ChatCommand());
        registerBlockProvider("chat", new ChatBlockProvider());

        commandSystem.registerCommand("clear", new ClearCommand());
        //

        commandSystem.registerCommand("click", new ClickCommand());
        registerBlockProvider("click", new ClickBlockProvider());

        commandSystem.registerCommand("close", new CloseCommand());
        registerBlockProvider("close", new CloseBlockProvider());

        commandSystem.registerCommand("curl", new CurlCommand());
        registerBlockProvider("curl", new CurlBlockProvider());

        commandSystem.registerCommand("desync", new DesyncCommand());
        registerBlockProvider("desync", new DesyncBlockProvider());

        commandSystem.registerCommand("disconnect", new DisconnectCommand());
        registerBlockProvider("disconnect", new DisconnectBlockProvider());

        commandSystem.registerCommand("echo", new EchoCommand());
        //

        commandSystem.registerCommand("help", new HelpCommand());
        //

        commandSystem.registerCommand("in", new InCommand());
        registerBlockProvider("in", new InBlockProvider());

        commandSystem.registerCommand("inventory", new InventoryCommand());
        registerBlockProvider("inventory", new InventoryBlockProvider());

        commandSystem.registerCommand("joinserver", new JoinServerCommand());
        registerBlockProvider("joinserver", new JoinServerBlockProvider());

        commandSystem.registerCommand("loop", new LoopCommand());
        registerBlockProvider("loop", new LoopBlockProvider());

        commandSystem.registerCommand("man", new ManCommand());
        //

        commandSystem.registerCommand("math", new MathCommand());

        commandSystem.registerCommand("mcfw", new McfwCommand());
        registerBlockProvider("mcfw", new McfwBlockProvider());

        commandSystem.registerCommand("rpack", new RPackCommand());
        registerBlockProvider("rpack", new RPackBlockProvider());

        commandSystem.registerCommand("rawsend", new RawSendCommand());
        //

        commandSystem.registerCommand("schemadump", new SchemaDumpCommand());
        registerBlockProvider("rawsend", new RawSendBlockProvider());

        commandSystem.registerCommand("syncsend", new SyncSendCommand());
        //

        commandSystem.registerCommand("screen", new ScreenCommand());
        registerBlockProvider("screen", new ScreenBlockProvider());

        commandSystem.registerCommand("window", new WindowCommand());
        //

        commandSystem.registerCommand("blocklydump", new BlocklyDumpCommand());
        //

        // events
        registerBlockProvider("screen_event", new ScreenEventBlockProvider());
        //        registerBlockProvider("on_tick", new OnTickBlockProvider());
        registerBlockProvider("on_start", new OnStartBlockProvider());

        registerBlockProvider("wait", new WaitBlockProvider());
        registerBlockProvider("rawcmd", new RawCommandBlockProvider());
    }

    private static void connectAndInitializePIIC() {
        if (UiUtils.piicClient != null && UiUtils.piicClient.isOpen()) {
            UiUtils.LOGGER.info(
                    "p.IIC connection is already healthy. Aborting redundant reconnect attempt.");
            isReconnecting.set(false);
            return;
        }

        try {
            int assignedPort = initializeAndGetPort();
            UiUtils.LOGGER.info(
                    "p.IIC setup complete. Command server will run on port {}.", assignedPort);

            if (webSocketCommandServer != null) {
                webSocketCommandServer.stop(1000);
            }
            webSocketCommandServer = new WebSocketCommandServer(assignedPort);
            webSocketCommandServer.init();
            isReconnecting.set(false);
        } catch (Exception e) {
            UiUtils.LOGGER.error("p.IIC system failed to initialize! Attempting reconnect...", e);
            handlePIICDisconnect();
        }
    }

    public static void handlePIICDisconnect() {
        if (isReconnecting.compareAndSet(false, true)) {
            piicReconnectScheduler.schedule(
                    () -> {
                        UiUtils.LOGGER.info(
                                "Attempting to reconnect and re-initialize p.IIC system...");
                        connectAndInitializePIIC();
                    },
                    5,
                    TimeUnit.SECONDS);
        }
    }

    private static int initializeAndGetPort() throws Exception {
        URI piicUri = new URI("ws://localhost:33532");
        UiUtils.piicClient = new PIICClient(piicUri);

        boolean connected = false;
        try {
            UiUtils.LOGGER.info("Attempting to connect to existing p.IIC manager...");
            connected = UiUtils.piicClient.connectBlocking(1, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }

        if (!connected) {
            UiUtils.LOGGER.warn("Could not connect. Launching new p.IIC manager process...");
            PIICLauncher launcher = new PIICLauncher();
            launcher.start();

            UiUtils.piicClient = new PIICClient(piicUri);
            if (!UiUtils.piicClient.connectBlocking(2, TimeUnit.SECONDS)) {
                throw new RuntimeException(
                        "Failed to connect to p.IIC manager even after it signaled ready.");
            }
        }

        UiUtils.LOGGER.info(
                "Successfully connected to p.IIC manager. Waiting for port assignment...");
        return UiUtils.piicClient.getAssignedPortBlocking();
    }

    public static void reload() {
        UiUtils.LOGGER.info("Reloading command system...");
        commandSystem = new CommandSystem();
        commands.clear();
        init();
        UiUtils.LOGGER.info("Command system reloaded successfully.");
    }

    public void registerCommand(String name, Command command) {
        commands.put(name.toLowerCase(), command);
    }

    public static void registerBlockProvider(String name, IBlockProvider provider) {
        blockProviders.put(name.toLowerCase(), provider);
    }

    private static List<String> splitCommandChain(String input) {
        List<String> commands = new ArrayList<>();
        StringBuilder currentCommand = new StringBuilder();
        char activeQuoteChar = 0; // 0 = none, ' = single, " = double

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (activeQuoteChar != 0) { // in quote
                if (c == activeQuoteChar) {
                    activeQuoteChar = 0; // end quote
                }
                currentCommand.append(c);
            } else { // outside a quote
                if (c == '\'' || c == '"') {
                    activeQuoteChar = c; // start of quote
                    currentCommand.append(c);
                } else if (c == '&' && i + 1 < input.length() && input.charAt(i + 1) == '&') {
                    // found a '&&' separator that isn't quoted
                    String commandToAdd = currentCommand.toString().trim();
                    if (!commandToAdd.isEmpty()) {
                        commands.add(commandToAdd);
                    }
                    currentCommand.setLength(0);
                    i++; // skip second '&'
                } else {
                    currentCommand.append(c);
                }
            }
        }

        // add the final command in the chain (or the only command if no '&&' was found)
        String finalCommand = currentCommand.toString().trim();
        if (!finalCommand.isEmpty()) {
            commands.add(finalCommand);
        }

        return commands;
    }

    public static List<CommandResult<?>> executeCommand(String input) {
        List<String> chainedCommands = splitCommandChain(input);
        List<CommandResult<?>> results = new ArrayList<>();

        for (String commandInput : chainedCommands) {
            String[] commandParts = commandInput.trim().split("\\s+", 2);
            if (commandParts.length == 0 || commandParts[0].isEmpty()) continue;

            String commandName = commandParts[0].toLowerCase();
            List<String> args = new ArrayList<>();
            if (commandParts.length > 1) {
                args.add(commandParts[1]);
            }

            Command command = commands.get(commandName);
            if (command == null) {
                CommandResult<?> error = CommandResult.of(false, "Unknown command: " + commandName);
                results.add(error);
                break;
            }

            try {
                CommandResult<?> result = command.execute(args);
                results.add(result);
                if (!result.success()) break;
            } catch (Exception e) {
                UiUtils.LOGGER.error("Error executing command: " + commandInput, e);
                results.add(
                        CommandResult.of(
                                false,
                                "Error executing command: "
                                        + e.getMessage()
                                        + ", "
                                        + e.getClass().getName()));
                break;
            }
        }

        return results;
    }
}
