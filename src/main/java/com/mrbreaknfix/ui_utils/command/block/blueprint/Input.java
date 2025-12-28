/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Input.java
 */
package com.mrbreaknfix.ui_utils.command.block.blueprint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Input {
    private final JsonObject json;

    private Input(String type, String name) {
        this.json = new JsonObject();
        this.json.addProperty("type", type);
        if (name != null) {
            this.json.addProperty("name", name);
        }
    }

    public static Input value(String name, String check) {
        Input input = new Input("input_value", name);
        input.json.addProperty("check", check);
        return input;
    }

    public static Input statement(String name) {
        return new Input("input_statement", name);
    }

    public static Input dummy() {
        return new Input("input_dummy", null);
    }

    public static Input dropdown(String name, String[][] options) {
        Input input = new Input("field_dropdown", name);
        JsonArray optionsArray = new JsonArray();
        for (String[] option : options) {
            JsonArray opt = new JsonArray();
            opt.add(option[0]);
            opt.add(option[1]);
            optionsArray.add(opt);
        }
        input.json.add("options", optionsArray);
        return input;
    }

    public JsonObject toJson() {
        return this.json;
    }
}
