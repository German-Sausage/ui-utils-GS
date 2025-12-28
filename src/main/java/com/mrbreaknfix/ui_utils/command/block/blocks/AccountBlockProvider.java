/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file AccountBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.*;
import com.mrbreaknfix.ui_utils.command.block.blueprint.ShapeShifterConfig.ShapeRule;

public class AccountBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        MutatorConfig mutator =
                MutatorConfig.create("account")
                        .colour(260)
                        .addVariableClause("set_variable", "set variable to output");

        String[][] subcommands = {
            {"get session part", "get"},
            {"dump session", "dump"},
            {"dump session as JSON", "dump-json"},
            {"export session to file", "export"},
            {"import session from file", "import"},
            {"set session part", "set"},
            {"set session from JSON", "set-json"}
        };
        String[][] getSetTypes = {
            {"uuid", "uuid"},
            {"username", "username"},
            {"session token", "session"}
        };
        ShapeShifterConfig shifter =
                ShapeShifterConfig.create("SUBCOMMAND")
                        .addOptionsSource("getSetTypes", getSetTypes)
                        .addRule(
                                "get",
                                ShapeRule.fieldDropdown("TYPE", "get", "TYPE_FIELD", "getSetTypes"))
                        .addRule("dump")
                        .addRule("dump-json")
                        .addRule("export", ShapeRule.valueInput("PATH", "to file path", "String"))
                        .addRule("import", ShapeRule.valueInput("PATH", "from file path", "String"))
                        .addRule(
                                "set",
                                ShapeRule.fieldDropdown("TYPE", "set", "TYPE_FIELD", "getSetTypes"),
                                ShapeRule.valueInput("VALUE", "to value", "String"))
                        .addRule(
                                "set-json",
                                ShapeRule.valueInput("JSON", "from JSON string", "String"));

        HybridMutatorConfig hybridConfig = HybridMutatorConfig.create(mutator, shifter);

        return List.of(
                BlockBlueprint.create("uiutils_account")
                        .message("account %1")
                        .args(Input.dropdown("SUBCOMMAND", subcommands))
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(260)
                        .tooltip(
                                "Manages the player's session. Shape-shifts and has optional outputs via the gear.")
                        .categories(ToolboxCategory.PLAYER)
                        .withHybridMutator(hybridConfig)
                        .withCodeGenerator(CodeGenerator.mutatorCommand("account")));
    }
}
