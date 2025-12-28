/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file UiUtilsPacketRegistrarGeneratorFinal.java
 */
package com.mrbreaknfix.ui_utils.tool;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * An integrated build tool to generate a self-contained Java registration class for Minecraft C2S
 * packets. This tool is executed by a Gradle task and receives all necessary paths as arguments.
 *
 * @author MrBreakNFix
 */
public class UiUtilsPacketRegistrarGeneratorFinal {

    private static final String GENERATED_CLASS_NAME = "GeneratedPacketRegistry";
    private static final List<String> TARGET_PACKAGES =
            List.of(
                    "net/minecraft/network/packet/c2s/play",
                    "net/minecraft/network/packet/c2s/common",
                    "net/minecraft/network/packet/c2s/config",
                    "net/minecraft/network/packet/c2s/query",
                    "net/minecraft/network/packet/c2s/login",
                    "net/minecraft/network/packet/c2s/handshake",
                    "net/minecraft/network/packet/c2s/status");

    private record PacketData(
            String key, Class<?> packetClass, List<ParamData> params, Constructor<?> constructor) {}

    private record ParamData(String sourceCodeTypeName, String internalTypeName, String name) {}

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(
                    "Usage: java UiUtilsPacketRegistrarGeneratorFinal <output_dir> <minecraft_jar_path>");
            System.exit(1);
        }

        try {
            Path targetDir = Path.of(args[0]);
            Path mappedJarPath = Path.of(args[1]);
            String packageName = derivePackageNameFrom(targetDir);

            System.out.println("--- Minecraft Packet Registrar Generator (Gradle Task) ---");
            System.out.println("Output Directory: " + targetDir);
            System.out.println("Minecraft JAR: " + mappedJarPath);
            System.out.println("Target Package: " + packageName);

            if (!Files.isDirectory(targetDir)) {
                Files.createDirectories(targetDir);
            }

            List<PacketData> allPacketData = analyzeJar(mappedJarPath);

            System.out.println("\n--- Generating Java Class ---");
            String generatedCode = generateJavaClass(allPacketData, packageName);

            saveGeneratedFile(targetDir, generatedCode);

        } catch (Exception e) {
            System.err.println("\nAn error occurred during packet generation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Exit with failure code for Gradle
        }
    }

    private static List<PacketData> analyzeJar(Path jarPath) throws IOException {
        System.out.println("\nAnalyzing JAR: " + jarPath.getFileName());
        Map<String, PacketData> packetDataMap = new TreeMap<>();

        // Use the system classloader which Gradle has configured with the full project classpath
        ClassLoader classLoader = UiUtilsPacketRegistrarGeneratorFinal.class.getClassLoader();

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            for (JarEntry entry : Collections.list(jarFile.entries())) {
                String name = entry.getName();
                if (!name.endsWith(".class")
                        || TARGET_PACKAGES.stream().noneMatch(name::startsWith)) continue;

                String internalClassName = name.replace('/', '.').substring(0, name.length() - 6);
                try {
                    // Load the class using the full classpath. This will find 'fastutil' etc.
                    Class<?> clazz = Class.forName(internalClassName, false, classLoader);

                    if (internalClassName.contains("C2SPacket")
                            && !clazz.isInterface()
                            && !clazz.isEnum()
                            && !Modifier.isAbstract(clazz.getModifiers())) {
                        Constructor<?> bestConstructor =
                                Arrays.stream(clazz.getConstructors())
                                        .max(
                                                Comparator.comparingInt(
                                                        Constructor::getParameterCount))
                                        .orElse(null);

                        if (bestConstructor != null) {
                            String key = generatePacketKey(clazz);
                            List<ParamData> params =
                                    Arrays.stream(bestConstructor.getParameters())
                                            .map(
                                                    p ->
                                                            new ParamData(
                                                                    getSourceCodeTypeName(
                                                                            p.getType()),
                                                                    p.getType().getName(),
                                                                    p.getName()))
                                            .collect(Collectors.toList());
                            packetDataMap.put(
                                    key, new PacketData(key, clazz, params, bestConstructor));
                        }
                    }
                } catch (NoClassDefFoundError | ClassNotFoundException e) {
                    /*
                                         System.err.println("Skipping class " + internalClassName + ": " + e.getMessage());
                    */
                } catch (Throwable t) {
                    System.err.println(
                            "Unexpected error processing "
                                    + internalClassName
                                    + ": "
                                    + t.getMessage());
                }
            }
        }
        System.out.println(
                "Analysis complete. Found "
                        + packetDataMap.size()
                        + " constructible C2S packet classes.");
        return new ArrayList<>(packetDataMap.values());
    }

    private static String generateJavaClass(List<PacketData> allPacketData, String packageName) {
        StringBuilder sb = new StringBuilder();
        Set<String> imports =
                new TreeSet<>(
                        List.of(
                                "java.lang.reflect.Constructor",
                                "java.util.function.Consumer",
                                "net.minecraft.network.packet.Packet"));

        for (PacketData data : allPacketData) {
            getTopLevelClassNameForImport(data.packetClass.getName()).ifPresent(imports::add);
            for (ParamData param : data.params) {
                getTopLevelClassNameForImport(param.internalTypeName).ifPresent(imports::add);
            }
        }

        sb.append(String.format("package %s;\n\n", packageName));
        imports.forEach(imp -> sb.append(String.format("import %s;\n", imp)));
        sb.append("\n/**\n * Auto-generated by PacketRegistrarGenerator. DO NOT EDIT.\n */\n");
        sb.append(String.format("public final class %s {\n\n", GENERATED_CLASS_NAME));
        sb.append(
                "    public static void registerAll(Consumer<PacketMetadata.PacketInfo> registrar) {\n");

        for (PacketData data : allPacketData) {
            String classReferenceName = getSourceCodeTypeName(data.packetClass);
            String paramsForGetConstructor =
                    data.params.stream()
                            .map(p -> p.sourceCodeTypeName + ".class")
                            .collect(Collectors.joining(", "));
            String usageStringGen = generateUsageString(data.key, data.constructor);

            sb.append(
                    String.format(
                            "\n        // Packet: %s\n", data.packetClass.getCanonicalName()));
            sb.append("        try {\n");
            sb.append(
                    String.format(
                            "            Class<? extends Packet<?>> clazz = %s.class;\n",
                            classReferenceName));
            sb.append(
                    String.format(
                            "            Constructor<?> constructor = clazz.getConstructor(%s);\n",
                            paramsForGetConstructor));
            sb.append(
                    String.format(
                            "            String usage = \"%s\";\n",
                            usageStringGen.replace("\"", "\\\"")));
            sb.append(
                    String.format(
                            "            registrar.accept(new PacketMetadata.PacketInfo(\"%s\", clazz, constructor, usage));\n",
                            data.key));
            sb.append("        } catch (NoSuchMethodException e) {\n");
            sb.append(
                    String.format(
                            "            System.err.printf(\"Failed to register packet: %s (%%s)%%n\", e.getMessage());\n",
                            classReferenceName));
            sb.append("        }\n");
        }
        sb.append("    }\n}\n");
        return sb.toString();
    }

    private static String getSourceCodeTypeName(Class<?> type) {
        String canonicalName = type.getCanonicalName();
        if (canonicalName == null) return type.getSimpleName();
        if (type.getPackage() == null) return canonicalName;
        return canonicalName.substring(type.getPackage().getName().length() + 1);
    }

    private static Optional<String> getTopLevelClassNameForImport(String internalName) {
        String effectiveName = internalName;
        if (effectiveName.startsWith("[")) {
            if (effectiveName.charAt(effectiveName.lastIndexOf('[') + 1) == 'L') {
                effectiveName =
                        effectiveName.substring(
                                effectiveName.lastIndexOf('[') + 2, effectiveName.length() - 1);
            } else return Optional.empty(); // Primitive array
        }
        if (effectiveName.startsWith("java.lang.") || !effectiveName.contains("."))
            return Optional.empty();
        int innerClassIndex = effectiveName.indexOf('$');
        if (innerClassIndex != -1) return Optional.of(effectiveName.substring(0, innerClassIndex));
        return Optional.of(effectiveName);
    }

    private static String generatePacketKey(Class<?> clazz) {
        String pkgName = clazz.getPackageName();
        String pkgAbbr =
                pkgName.contains(".c2s.")
                        ? pkgName.substring(pkgName.indexOf(".c2s.") + 5)
                        : pkgName.substring(pkgName.lastIndexOf('.') + 1);
        String className = getSourceCodeTypeName(clazz).replace("C2SPacket", "");
        String snakeCaseName = className.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
        return pkgAbbr + "." + snakeCaseName;
    }

    private static String generateUsageString(String key, Constructor<?> constructor) {
        String params =
                Arrays.stream(constructor.getParameters())
                        .filter(p -> isUserProvided(p.getType()))
                        .map(p -> "<" + p.getName() + ":" + getSimpleTypeName(p.getType()) + ">")
                        .collect(Collectors.joining(" "));
        return String.format("%s %s", key, params).trim();
    }

    private static boolean isUserProvided(Class<?> type) {
        String name = type.getName();
        return !(name.equals("net.minecraft.item.ItemStack")
                || name.startsWith("it.unimi.dsi.fastutil")
                || name.equals("net.minecraft.util.hit.BlockHitResult"));
    }

    private static String getSimpleTypeName(Class<?> type) {
        if (!isUserProvided(type)) return type.getSimpleName() + "(auto)";
        String name = type.getName();
        if (name.equals("net.minecraft.util.math.BlockPos")) return "BlockPos(x y z)";
        if (name.equals("net.minecraft.util.math.Vec3d")) return "Vec3d(x y z)";
        return type.getSimpleName();
    }

    private static String derivePackageNameFrom(Path path) throws IOException {
        String pathStr = path.toAbsolutePath().toString().replace('\\', '/');
        int anchorIndex = pathStr.indexOf("src/main/java/");
        if (anchorIndex == -1) {
            anchorIndex = pathStr.indexOf("java/");
            if (anchorIndex == -1)
                throw new IOException("Could not derive package name from path: " + pathStr);
            return pathStr.substring(anchorIndex + 5).replace('/', '.');
        }
        return pathStr.substring(anchorIndex + 14).replace('/', '.');
    }

    private static void saveGeneratedFile(Path saveDir, String content) throws IOException {
        Path outputFile = saveDir.resolve(GENERATED_CLASS_NAME + ".java");
        Files.writeString(
                outputFile,
                content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("\nSUCCESS: File saved to: " + outputFile.toAbsolutePath());
    }
}
