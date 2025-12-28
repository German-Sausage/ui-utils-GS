/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file AccountCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UndashedUuid;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;
import com.mrbreaknfix.ui_utils.mixin.FileCacheAccessor;
import com.mrbreaknfix.ui_utils.mixin.MinecraftClientAccessor;
import com.mrbreaknfix.ui_utils.mixin.PlayerSkinProviderAccessor;

import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.PlayerSkinTextureDownloader;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Util;

import static com.mrbreaknfix.ui_utils.UiUtils.LOGGER;
import static com.mrbreaknfix.ui_utils.UiUtils.mc;

public class AccountCommand extends BaseCommand {
    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        if (parsedArgs.isEmpty() || !(parsedArgs.get(0) instanceof String args0)) {
            return CommandResult.of(false, "Invalid usage. See `help account` for details.");
        }

        switch (args0) {
            case "get" -> {
                if (parsedArgs.size() < 2) {
                    return CommandResult.of(false, "Usage: account get <uuid|username|session>");
                }
                String type = (String) parsedArgs.get(1);
                Session session = mc.getSession();
                UUID uuid = session.getUuidOrNull();
                switch (type) {
                    case "uuid" -> {
                        return CommandResult.of(true, uuid != null ? uuid.toString() : "N/A");
                    }
                    case "username" -> {
                        return CommandResult.of(true, session.getUsername());
                    }
                    case "session" -> {
                        return CommandResult.of(true, session.getAccessToken());
                    }
                    default -> {
                        return CommandResult.of(
                                false,
                                "Invalid account part: "
                                        + type
                                        + ". Use 'uuid', 'username', or 'session'.");
                    }
                }
            }
            case "set" -> {
                if (parsedArgs.size() < 3) {
                    return CommandResult.of(
                            false, "Usage: account set <uuid|username|session> <value>");
                }
                String type = (String) parsedArgs.get(1);
                String value = (String) parsedArgs.get(2);
                Session currentSession = mc.getSession();
                Session newSession;
                switch (type) {
                    case "uuid" ->
                            newSession =
                                    new Session(
                                            currentSession.getUsername(),
                                            UndashedUuid.fromStringLenient(value),
                                            currentSession.getAccessToken(),
                                            Optional.empty(),
                                            Optional.empty());
                    case "username" ->
                            newSession =
                                    new Session(
                                            value,
                                            currentSession.getUuidOrNull(),
                                            currentSession.getAccessToken(),
                                            Optional.empty(),
                                            Optional.empty());
                    case "session" ->
                            newSession =
                                    new Session(
                                            currentSession.getUsername(),
                                            currentSession.getUuidOrNull(),
                                            value,
                                            Optional.empty(),
                                            Optional.empty());
                    default -> {
                        return CommandResult.of(false, "Invalid account type: " + type);
                    }
                }
                setSession(newSession);
                return CommandResult.of(true, "Account part updated successfully.");
            }
            case "dump" -> {
                Session session = mc.getSession();
                UUID uuid = session.getUuidOrNull();
                return CommandResult.of(
                        true,
                        """
                        Account dump:
                        UUID: %s
                        Username: %s
                        Access Token: %s
                        """
                                .formatted(uuid, session.getUsername(), session.getAccessToken()));
            }
            case "export" -> {
                if (parsedArgs.size() < 2) {
                    return CommandResult.of(false, "Usage: account export <file_path>");
                }
                Path path = Paths.get((String) parsedArgs.get(1));
                JsonObject jsonBody = getSessionAsJson();
                try {
                    String jsonContent =
                            new GsonBuilder().setPrettyPrinting().create().toJson(jsonBody);
                    Files.writeString(path, jsonContent);
                    return CommandResult.of(
                            true, "Session exported successfully to " + path.toAbsolutePath());
                } catch (IOException e) {
                    return CommandResult.of(false, "Failed to export session: " + e.getMessage());
                }
            }
            case "import" -> {
                if (parsedArgs.size() < 2) {
                    return CommandResult.of(false, "Usage: account import <file_path>");
                }
                return setSessionFromJson(Paths.get((String) parsedArgs.get(1)));
            }
            case "dump-json" -> {
                return CommandResult.of(
                        true, new Gson().toJson(getSessionAsJson()), getSessionAsJson());
            }
            case "set-json" -> {
                if (parsedArgs.size() < 2) {
                    return CommandResult.of(false, "Usage: account set-json <json_string>");
                }
                return setSessionFromJson((String) parsedArgs.get(1));
            }
            default -> {
                return CommandResult.of(false, "Unknown subcommand: " + args0);
            }
        }
    }

    private JsonObject getSessionAsJson() {
        Session session = mc.getSession();
        UUID uuid = session.getUuidOrNull();
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("uuid", uuid != null ? uuid.toString() : null);
        jsonBody.addProperty("username", session.getUsername());
        jsonBody.addProperty("accessToken", session.getAccessToken());
        return jsonBody;
    }

    private CommandResult<?> setSessionFromJson(String jsonContent) {
        try {
            LOGGER.info("Received JSON string for session update: {}", jsonContent);
            JsonObject json = new Gson().fromJson(jsonContent, JsonObject.class);
            if (!json.has("username") || !json.has("uuid") || !json.has("accessToken")) {
                return CommandResult.of(
                        false,
                        "Invalid JSON format. Must include 'username', 'uuid', and 'accessToken'.");
            }
            String username = json.get("username").getAsString();
            String uuidStr = json.get("uuid").getAsString();
            String accessToken = json.get("accessToken").getAsString();

            Session newSession =
                    new Session(
                            username,
                            UndashedUuid.fromStringLenient(uuidStr),
                            accessToken,
                            Optional.empty(),
                            Optional.empty());

            setSession(newSession);
            return CommandResult.of(true, "Session set successfully from JSON string.");
        } catch (JsonSyntaxException | IllegalStateException e) {
            return CommandResult.of(false, "Failed to parse JSON string: " + e.getMessage());
        }
    }

    private CommandResult<?> setSessionFromJson(Path path) {
        if (!Files.exists(path)) {
            return CommandResult.of(false, "File not found: " + path.toAbsolutePath());
        }
        try {
            String jsonContent = Files.readString(path);
            return setSessionFromJson(jsonContent);
        } catch (IOException e) {
            return CommandResult.of(false, "Failed to read file: " + e.getMessage());
        }
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("account", "Manage user account settings.")
                .then(
                        ArgumentNode.literal("dump", "Dump current session info."),
                        ArgumentNode.literal(
                                "dump-json", "Dump current session info as a raw JSON string."),
                        ArgumentNode.literal("get", "Get a single part of the session.")
                                .then(
                                        ArgumentNode.literal("uuid", "Get the current UUID."),
                                        ArgumentNode.literal(
                                                "username", "Get the current username."),
                                        ArgumentNode.literal(
                                                "session", "Get the current session token.")),
                        ArgumentNode.literal("set", "Set a new account detail.")
                                .then(
                                        ArgumentNode.literal("uuid", "Set UUID.")
                                                .then(
                                                        ArgumentNode.argument(
                                                                "<uuid>", "The new UUID.")),
                                        ArgumentNode.literal("username", "Set username.")
                                                .then(
                                                        ArgumentNode.argument(
                                                                "<username>", "The new username.")),
                                        ArgumentNode.literal("session", "Set session token.")
                                                .then(
                                                        ArgumentNode.argument(
                                                                "<session_token>",
                                                                "The new session token."))),
                        ArgumentNode.literal("set-json", "Set session from a raw JSON string.")
                                .then(ArgumentNode.argument("<json_string>", "The session JSON.")),
                        ArgumentNode.literal("export", "Export current session to a JSON file.")
                                .then(
                                        ArgumentNode.argument(
                                                "<file_path>", "Path to save the file.")),
                        ArgumentNode.literal("import", "Import session from a JSON file.")
                                .then(
                                        ArgumentNode.argument(
                                                "<file_path>", "Path to the file to load.")));
    }

    @Override
    public String manual() {
        return """
                NAME
                    account - Manage user account settings

                SYNOPSIS
                    account <command> [args]

                DESCRIPTION
                    A full-featured utility to manage the player session. Allows for manual setting, file-based import/export, and raw JSON string manipulation for scripting.

                COMMANDS
                    get <uuid|username|session>
                        Gets a single part of the current session and returns it.
                    dump
                        Dump the current account information in a readable format.
                    set <uuid|username|session> <value>
                        Set a single part of the account session.

                    export <file_path>
                        Export the current session to a specified JSON file.
                    import <file_path>
                        Import and apply a session from a specified JSON file.

                    dump-json
                        Dump the current session as a raw, machine-readable JSON string.
                    set-json <json_string>
                        Import and apply a session from a raw JSON string. The string can be wrapped in quotes.

                EXAMPLES
                    account get username
                    account dump
                    account set username Steve
                    account export ./my_session.json
                    account import ./my_session.json
                    account set-json "{\\"username\\":\\"Alex\\",\\"uuid\\":\\"069a79f4-44e9-4726-a5be-fca90e38aaf5\\", ...}"
                """;
    }

    public static void setSession(Session session) {
        MinecraftClientAccessor mca = (MinecraftClientAccessor) mc;
        mca.setSession(session);

        YggdrasilAuthenticationService yggdrasilAuthenticationService =
                new YggdrasilAuthenticationService(mc.getNetworkProxy());
        applyLoginEnvironment(yggdrasilAuthenticationService);

        UserApiService apiService =
                yggdrasilAuthenticationService.createUserApiService(session.getAccessToken());
        mca.setUserApiService(apiService);
        mca.setSocialInteractionsManager(new SocialInteractionsManager(mc, apiService));
        mca.setProfileKeys(ProfileKeys.create(apiService, session, mc.runDirectory.toPath()));
        mca.setAbuseReportContext(
                AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), apiService));
        mca.setGameProfileFuture(
                CompletableFuture.supplyAsync(
                        () ->
                                mc.getApiServices()
                                        .sessionService()
                                        .fetchProfile(mc.getSession().getUuidOrNull(), true),
                        Util.getIoWorkerExecutor()));
    }

    public static void applyLoginEnvironment(YggdrasilAuthenticationService authService) {
        MinecraftClientAccessor mca = (MinecraftClientAccessor) mc;
        SignatureVerifier.create(authService.getServicesKeySet(), ServicesKeyType.PROFILE_KEY);
        PlayerSkinProvider.FileCache skinCache =
                ((PlayerSkinProviderAccessor) mc.getSkinProvider()).getSkinCache();
        Path skinCachePath = ((FileCacheAccessor) skinCache).getDirectory();
        mca.setApiServices(ApiServices.create(authService, mc.runDirectory));
        mca.setSkinProvider(
                new PlayerSkinProvider(
                        skinCachePath,
                        mc.getApiServices(),
                        new PlayerSkinTextureDownloader(
                                mc.getNetworkProxy(), mc.getTextureManager(), mc),
                        mc));
    }
}
