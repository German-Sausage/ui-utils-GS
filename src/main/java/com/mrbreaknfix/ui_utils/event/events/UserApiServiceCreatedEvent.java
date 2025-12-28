/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file UserApiServiceCreatedEvent.java
 */
package com.mrbreaknfix.ui_utils.event.events;

import com.mrbreaknfix.ui_utils.event.Event;

import org.jetbrains.annotations.Nullable;

public class UserApiServiceCreatedEvent extends Event {

    private final String accessToken;

    public UserApiServiceCreatedEvent(@Nullable String session) {
        this.accessToken = session;
    }
}
