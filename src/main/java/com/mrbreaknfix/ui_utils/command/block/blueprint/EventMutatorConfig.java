/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file EventMutatorConfig.java
 */
package com.mrbreaknfix.ui_utils.command.block.blueprint;

public class EventMutatorConfig extends MutatorConfig {
    private EventMutatorConfig(String eventName) {
        super("event_" + eventName);
    }

    public static EventMutatorConfig create(String eventName) {
        return new EventMutatorConfig(eventName);
    }

    public EventMutatorConfig addClause(String inputName, String label, String jsonProperty) {
        String clauseType = this.getMixinName().replace("_mixin", "") + "_clause_" + jsonProperty;

        this.rules.add(new EventMutatorRule(inputName, label, clauseType, jsonProperty));

        BlockBlueprint clauseBlock =
                BlockBlueprint.create(clauseType)
                        .message(label)
                        .colour(
                                this.container.definition.has("colour")
                                        ? this.container.definition.get("colour").getAsInt()
                                        : 20)
                        .toolboxVisible(false);
        this.clauseBlocks.add(clauseBlock);
        return this;
    }

    @Override
    public EventMutatorConfig colour(int colour) {
        super.colour(colour);
        return this;
    }

    private static class EventMutatorRule extends MutatorRule {
        EventMutatorRule(String name, String label, String clauseType, String jsonProperty) {
            super(name, label, "Variable", clauseType, jsonProperty, false);
            this.json.addProperty("jsonProperty", jsonProperty);
        }
    }
}
