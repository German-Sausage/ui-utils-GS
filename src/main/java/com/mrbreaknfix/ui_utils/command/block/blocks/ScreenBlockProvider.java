/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ScreenBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.Arrays;
import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.*;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ShapeShifterConfig.ShapeRule;

import net.minecraft.screen.slot.SlotActionType;

public class ScreenBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        return List.of(createScreenManagementBlock(), createScreenSlotBlock());
    }

    private BlockBlueprint createScreenManagementBlock() {
        MutatorConfig mutator =
                MutatorConfig.create("screen")
                        .colour(180)
                        .addVariableClause("set_variable", "set variable to output");

        String[][] subcommands = {
            {"get info", "info"},
            {"list saved screens", "list"},
            {"show history", "history"},
            {"save current screen", "save"},
            {"load screen", "load"},
            {"remove saved screen", "remove"},
            {"navigate back", "back"},
            {"navigate forward", "forward"},
            {"reopen current", "reopen"},
            {"desync from server", "desync"},
            {"close without packet", "close"}
        };

        ShapeShifterConfig shifter =
                ShapeShifterConfig.create("SUBCOMMAND")
                        .addRule(
                                "info",
                                ShapeRule.valueInput(
                                        "SLOT_NAME", "for saved slot (optional)", "String"))
                        .addRule(
                                "history",
                                ShapeRule.valueInput(
                                        "COUNT", "show # entries (optional)", "Number"))
                        .addRule(
                                "save",
                                ShapeRule.valueInput(
                                        "SLOT_NAME", "to slot name (optional)", "String"))
                        .addRule(
                                "load",
                                ShapeRule.valueInput(
                                        "SLOT_NAME", "from slot name (optional)", "String"))
                        .addRule("remove", ShapeRule.valueInput("SLOT_NAME", "slot name", "String"))
                        .addRule("list")
                        .addRule("back")
                        .addRule("forward")
                        .addRule("reopen")
                        .addRule("desync")
                        .addRule("close");

        HybridMutatorConfig hybridConfig = HybridMutatorConfig.create(mutator, shifter);

        return BlockBlueprint.create("uiutils_screen")
                .message("screen %1")
                .args(Input.dropdown("SUBCOMMAND", subcommands))
                .previousStatement("ACTION")
                .nextStatement("ACTION")
                .colour(180)
                .tooltip(
                        "Manage screen states, history, and networking. Use the gear to capture output.")
                .categories(ToolboxCategory.SCREEN)
                .withHybridMutator(hybridConfig)
                .withCodeGenerator(CodeGenerator.mutatorCommand("screen"));
    }

    private BlockBlueprint createScreenSlotBlock() {
        MutatorConfig mutator =
                MutatorConfig.create("screen_slot")
                        .colour(220)
                        .addVariableClause("set_variable", "set variable to output");

        String[][] subcommands = {
            {"click slot", "click"},
            {"get slot info", "info"},
            {"list all slots", "list"},
            {"highlight slot", "highlight"},
            {"show slot IDs", "show-ids"}
        };

        //  dropdowns
        String[][] slotActionTypes =
                Arrays.stream(SlotActionType.values())
                        .map(action -> new String[] {action.name(), action.name()})
                        .toArray(String[][]::new);
        String[][] showIdsModes = {{"on", "on"}, {"off", "off"}, {"toggle", "toggle"}};

        // change inputs based on the selected slot action
        ShapeShifterConfig shifter =
                ShapeShifterConfig.create("SUBCOMMAND")
                        .addOptionsSource("slotActionTypes", slotActionTypes)
                        .addOptionsSource("showIdsModes", showIdsModes)
                        .addRule(
                                "click",
                                ShapeRule.valueInput("SLOT_ID", "ID", "Number"),
                                ShapeRule.valueInput("BUTTON", "button", "Number"),
                                ShapeRule.fieldDropdown(
                                        "ACTION_TYPE_INPUT",
                                        "action",
                                        "ACTION_TYPE",
                                        "slotActionTypes"))
                        .addRule("info", ShapeRule.valueInput("SLOT_ID", "ID", "Number"))
                        .addRule(
                                "highlight",
                                ShapeRule.valueInput("SLOT_ID", "ID", "Number"),
                                ShapeRule.valueInput(
                                        "DURATION", "duration ms (optional)", "Number"))
                        .addRule(
                                "show-ids",
                                ShapeRule.fieldDropdown(
                                        "MODE_INPUT", "mode", "MODE", "showIdsModes"))
                        .addRule("list");

        HybridMutatorConfig hybridConfig = HybridMutatorConfig.create(mutator, shifter);

        return BlockBlueprint.create("uiutils_screen_slot")
                .message("screen slot %1")
                .args(Input.dropdown("SUBCOMMAND", subcommands))
                .previousStatement("ACTION")
                .nextStatement("ACTION")
                .colour(220)
                .tooltip("Interact with slots in a container. Use the gear to capture output.")
                .categories(ToolboxCategory.SCREEN)
                .withHybridMutator(hybridConfig)
                .withCodeGenerator(CodeGenerator.mutatorCommand("screen slot"));
    }
}
