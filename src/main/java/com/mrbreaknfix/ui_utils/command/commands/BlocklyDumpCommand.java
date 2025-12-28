/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file BlocklyDumpCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;
import com.mrbreaknfix.ui_utils.command.CommandSystem;
import com.mrbreaknfix.ui_utils.command.block.blueprint.BlockBlueprint;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ToolboxCategory;

public class BlocklyDumpCommand extends BaseCommand {

    private static class BlueprintContainer {
        final BlockBlueprint blueprint;
        final List<JsonObject> jsonBlueprints;

        BlueprintContainer(BlockBlueprint blueprint) {
            this.blueprint = blueprint;
            this.jsonBlueprints = blueprint.buildWithChildren();
        }
    }

    @Override
    protected CommandResult<?> executeParsed(List<Object> parsedArgs) {
        List<BlueprintContainer> allContainers =
                CommandSystem.blockProviders.values().stream()
                        .flatMap(provider -> provider.getBlockBlueprints().stream())
                        .map(BlueprintContainer::new)
                        .collect(Collectors.toList());

        List<BlueprintContainer> userFacingContainers =
                allContainers.stream()
                        .filter(c -> c.blueprint.build().get("toolboxVisible").getAsBoolean())
                        .collect(Collectors.toList());
        Map<ToolboxCategory, JsonArray> blocksByCategory =
                buildCategorizedBlocks(userFacingContainers);
        JsonObject toolboxJson = buildFinalToolbox(blocksByCategory);

        JsonObject themeJson = buildThemeJson();

        JsonObject finalResponse = new JsonObject();
        JsonArray allJsonBlueprints =
                allContainers.stream()
                        .flatMap(c -> c.jsonBlueprints.stream())
                        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);

        finalResponse.add("blueprints", allJsonBlueprints);
        finalResponse.add("toolbox", toolboxJson);
        finalResponse.add("theme", themeJson);

        return CommandResult.of(true, "Loaded command block schema", finalResponse);
    }

    private JsonObject buildThemeJson() {
        JsonObject theme = new JsonObject();
        theme.addProperty("base", "dark");

        JsonObject categoryStyles = new JsonObject();
        for (ToolboxCategory category : ToolboxCategory.values()) {
            JsonObject style = new JsonObject();
            style.addProperty("colour", category.getColor());
            categoryStyles.add(category.getStyleName(), style);
        }
        theme.add("categoryStyles", categoryStyles);

        return theme;
    }

    private Map<ToolboxCategory, JsonArray> buildCategorizedBlocks(
            List<BlueprintContainer> containers) {
        Map<ToolboxCategory, JsonArray> blocksByCategory = new EnumMap<>(ToolboxCategory.class);
        for (BlueprintContainer container : containers) {
            String blockType = container.blueprint.getType();
            List<ToolboxCategory> categories = container.blueprint.getCategories();

            if (categories.isEmpty()) {
                blocksByCategory
                        .computeIfAbsent(ToolboxCategory.COMMANDS, k -> new JsonArray())
                        .add(createToolboxBlock(blockType));
            } else {
                for (ToolboxCategory category : categories) {
                    blocksByCategory
                            .computeIfAbsent(category, k -> new JsonArray())
                            .add(createToolboxBlock(blockType));
                }
            }
        }
        return blocksByCategory;
    }

    private JsonObject buildFinalToolbox(Map<ToolboxCategory, JsonArray> blocksByCategory) {
        JsonObject toolbox = new JsonObject();
        toolbox.addProperty("kind", "categoryToolbox");
        JsonArray contents = new JsonArray();

        blocksByCategory.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(
                        entry -> {
                            ToolboxCategory catEnum = entry.getKey();
                            JsonObject categoryJson = new JsonObject();
                            categoryJson.addProperty("kind", "category");
                            categoryJson.addProperty("name", catEnum.getDisplayName());
                            categoryJson.addProperty("categorystyle", catEnum.getStyleName());
                            categoryJson.add("contents", entry.getValue());
                            contents.add(categoryJson);
                        });

        contents.add(createSeparator());

        addStandardCategory(
                contents,
                "Logic",
                "logic_category",
                "controls_if",
                "logic_compare",
                "logic_operation",
                "logic_negate",
                "logic_boolean",
                "logic_null",
                "logic_ternary");

        addStandardCategory(
                contents,
                "Loops",
                "loop_category",
                "controls_repeat_ext",
                "controls_whileUntil",
                "controls_for",
                "controls_forEach",
                "controls_flow_statements");

        addStandardCategory(
                contents,
                "Math",
                "math_category",
                "math_number",
                "math_arithmetic",
                "math_single",
                "math_trig",
                "math_constant",
                "math_number_property",
                "math_round",
                "math_on_list",
                "math_modulo",
                "math_constrain",
                "math_random_int",
                "math_random_float");

        addStandardCategory(
                contents,
                "Text",
                "text_category",
                "text",
                "text_join",
                "text_append",
                "text_length",
                "text_isEmpty",
                "text_indexOf",
                "text_charAt",
                "text_getSubstring",
                "text_changeCase",
                "text_trim",
                "text_print",
                "text_prompt_ext");

        addStandardCategory(
                contents,
                "Lists",
                "list_category",
                "lists_create_with",
                "lists_create_empty",
                "lists_repeat",
                "lists_length",
                "lists_isEmpty",
                "lists_indexOf",
                "lists_getIndex",
                "lists_setIndex",
                "lists_getSublist",
                "lists_split",
                "lists_sort");

        contents.add(createSeparator());

        addCustomCategory(contents, "Variables", "variable_category", "VARIABLE");
        addCustomCategory(contents, "Functions", "procedure_category", "PROCEDURE");

        toolbox.add("contents", contents);
        return toolbox;
    }

    private JsonObject createToolboxBlock(String type) {
        JsonObject block = new JsonObject();
        block.addProperty("kind", "block");
        block.addProperty("type", type);
        return block;
    }

    private JsonObject createSeparator() {
        JsonObject sep = new JsonObject();
        sep.addProperty("kind", "sep");
        return sep;
    }

    private void addStandardCategory(
            JsonArray mainContents, String name, String style, String... blockTypes) {
        JsonObject category = new JsonObject();
        category.addProperty("kind", "category");
        category.addProperty("name", name);
        category.addProperty("categorystyle", style);
        JsonArray blockContents = new JsonArray();
        for (String type : blockTypes) {
            blockContents.add(createToolboxBlock(type));
        }
        category.add("contents", blockContents);
        mainContents.add(category);
    }

    private void addCustomCategory(
            JsonArray mainContents, String name, String style, String custom) {
        JsonObject category = new JsonObject();
        category.addProperty("kind", "category");
        category.addProperty("name", name);
        category.addProperty("categorystyle", style);
        category.addProperty("custom", custom);
        mainContents.add(category);
    }

    @Override
    public boolean visible() {
        return false;
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal(
                "blocklydump", "Dumps all Blockly block, toolbox, and theme definitions.");
    }

    @Override
    public String manual() {
        return "Internal command to provide block definitions to a Blockly frontend.";
    }
}
