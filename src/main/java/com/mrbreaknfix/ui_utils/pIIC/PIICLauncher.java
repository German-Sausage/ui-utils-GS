/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PIICLauncher.java
 */
package com.mrbreaknfix.ui_utils.pIIC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.mrbreaknfix.ui_utils.UiUtils;
import com.mrbreaknfix.ui_utils.pIIC.server.PIICServer;

import net.fabricmc.loader.api.FabricLoader;

public class PIICLauncher {

    private Path getPIICDirectory() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path ourDir = gameDir.resolve("config").resolve("ui-utils");
        try {
            Files.createDirectories(ourDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create p.IIC directory", e);
        }
        return ourDir;
    }

    private Path deployJar() throws IOException {
        String jarName = "p.IIC-manager.jar";
        Path targetPath = getPIICDirectory().resolve(jarName);
        String resourcePath = "/assets/ui_utils/bin/" + jarName;
        try (InputStream sourceStream = PIICLauncher.class.getResourceAsStream(resourcePath)) {
            if (sourceStream == null) {
                throw new IOException(
                        "Could not find p.IIC-manager.jar in mod resources: " + resourcePath);
            }
            boolean needsUpdate =
                    !Files.exists(targetPath) || Files.size(targetPath) != sourceStream.available();
            if (needsUpdate) {
                UiUtils.LOGGER.info(
                        "Deploying/updating p.IIC manager JAR to: " + targetPath.toAbsolutePath());
                Files.copy(sourceStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                UiUtils.LOGGER.info("p.IIC manager JAR is already up-to-date.");
            }
        }
        return targetPath;
    }

    public void start() throws IOException, TimeoutException, InterruptedException {
        Path jarPath = deployJar();
        String javaExecutable = System.getProperty("java.home") + "/bin/java";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            javaExecutable += ".exe";
        }

        Path logFile = getPIICDirectory().resolve("p.IIC-manager.log");

        UiUtils.LOGGER.info("Starting p.IIC manager process...");

        List<String> command =
                new ArrayList<>(
                        List.of(javaExecutable, "-jar", jarPath.toAbsolutePath().toString()));

        if (UiUtils.isDevModeEnabled) {
            command.add("--dev");
            UiUtils.LOGGER.info("p.IIC manager launched in development mode.");
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectError(logFile.toFile());

        Process process = processBuilder.start();

        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            long timeout = 5; // 5 second timeout
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(timeout)) {
                if (reader.ready()) {
                    String line = reader.readLine();
                    if (line != null && line.trim().equals(PIICServer.READY_SIGNAL)) {
                        UiUtils.LOGGER.info("Received ready signal from p.IIC manager.");
                        return;
                    }
                }
                Thread.sleep(50);
            }
            process.destroyForcibly();
            throw new TimeoutException(
                    "p.IIC manager process did not signal ready within " + timeout + " seconds.");
        }
    }
}
