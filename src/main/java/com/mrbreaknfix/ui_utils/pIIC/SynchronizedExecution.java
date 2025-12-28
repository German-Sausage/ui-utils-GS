/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file SynchronizedExecution.java
 */
package com.mrbreaknfix.ui_utils.pIIC;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A state-holder object for a single synchronized command execution. It tracks the command, the
 * participants, and the state of the multi-phase protocol.
 */
public class SynchronizedExecution {
    public record ExecutionResult(long startTime, long endTime) {

        public long getDurationNanos() {
            return endTime - startTime;
        }
    }

    private final String executionId;
    private final String command;
    private final String initiatorId;
    private final String selfId;

    private final Set<String> expectedClients;
    private final Set<String> readyClients = ConcurrentHashMap.newKeySet();
    private final Map<String, ExecutionResult> clientResults = new ConcurrentHashMap<>();
    private final AtomicBoolean executed = new AtomicBoolean(false);

    public SynchronizedExecution(
            String executionId,
            String command,
            String initiatorId,
            String selfId,
            List<String> targetIds) {
        this.executionId = executionId;
        this.command = command;
        this.initiatorId = initiatorId;
        this.selfId = selfId;

        if (isInitiator()) {
            this.expectedClients = new HashSet<>(targetIds);
            if (!this.expectedClients.contains(initiatorId)) {
                this.expectedClients.add(initiatorId);
            }
        } else {
            this.expectedClients = new HashSet<>();
        }
    }

    public String getCommand() {
        return command;
    }

    public String getInitiatorId() {
        return initiatorId;
    }

    public boolean isInitiator() {
        return selfId != null && selfId.equals(initiatorId);
    }

    public void addReadyClient(String clientId) {
        if (isInitiator()) {
            readyClients.add(clientId);
        }
    }

    public boolean isEveryoneReady() {
        return isInitiator() && readyClients.containsAll(expectedClients);
    }

    public boolean hasExecuted() {
        return executed.get();
    }

    public void markAsExecuted() {
        this.executed.set(true);
    }

    /** Stores the execution result from a client. */
    public void addResult(String clientId, ExecutionResult result) {
        if (isInitiator()) {
            clientResults.put(clientId, result);
        }
    }

    /** Checks if all expected clients have returned their execution results. */
    public boolean hasAllResult() {
        return isInitiator() && clientResults.keySet().containsAll(expectedClients);
    }

    public Map<String, ExecutionResult> getResults() {
        return clientResults;
    }

    public int getReadyClientCount() {
        return readyClients.size();
    }

    public int getExpectedClientCount() {
        return expectedClients.size();
    }
}
