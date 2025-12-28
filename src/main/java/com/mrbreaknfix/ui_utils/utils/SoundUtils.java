/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file SoundUtils.java
 */
package com.mrbreaknfix.ui_utils.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.*;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mrbreaknfix.ui_utils.UiUtils.MOD_ID;

public final class SoundUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final String SOUND_PATH_PREFIX = String.format("assets/%s/sounds/", MOD_ID);

    private SoundUtils() {}

    /**
     * Plays a sound file from the mod's resources. The sound file should be placed in
     * 'resources/assets/ui_utils/sounds/'.
     *
     * @param soundName The name of the sound file (e.g., "notification.wav").
     */
    public static void playSound(String soundName) {
        new Thread(
                        () -> {
                            try {
                                String resourcePath = SOUND_PATH_PREFIX + soundName;
                                InputStream stream =
                                        SoundUtils.class
                                                .getClassLoader()
                                                .getResourceAsStream(resourcePath);

                                if (stream == null) {
                                    LOGGER.error("Sound resource not found: {}", resourcePath);
                                    return;
                                }

                                // getResourceAsStream may not support mark/reset, so we buffer it.
                                InputStream bufferedStream = new BufferedInputStream(stream);

                                try (AudioInputStream audioStream =
                                        AudioSystem.getAudioInputStream(bufferedStream)) {
                                    Clip clip = getClip();

                                    clip.open(audioStream);
                                    clip.start();
                                }
                            } catch (Exception e) {
                                LOGGER.error("Could not play sound '{}'", soundName, e);
                            }
                        })
                .start();
    }

    private static @NotNull Clip getClip() throws LineUnavailableException {
        Clip clip = AudioSystem.getClip();

        // Add listener to close clip and free resources when done
        // playing.
        clip.addLineListener(
                event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
        return clip;
    }
}
