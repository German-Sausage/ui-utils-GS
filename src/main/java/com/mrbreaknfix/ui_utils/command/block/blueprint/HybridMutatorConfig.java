/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file HybridMutatorConfig.java
 */
package com.mrbreaknfix.ui_utils.command.block.blueprint;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record HybridMutatorConfig(
        MutatorConfig mutatorConfig, ShapeShifterConfig shapeShifterConfig) {

    public static HybridMutatorConfig create(
            MutatorConfig mutatorConfig, ShapeShifterConfig shapeShifterConfig) {
        return new HybridMutatorConfig(mutatorConfig, shapeShifterConfig);
    }

    public String getMixinName() {
        return mutatorConfig.getMixinName();
    }

    public List<BlockBlueprint> getChildBlocks() {
        return mutatorConfig.getChildBlocks();
    }

    public JsonObject toJson() {
        JsonObject combinedData = shapeShifterConfig.toJson();

        JsonObject mutatorJson = mutatorConfig.toJson();
        for (Map.Entry<String, JsonElement> entry : mutatorJson.entrySet()) {
            combinedData.add(entry.getKey(), entry.getValue());
        }

        return combinedData;
    }
}
