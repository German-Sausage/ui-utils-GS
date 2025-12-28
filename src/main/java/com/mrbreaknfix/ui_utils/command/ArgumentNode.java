/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file ArgumentNode.java
 */
package com.mrbreaknfix.ui_utils.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.Expose;

import org.jetbrains.annotations.Nullable;

public class ArgumentNode {

    @Expose private final String name;
    @Expose private final SuggestionType type;
    @Expose @Nullable private final String description;
    @Expose @Nullable private final String ref;
    @Expose private final List<ArgumentNode> children = new ArrayList<>();

    private ArgumentNode(
            String name, SuggestionType type, @Nullable String description, @Nullable String ref) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.ref = ref;
    }

    public String getName() {
        return name;
    }

    public SuggestionType getType() {
        return type;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getRef() {
        return ref;
    }

    public static ArgumentNode literal(String name, @Nullable String description) {
        return new ArgumentNode(name, SuggestionType.LITERAL, description, null);
    }

    public static ArgumentNode argument(String name, @Nullable String description) {
        return new ArgumentNode(name, SuggestionType.ARGUMENT, description, null);
    }

    public static ArgumentNode nestedCommand(String name, @Nullable String description) {
        return new ArgumentNode(name, SuggestionType.NESTED_COMMAND, description, null);
    }

    public static ArgumentNode flagSet(String ref, @Nullable String description) {
        return new ArgumentNode(
                "[" + ref + "]", SuggestionType.FLAG_SET_REFERENCE, description, ref);
    }

    public ArgumentNode then(ArgumentNode child) {
        this.children.add(child);
        return this;
    }

    public ArgumentNode then(ArgumentNode... children) {
        this.children.addAll(List.of(children));
        return this;
    }

    public List<ArgumentNode> getChildren() {
        return Collections.unmodifiableList(children);
    }
}
