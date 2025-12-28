/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file PacketNameMapGenerator.java
 */
package com.mrbreaknfix.ui_utils.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A build tool to generate a class with a mapping from intermediary to named packet names. It works
 * by parsing the local tiny-mappings file provided by Fabric Loom.
 *
 * @author MrBreakNFix
 */
public class PacketNameMapGenerator {

    private static final String GENERATED_CLASS_NAME = "PacketNameUtil";
    private static final List<String> TARGET_PACKAGES =
            List.of(
                    "net/minecraft/network/packet/c2s/play/",
                    "net/minecraft/network/packet/c2s/common/",
                    "net/minecraft/network/packet/c2s/config/",
                    "net/minecraft/network/packet/c2s/query/",
                    "net/minecraft/network/packet/c2s/login/",
                    "net/minecraft/network/packet/c2s/handshake/",
                    "net/minecraft/network/packet/c2s/status/");

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(
                    "Usage: java PacketNameMapGenerator <output_dir> <mappings_file_path>");
            System.exit(1);
        }

        try {
            Path targetDir = Path.of(args[0]);
            Path mappingsFile = Path.of(args[1]);
            String packageName = "com.mrbreaknfix.ui_utils.packet";

            System.out.println("--- Packet Name Map Generator (Gradle Task) ---");
            System.out.println("Output Directory: " + targetDir);
            System.out.println("Mappings File: " + mappingsFile);

            Map<String, String> packetMap = parseMappings(mappingsFile);
            String generatedCode = generateJavaClass(packetMap, packageName);
            saveGeneratedFile(targetDir, generatedCode);

        } catch (Exception e) {
            System.err.println(
                    "\nAn error occurred during packet name map generation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Map<String, String> parseMappings(Path mappingsJarFile) throws IOException {
        System.out.println("Parsing mappings file: " + mappingsJarFile.getFileName());
        Map<String, String> packetMap = new TreeMap<>();

        URI jarUri = URI.create("jar:" + mappingsJarFile.toUri());

        try (FileSystem jarFileSystem = FileSystems.newFileSystem(jarUri, Map.of())) {
            Path mappingsPathInJar = jarFileSystem.getPath("mappings/mappings.tiny");

            if (!Files.exists(mappingsPathInJar)) {
                throw new IOException(
                        "Could not find 'mappings/mappings.tiny' inside " + mappingsJarFile);
            }

            try (BufferedReader reader = Files.newBufferedReader(mappingsPathInJar)) {
                String header = reader.readLine();
                if (header == null || !header.startsWith("tiny\t")) {
                    throw new IOException("Invalid or empty mappings file.");
                }

                List<String> namespaces = Arrays.asList(header.split("\t"));
                int intermediaryIndex = namespaces.indexOf("intermediary");
                int namedIndex = namespaces.indexOf("named");

                if (intermediaryIndex == -1 || namedIndex == -1) {
                    throw new IOException(
                            "Mappings file header is missing 'intermediary' or 'named' namespace: "
                                    + header);
                }

                // Index:      0   1  2      3             4
                // Data for a class mapping looks like: c <intermediary> <named>
                // So the data column for the namespace at header index `i` is `i - 2`.
                final int intermediaryDataIndex = intermediaryIndex - 2;
                final int namedDataIndex = namedIndex - 2;

                System.out.println(
                        "Detected mapping columns: Intermediary at data index "
                                + intermediaryDataIndex
                                + ", Named at data index "
                                + namedDataIndex);

                reader.lines()
                        .filter(line -> line.startsWith("c\t"))
                        .forEach(
                                line -> {
                                    String[] parts = line.split("\t");
                                    if (parts.length
                                            <= Math.max(intermediaryDataIndex, namedDataIndex))
                                        return;

                                    String namedName = parts[namedDataIndex];
                                    if (TARGET_PACKAGES.stream().anyMatch(namedName::startsWith)) {
                                        String intermediaryName = parts[intermediaryDataIndex];
                                        String simpleIntermediary =
                                                getSimpleClassName(intermediaryName);
                                        String simpleNamed = getSimpleClassName(namedName);
                                        packetMap.put(simpleIntermediary, simpleNamed);
                                    }
                                });
            }
        }
        System.out.println("Parsing complete. Found " + packetMap.size() + " C2S packet mappings.");
        if (packetMap.isEmpty()) {
            System.err.println(
                    "WARNING: No packet mappings were found. The generated file will be empty. Check TARGET_PACKAGES if this is unexpected.");
        }
        return packetMap;
    }

    private static String getSimpleClassName(String fullPath) {
        int lastSlash = fullPath.lastIndexOf('/');
        return (lastSlash == -1) ? fullPath : fullPath.substring(lastSlash + 1);
    }

    private static void saveGeneratedFile(Path saveDir, String content) throws IOException {
        Path outputFile = saveDir.resolve(GENERATED_CLASS_NAME + ".java");
        Files.writeString(
                outputFile,
                content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("SUCCESS: File saved to: " + outputFile.toAbsolutePath());
    }

    private static String generateJavaClass(Map<String, String> packetMap, String packageName) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("package %s;\n\n", packageName));
        sb.append("import net.minecraft.network.packet.Packet;\n\n");
        sb.append("import java.util.HashMap;\n");
        sb.append("import java.util.List;\n");
        sb.append("import java.util.Map;\n");
        sb.append("import java.util.stream.Collectors;\n\n");
        sb.append("/**\n * Auto-generated by PacketNameMapGenerator. DO NOT EDIT.\n */\n");
        sb.append(String.format("public final class %s {\n", GENERATED_CLASS_NAME));
        sb.append("    private static final Map<String, String> PACKET_MAP = new HashMap<>();\n");
        sb.append("    private static List<String> packetNameCache = null;\n\n");
        sb.append("    public static String getPacketName(Packet<?> packet) {\n");
        sb.append("        String rawClassName = packet.getClass().getName();\n");
        sb.append(
                "        String simpleName = rawClassName.substring(rawClassName.lastIndexOf('.') + 1);\n");
        sb.append("        String mappedName = PACKET_MAP.get(simpleName);\n");
        sb.append("        if (mappedName != null) {\n");
        sb.append(
                "            return mappedName.replace(\"C2SPacket\", \"\").replace('$', '.');\n");
        sb.append("        }\n");
        sb.append("        return simpleName.replace(\"C2SPacket\", \"\").replace('$', '.');\n");
        sb.append("    }\n\n");
        sb.append("    public static List<String> getAllPacketNames() {\n");
        sb.append("        if (packetNameCache == null) {\n");
        sb.append("            packetNameCache = PACKET_MAP.values().stream()\n");
        sb.append(
                "                    .map(name -> name.replace(\"C2SPacket\", \"\").replace('$', '.'))\n");
        sb.append("                    .distinct()\n");
        sb.append("                    .sorted()\n");
        sb.append("                    .collect(Collectors.toList());\n");
        sb.append("        }\n");
        sb.append("        return packetNameCache;\n");
        sb.append("    }\n\n");
        sb.append("    static {\n");
        for (Map.Entry<String, String> entry : packetMap.entrySet()) {
            sb.append(
                    String.format(
                            "        PACKET_MAP.put(\"%s\", \"%s\");\n",
                            entry.getKey(), entry.getValue()));
        }
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }
}
