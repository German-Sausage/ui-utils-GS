/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file SyncExecutionCoordinator.java
 */
package com.mrbreaknfix.ui_utils.pIIC;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.command.CommandSystem;

import net.minecraft.client.MinecraftClient;

/**
 * Manages the lifecycle and state of the 'syncsend' protocol. This class contains all logic for the
 * prepare/ready/execute/result flow, keeping it entirely separate from the PIICClient connection
 * logic.
 */
public class SyncExecutionCoordinator {

    private static final Map<String, SynchronizedExecution> pendingExecutions =
            new ConcurrentHashMap<>();
    private static PIICClient piicClient;

    public static void initialize(PIICClient client) {
        if (client == null) {
            UiUtils.LOGGER.error(
                    "Cannot initialize SyncExecutionCoordinator with a null PIICClient!");
            return;
        }
        piicClient = client;
        piicClient.registerActionHandler(
                "prepare_command", SyncExecutionCoordinator::handlePrepare);
        piicClient.registerActionHandler("command_ready", SyncExecutionCoordinator::handleReady);
        piicClient.registerActionHandler(
                "execute_command", SyncExecutionCoordinator::handleExecute);
        piicClient.registerActionHandler(
                "execution_result", SyncExecutionCoordinator::handleResult);
        UiUtils.LOGGER.info(
                "SyncExecutionCoordinator initialized and protocol handlers registered.");
    }

    public static void start(String command, List<String> targetIds) {
        if (piicClient == null || !piicClient.isOpen()) {
            UiUtils.LOGGER.error("Cannot start synchronized command: PIICClient is not connected.");
            return;
        }
        String executionId = UUID.randomUUID().toString();
        SynchronizedExecution execution =
                new SynchronizedExecution(
                        executionId,
                        command,
                        piicClient.getInstanceId(),
                        piicClient.getInstanceId(),
                        targetIds);
        pendingExecutions.put(executionId, execution);
        UiUtils.LOGGER.info(
                "[SYNC] Initiating execution ID: {}. Command: '{}'", executionId, command);
        JsonObject payload = createPayload("prepare_command", "executionId", executionId);
        payload.addProperty("command", command);
        piicClient.sendBroadcast(payload);
    }

    private static void handlePrepare(String fromId, JsonObject payload) {
        String executionId = payload.get("executionId").getAsString();
        if (pendingExecutions.containsKey(executionId)) {
            UiUtils.LOGGER.info(
                    "[SYNC] Initiator ignoring its own 'prepare_command' signal for ID: {}.",
                    executionId);
        } else {
            String command = payload.get("command").getAsString();
            SynchronizedExecution execution =
                    new SynchronizedExecution(
                            executionId,
                            command,
                            fromId,
                            piicClient.getInstanceId(),
                            Collections.emptyList());
            pendingExecutions.put(executionId, execution);
            UiUtils.LOGGER.info(
                    "[SYNC] Received and cached command for execution ID: {}. Command: '{}'",
                    executionId,
                    command);
        }
        piicClient.sendUnicast(fromId, createPayload("command_ready", "executionId", executionId));
    }

    private static void handleReady(String fromId, JsonObject payload) {
        String executionId = payload.get("executionId").getAsString();
        SynchronizedExecution execution = pendingExecutions.get(executionId);
        if (execution != null && execution.isInitiator()) {
            execution.addReadyClient(fromId);
            UiUtils.LOGGER.info(
                    "[SYNC] Received 'command_ready' from '{}' for ID: {}. ({} / {} clients ready)",
                    fromId,
                    executionId,
                    execution.getReadyClientCount(),
                    execution.getExpectedClientCount());
            if (execution.isEveryoneReady()) {
                UiUtils.LOGGER.info(
                        "[SYNC] All clients ready for ID: {}. Broadcasting GO signal!",
                        executionId);
                piicClient.sendBroadcast(
                        createPayload("execute_command", "executionId", executionId));
            }
        }
    }

