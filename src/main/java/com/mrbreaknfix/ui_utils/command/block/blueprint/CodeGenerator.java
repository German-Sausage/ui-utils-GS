/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file CodeGenerator.java
 */
package com.mrbreaknfix.ui_utils.command.block.blueprint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class CodeGenerator {
    private final JsonObject json;

    protected CodeGenerator(String type) {
        this.json = new JsonObject();
        this.json.addProperty("type", type);
    }

    public static CodeGenerator standardCommand(String commandName) {
        CodeGenerator gen = new CodeGenerator("STANDARD_COMMAND");
        gen.json.addProperty("commandName", commandName);
        return gen;
    }

    public static CodeGenerator fromFieldsCommand(String commandName, String... fieldNames) {
        CodeGenerator gen = new CodeGenerator("FROM_FIELDS_COMMAND");
        gen.json.addProperty("commandName", commandName);
        JsonArray fields = new JsonArray();
        for (String f : fieldNames) fields.add(f);
        gen.json.add("fields", fields);
        return gen;
    }

    public static CodeGenerator mutatorCommand(String commandName) {
        CodeGenerator gen = new CodeGenerator("MUTATOR_COMMAND");
        gen.json.addProperty("commandName", commandName);
        return gen;
    }

    public static CodeGenerator rawSendCommand() {
        CodeGenerator gen = new CodeGenerator("RAW_SEND_COMMAND");
        gen.json.addProperty("commandName", "rawsend");
        return gen;
    }

    public static CodeGenerator shapeShifterCommand(String commandName) {
        CodeGenerator gen = new CodeGenerator("SHAPE_SHIFTER_COMMAND");
        gen.json.addProperty("commandName", commandName);
        return gen;
    }

    public static CodeGenerator shapeShifterWithVariable() {
        return new CodeGenerator("SHAPE_SHIFTER_WITH_VARIABLE");
    }

    public static CodeGenerator eventHandler() {
        return new CodeGenerator("EVENT_HANDLER");
    }

    public static CodeGenerator eventHandlerWithVars() {
        return new CodeGenerator("EVENT_HANDLER_WITH_VARS");
    }

    public static CodeGenerator awaitPromise(String promise) {
        CodeGenerator gen = new CodeGenerator("AWAIT_PROMISE");
        gen.json.addProperty("promise", promise);
        return gen;
    }

    public JsonObject toJson() {
        return this.json;
    }
}
