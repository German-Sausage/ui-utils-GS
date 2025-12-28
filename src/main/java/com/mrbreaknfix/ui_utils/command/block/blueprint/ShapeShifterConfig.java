/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ShapeShifterConfig.java
 */
package com.mrbreaknfix.ui_utils.command.block.blueprint;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ShapeShifterConfig {
    private final String controlField;
    private final Map<String, JsonArray> shapeRules = new HashMap<>();
    private final JsonObject optionsSources = new JsonObject();

    private ShapeShifterConfig(String controlField) {
        this.controlField = controlField;
    }

    public static ShapeShifterConfig create(String controlField) {
        return new ShapeShifterConfig(controlField);
    }

    public ShapeShifterConfig addOptionsSource(String key, String[][] options) {
        JsonArray optionsArray = new JsonArray();
        for (String[] option : options) {
            JsonArray opt = new JsonArray();
            opt.add(option[0]);
            opt.add(option[1]);
            optionsArray.add(opt);
        }
        this.optionsSources.add(key, optionsArray);
        return this;
    }

    public ShapeShifterConfig addRule(String controlValue, ShapeRule... rules) {
        JsonArray ruleArray = new JsonArray();
        for (ShapeRule rule : rules) {
            ruleArray.add(rule.toJson());
        }
        this.shapeRules.put(controlValue, ruleArray);
        return this;
    }

    public ShapeShifterConfig addRule(String controlValue) {
        this.shapeRules.put(controlValue, new JsonArray());
        return this;
    }

    public JsonObject toJson() {
        JsonObject data = new JsonObject();
        data.addProperty("controlField", this.controlField);

        JsonObject rulesJson = new JsonObject();
        this.shapeRules.forEach(rulesJson::add);
        data.add("shapeRules", rulesJson);

        data.add("optionsSources", this.optionsSources);
        return data;
    }

    public record ShapeRule(JsonObject json) {

        public static ShapeRule fieldDropdown(
                String inputName, String label, String fieldName, String optionsKey) {
            JsonObject rule = new JsonObject();
            rule.addProperty("type", "field_dropdown");
            rule.addProperty("name", inputName);
            rule.addProperty("label", label);
            rule.addProperty("fieldName", fieldName);
            rule.addProperty("optionsKey", optionsKey);
            return new ShapeRule(rule);
        }

        public static ShapeRule fieldDropdown(
                String inputName, String label, String fieldName, String[][] options) {
            JsonObject rule = new JsonObject();
            rule.addProperty("type", "field_dropdown");
            rule.addProperty("name", inputName);
            rule.addProperty("label", label);
            rule.addProperty("fieldName", fieldName);
            JsonArray optionsArray = new JsonArray();
            for (String[] option : options) {
                JsonArray opt = new JsonArray();
                opt.add(option[0]);
                opt.add(option[1]);
                optionsArray.add(opt);
            }
            rule.add("options", optionsArray);
            return new ShapeRule(rule);
        }

        public static ShapeRule valueInput(String inputName, String label, String typeCheck) {
            JsonObject rule = new JsonObject();
            rule.addProperty("type", "input_value");
            rule.addProperty("name", inputName);
            rule.addProperty("label", label);
            rule.addProperty("typeCheck", typeCheck);
            return new ShapeRule(rule);
        }

        public JsonObject toJson() {
            return this.json;
        }
    }
}