    private static void handleExecute(String fromId, JsonObject payload) {
        MinecraftClient.getInstance()
                .execute(
                        () -> {
                            String executionId = payload.get("executionId").getAsString();
                            UiUtils.LOGGER.info(
                                    "[SYNC] Received EXECUTE signal for ID: {}", executionId);
                            SynchronizedExecution execution = pendingExecutions.get(executionId);
                            if (execution != null && !execution.hasExecuted()) {
                                UiUtils.LOGGER.info(
                                        "[SYNC] Executing cached command: '{}'",
                                        execution.getCommand());
                                long startTime = System.nanoTime();
                                CommandSystem.executeCommand(execution.getCommand());
                                long endTime = System.nanoTime();
                                execution.markAsExecuted();
                                JsonObject resultPayload =
                                        createPayload(
                                                "execution_result", "executionId", executionId);
                                resultPayload.addProperty("startTime", startTime);
                                resultPayload.addProperty("endTime", endTime);
                                UiUtils.LOGGER.info(
                                        "[SYNC] Execution finished. Sending result to initiator ({})",
                                        execution.getInitiatorId());
                                piicClient.sendUnicast(execution.getInitiatorId(), resultPayload);
                            } else {
                                UiUtils.LOGGER.error(
                                        "[SYNC] Could not execute command for ID: {}. Pending object was {} or already executed.",
                                        executionId,
                                        (execution == null ? "not found" : "found"));
                            }
                        });
    }

    /**
     * Handles the final execution result. Only the initiator processes this. Now provides a
     * detailed analysis of execution timing.
     */
    private static void handleResult(String fromId, JsonObject payload) {
        String executionId = payload.get("executionId").getAsString();
        SynchronizedExecution execution = pendingExecutions.get(executionId);

        if (execution != null && execution.isInitiator()) {
            long startTime = payload.get("startTime").getAsLong();
            long endTime = payload.get("endTime").getAsLong();
            execution.addResult(
                    fromId, new SynchronizedExecution.ExecutionResult(startTime, endTime));

            if (execution.hasAllResult()) {
                analyzeAndLogResults(execution);
                pendingExecutions.remove(executionId);
            }
        }
    }

    private static void analyzeAndLogResults(SynchronizedExecution execution) {
        Map<String, SynchronizedExecution.ExecutionResult> results = execution.getResults();
        long minStartTime = Long.MAX_VALUE;
        long maxStartTime = Long.MIN_VALUE;

        UiUtils.LOGGER.info(
                "--- Synchronized Execution Final Report [ID: {}] ---",
                execution.getResults().keySet().iterator().next()); // =I I don't like this

        for (Map.Entry<String, SynchronizedExecution.ExecutionResult> entry : results.entrySet()) {
            String clientId = entry.getKey();
            SynchronizedExecution.ExecutionResult result = entry.getValue();
            minStartTime = Math.min(minStartTime, result.startTime());
            maxStartTime = Math.max(maxStartTime, result.startTime());

            UiUtils.LOGGER.info("  - Client: {}", clientId);
            UiUtils.LOGGER.info(
                    "    Local Duration: {} ms", result.getDurationNanos() / 1_000_000.0);
        }

        long executionOffsetNanos = maxStartTime - minStartTime;
        UiUtils.LOGGER.info("-------------------------------------------------------");
        UiUtils.LOGGER.info(
                "  Total Execution Offset (Start Time Skew): {} ms",
                executionOffsetNanos / 1_000_000.0);
        UiUtils.LOGGER.info("-------------------------------------------------------");
        UiUtils.LOGGER.info("[SYNC] Completed and cleaned up execution.");
    }

    private static JsonObject createPayload(String action, String key, String value) {
        JsonObject payload = new JsonObject();
        payload.addProperty("action", action);
        payload.addProperty(key, value);
        return payload;
    }
}
