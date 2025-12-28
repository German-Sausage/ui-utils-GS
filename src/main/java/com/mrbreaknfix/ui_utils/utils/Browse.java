/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file Browse.java
 */
package com.mrbreaknfix.ui_utils.utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class Browse {

    public static void openLink(String url) {
        // Try using java.awt.Desktop first (available since Java 6)
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI.create(url));
                    return;
                }
            } catch (Exception e) {
                // Fallback if Desktop fails
                System.err.println("Desktop.browse() failed: " + e.getMessage());
            }
        }

        // Fallback based on OS
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else if (os.contains("nix") || os.contains("nux")) {
                // Try common Linux browsers
                String[] browsers = {"xdg-open", "chromium", "google-chrome", "firefox", "mozilla"};
                boolean success = false;
                for (String browser : browsers) {
                    try {
                        new ProcessBuilder(browser, url).start();
                        success = true;
                        break;
                    } catch (IOException ignored) {
                    }
                }
                if (!success) {
                    throw new IOException("No supported browser found");
                }
            } else {
                throw new UnsupportedOperationException("Unsupported OS: " + os);
            }
        } catch (IOException | UnsupportedOperationException e) {
            System.err.println("Failed to open URL: " + e.getMessage());
        }
    }
}
