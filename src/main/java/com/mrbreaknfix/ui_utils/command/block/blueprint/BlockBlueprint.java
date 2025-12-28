/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file BlockBlueprint.java
 */
package com.mrbreaknfix.ui_utils.command.block.blueprint;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// Don't read the code for the blocks...
// I was high on lack of sleep.
// Do not ask me questions about it either.
// Only God and I knew how this code works,
// Now, only God does.
// If you choose to waste time here, please beware your time will not be refunded.
public class BlockBlueprint {
    final JsonObject definition = new JsonObject();
    private final JsonObject blueprint = new JsonObject();
    private final List<BlockBlueprint> children = new ArrayList<>();
    private final List<ToolboxCategory> categories = new ArrayList<>();

    private BlockBlueprint(String type) {
        this.definition.addProperty("type", type);
        this.blueprint.add("definition", this.definition);
        this.toolboxVisible(true);
    }

    public static BlockBlueprint create(String type) {
        return new BlockBlueprint(type);
    }

    public BlockBlueprint message(String message) {
        this.definition.addProperty("message0", message);
        return this;
    }

    public BlockBlueprint args(Input... inputs) {
        JsonArray args = new JsonArray();
        for (Input input : inputs) {
            args.add(input.toJson());
        }
        this.definition.add("args0", args);
        return this;
    }

    public BlockBlueprint previousStatement(String connectionType) {
        this.definition.addProperty("previousStatement", connectionType);
        return this;
    }

    public BlockBlueprint nextStatement(String connectionType) {
        this.definition.addProperty("nextStatement", connectionType);
        return this;
    }

    public BlockBlueprint colour(int colour) {
        this.definition.addProperty("colour", colour);
        return this;
    }

    public BlockBlueprint tooltip(String tooltip) {
        this.definition.addProperty("tooltip", tooltip);
        return this;
    }

    public BlockBlueprint inputsInline(boolean inline) {
        this.definition.addProperty("inputsInline", inline);
        return this;
    }

    public BlockBlueprint toolboxVisible(boolean visible) {
        this.blueprint.addProperty("toolboxVisible", visible);
        return this;
    }

    public BlockBlueprint withMutator(MutatorConfig mutatorConfig) {
        this.blueprint.add("mutatorData", mutatorConfig.toJson());
        this.definition.addProperty("mutator", mutatorConfig.getMixinName());
        this.children.addAll(mutatorConfig.getChildBlocks());
        return this;
    }

    public BlockBlueprint withShapeShifter(ShapeShifterConfig shapeShifterConfig) {
        this.blueprint.add("mutatorData", shapeShifterConfig.toJson());
        return this;
    }

    public BlockBlueprint withCodeGenerator(CodeGenerator generator) {
        this.blueprint.add("codeGenerator", generator.toJson());
        return this;
    }

    public BlockBlueprint categories(ToolboxCategory... categories) {
        this.categories.addAll(List.of(categories));
        return this;
    }

    public List<ToolboxCategory> getCategories() {
        return this.categories;
    }

    public String getType() {
        return this.definition.get("type").getAsString();
    }

    public JsonObject build() {
        return this.blueprint;
    }

    public List<JsonObject> buildWithChildren() {
        List<JsonObject> allBlueprints = new ArrayList<>();
        allBlueprints.add(this.build());
        for (BlockBlueprint child : this.children) {
            allBlueprints.addAll(child.buildWithChildren());
        }
        return allBlueprints;
    }

    public BlockBlueprint withHybridMutator(HybridMutatorConfig config) {
        this.blueprint.add("mutatorData", config.toJson());
        this.definition.addProperty("mutator", config.getMixinName());
        this.children.addAll(config.getChildBlocks());
        return this;
    }
}
