/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file CurlBlockProvider.java
 */
package com.mrbreaknfix.ui_utils.command.block.blocks;

import java.util.List;

import com.mrbreaknfix.ui_utils.command.block.IBlockProvider;
import com.mrbreaknfix.ui_utils.command.block.blueprint.*;

public class CurlBlockProvider implements IBlockProvider {
    @Override
    public List<BlockBlueprint> getBlockBlueprints() {
        MutatorConfig mutator =
                MutatorConfig.create("curl")
                        .colour(20)
                        .addClause("request", "with method (-X)", "String", true)
                        .addClause("header", "with header (-H)", "String", true)
                        .addClause("data_raw", "with raw data (-d)", "String", true)
                        .addClause("data_urlencode", "with url-encoded data", "String", true)
                        .addClause("form", "with form data (-F)", "String", true)
                        .addClause("output", "with output file (-o)", "String", true)
                        .addClause("user_agent", "with user agent (-A)", "String", true)
                        .addClause("user", "with user auth (-u)", "String", true)
                        .addClause("connect_timeout", "with connection timeout", "Number")
                        .addClause("location", "option: follow redirects (-L)", "Boolean")
                        .addClause("include", "option: include headers (-i)", "Boolean")
                        .addClause("verbose", "option: verbose output (-v)", "Boolean")
                        .addClause("insecure", "option: allow insecure (-k)", "Boolean")
                        .addVariableClause("set_variable", "set variable to output");

        return List.of(
                BlockBlueprint.create("uiutils_curl")
                        .message("curl url %1")
                        .args(Input.value("URL", "String"))
                        .previousStatement("ACTION")
                        .nextStatement("ACTION")
                        .colour(20)
                        .tooltip(
                                "Transfer data from or to a server. Use the gear to add flags or capture output.")
                        .categories(ToolboxCategory.NETWORK, ToolboxCategory.UTILITY)
                        .withMutator(mutator)
                        .withCodeGenerator(CodeGenerator.mutatorCommand("curl")));
    }
}
