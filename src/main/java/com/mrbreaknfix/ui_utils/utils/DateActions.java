/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file DateActions.java
 */
package com.mrbreaknfix.ui_utils.utils;

import java.time.Month;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.mrbreaknfix.ui_utils.persistance.Settings;

public class DateActions {
    private static final long MIN_DELAY_MINUTES = 30;
    private static final long MAX_DELAY_MINUTES = 60;

    public static void initialize() {
        ZonedDateTime launchTime = ZonedDateTime.now();
        Month month = launchTime.getMonth();
        int day = launchTime.getDayOfMonth();

        if (month == Month.APRIL && day == 1 && !Settings.aprilFoolsDisabled) {
            startPinger();
        }
    }

    private static void startPinger() {
        ScheduledExecutorService pingerServer = Executors.newSingleThreadScheduledExecutor();

        Runnable notificationTask =
                new Runnable() {
                    @Override
                    public void run() {
                        // play the discord notification sound
                        SoundUtils.playSound("notification.wav");

                        long nextDelayInMinutes =
                                ThreadLocalRandom.current()
                                        .nextLong(MIN_DELAY_MINUTES, MAX_DELAY_MINUTES + 1);

                        pingerServer.schedule(this, nextDelayInMinutes, TimeUnit.MINUTES);
                    }
                };

        long initialDelayInMinutes =
                ThreadLocalRandom.current().nextLong(MIN_DELAY_MINUTES, MAX_DELAY_MINUTES + 1);
        pingerServer.schedule(notificationTask, initialDelayInMinutes, TimeUnit.MINUTES);
    }
}
