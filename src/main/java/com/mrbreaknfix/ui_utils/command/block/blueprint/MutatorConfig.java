/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file MutatorConfig.java
 */
package com.mrbreaknfix.ui_utils.command.block.blueprint;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MutatorConfig {
    protected final String mixinName;
    protected final BlockBlueprint container;
    protected final List<MutatorRule> rules = new ArrayList<>();
    protected final List<BlockBlueprint> clauseBlocks = new ArrayList<>();

    protected MutatorConfig(String commandName) {
        this.mixinName = commandName + "_mutator_mixin";
        this.container =
                BlockBlueprint.create(commandName + "_mutator_container")
                        .message(commandName + " options %1 %2")
                        .args(Input.dummy(), Input.statement("STACK"))
                        .toolboxVisible(false);
    }

    public static MutatorConfig create(String commandName) {
        return new MutatorConfig(commandName);
    }

    public MutatorConfig colour(int colour) {
        this.container.colour(colour);
        this.clauseBlocks.forEach(cb -> cb.colour(colour));
        return this;
    }

    public MutatorConfig addClause(
            String flagName, String label, String inputTypeCheck, boolean isQuoted) {
        String clauseType = this.mixinName.replace("_mixin", "") + "_clause_" + flagName;
        this.rules.add(
                new MutatorRule(
                        flagName + "_INPUT",
                        label,
                        inputTypeCheck,
                        clauseType,
                        flagName,
                        isQuoted));

        BlockBlueprint clauseBlock =
                BlockBlueprint.create(clauseType)
                        .message(label)
                        .colour(
                                this.container.definition.has("colour")
                                        ? this.container.definition.get("colour").getAsInt()
                                        : 20)
                        .previousStatement(clauseType)
                        .nextStatement(clauseType)
                        .toolboxVisible(false);
        this.clauseBlocks.add(clauseBlock);
        return this;
    }

    public MutatorConfig addClause(String flagName, String label, String inputTypeCheck) {
        return this.addClause(flagName, label, inputTypeCheck, false);
    }

    public MutatorConfig addVariableClause(String flagName, String label) {
        return this.addClause(flagName, label, "Variable", false);
    }

    public String getMixinName() {
        return mixinName;
    }

    public List<BlockBlueprint> getChildBlocks() {
        JsonArray allClauseTypes = new JsonArray();
        this.clauseBlocks.forEach(
                cb -> allClauseTypes.add(cb.definition.get("type").getAsString()));
        this.clauseBlocks.forEach(
                cb -> {
                    cb.definition.add("previousStatement", allClauseTypes);
                    cb.definition.add("nextStatement", allClauseTypes);
                });

        List<BlockBlueprint> children = new ArrayList<>();
        children.add(this.container);
        children.addAll(this.clauseBlocks);
        return children;
    }

    public JsonObject toJson() {
        JsonObject data = new JsonObject();
        data.addProperty("mixinName", this.mixinName);
        data.addProperty("containerType", this.container.definition.get("type").getAsString());

        JsonArray clauseTypes = new JsonArray();
        this.clauseBlocks.forEach(cb -> clauseTypes.add(cb.definition.get("type").getAsString()));
        data.add("clauseTypes", clauseTypes);

        JsonArray dynamicInputs = new JsonArray();
        this.rules.forEach(rule -> dynamicInputs.add(rule.toJson()));
        data.add("dynamicInputs", dynamicInputs);

        return data;
    }

    protected static class MutatorRule {
        protected final JsonObject json;

        MutatorRule(
                String name,
                String label,
                String typeCheck,
                String clauseType,
                String flagName,
                boolean isQuoted) {
            this.json = new JsonObject();
            this.json.addProperty("name", name);
            this.json.addProperty("label", label);
            this.json.addProperty("typeCheck", typeCheck);
            this.json.addProperty("clauseType", clauseType);
            this.json.addProperty("flagName", flagName);
            this.json.addProperty("isQuoted", isQuoted);
        }

        public JsonObject toJson() {
            return this.json;
        }
    }
}
