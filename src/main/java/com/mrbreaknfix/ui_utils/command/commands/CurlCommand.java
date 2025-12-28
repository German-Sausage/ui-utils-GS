/**
 * Copyright (c) TheBreakery by MrBreakNFix
 *
 * @file CurlCommand.java
 */
package com.mrbreaknfix.ui_utils.command.commands;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mrbreaknfix.ui_utils.command.ArgumentNode;
import com.mrbreaknfix.ui_utils.command.BaseCommand;
import com.mrbreaknfix.ui_utils.command.CommandResult;

public class CurlCommand extends BaseCommand {
    // todo: use in breakery for mojang api auth demo
    @Override
    @SuppressWarnings("unchecked")
    protected CommandResult<?> executeParsed(List<Object> rawArgs) {
        List<String> args =
                ((List<String>) (List<?>) rawArgs)
                        .stream()
                                .filter(arg -> !arg.equals("\\")) // filter out shell backslashes
                                .toList();

        String url = null;
        String method = "GET";
        String rawData = null;
        String userAgent = null;
        String basicAuth = null;
        String outputFileStr = null;
        long connectTimeout = 20;
        boolean followRedirects = false;
        boolean includeHeaders = false;
        boolean isVerbose = false;
        boolean insecure = false;
        List<String> headers = new ArrayList<>();
        List<String> dataUrlEncoded = new ArrayList<>();
        List<String> formParts = new ArrayList<>();

        try {
            for (int i = 0; i < args.size(); i++) {
                String arg = args.get(i);
                switch (arg) {
                    case "-X", "--request":
                    case "-H", "--header":
                    case "-d", "--data", "--data-raw":
                    case "--data-urlencode":
                    case "-F", "--form":
                    case "-o", "--output":
                    case "-A", "--user-agent":
                    case "-u", "--user":
                    case "--connect-timeout":
                        if (i + 1 >= args.size()) {
                            return CommandResult.of(
                                    false,
                                    "Flag '" + arg + "' requires a value, but none was provided.");
                        }
                        String value = args.get(++i);
                        switch (arg) {
                            case "-X", "--request" -> method = value;
                            case "-H", "--header" -> headers.add(value);
                            case "-d", "--data", "--data-raw" -> rawData = value;
                            case "--data-urlencode" -> dataUrlEncoded.add(value);
                            case "-F", "--form" -> formParts.add(value);
                            case "-o", "--output" -> outputFileStr = value;
                            case "-A", "--user-agent" -> userAgent = value;
                            case "-u", "--user" -> basicAuth = value;
                            case "--connect-timeout" -> connectTimeout = Long.parseLong(value);
                        }
                        break;

                    case "-L", "--location":
                        followRedirects = true;
                        break;
                    case "-i", "--include":
                        includeHeaders = true;
                        break;
                    case "-v", "--verbose":
                        isVerbose = true;
                        break;
                    case "-k", "--insecure":
                        insecure = true;
                        break;

                    default:
                        if (arg.startsWith("-")) {
                            return CommandResult.of(false, "Unknown flag: " + arg);
                        }
                        if (url != null) {
                            return CommandResult.of(
                                    false, "Multiple URLs specified. Please provide only one.");
                        }
                        url = arg;
                        break;
                }
            }
        } catch (NumberFormatException e) {
            return CommandResult.of(false, "Invalid numeric value for a flag.");
        }

        if (url == null) {
            return CommandResult.of(false, "No URL specified. See `help curl` for usage.");
        }

        Path outputFile = outputFileStr != null ? Paths.get(outputFileStr) : null;

        HttpClient.Builder clientBuilder =
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_2)
                        .connectTimeout(Duration.ofSeconds(connectTimeout));
        if (followRedirects) clientBuilder.followRedirects(HttpClient.Redirect.NORMAL);
        if (insecure) clientBuilder.sslContext(createInsecureSslContext());
        HttpClient client = clientBuilder.build();

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(new URI(url));
            if (userAgent != null) requestBuilder.header("User-Agent", userAgent);
            if (basicAuth != null)
                requestBuilder.header(
                        "Authorization",
                        "Basic "
                                + Base64.getEncoder()
                                        .encodeToString(
                                                basicAuth.getBytes(StandardCharsets.UTF_8)));
            for (String header : headers) {
                String[] parts = header.split(":", 2);
                if (parts.length == 2) requestBuilder.header(parts[0].trim(), parts[1].trim());
            }
            if (!formParts.isEmpty()) {
                String boundary = "------------" + UUID.randomUUID().toString();
                List<byte[]> bodyByteArrays = new ArrayList<>();
                for (String part : formParts) {
                    bodyByteArrays.add(("--" + boundary + "\r\n").getBytes());
                    String[] kv = part.split("=", 2);
                    if (kv.length > 1 && kv[1].startsWith("@")) {
                        Path filePath = Paths.get(kv[1].substring(1));
                        String fileName = filePath.getFileName().toString();
                        String mimeType =
                                Files.probeContentType(filePath) != null
                                        ? Files.probeContentType(filePath)
                                        : "application/octet-stream";
                        bodyByteArrays.add(
                                ("Content-Disposition: form-data; name=\""
                                                + kv[0]
                                                + "\"; filename=\""
                                                + fileName
                                                + "\"\r\n")
                                        .getBytes());
                        bodyByteArrays.add(("Content-Type: " + mimeType + "\r\n\r\n").getBytes());
                        bodyByteArrays.add(Files.readAllBytes(filePath));
                        bodyByteArrays.add("\r\n".getBytes());
                    } else {
                        bodyByteArrays.add(
                                ("Content-Disposition: form-data; name=\"" + kv[0] + "\"\r\n\r\n")
                                        .getBytes());
                        if (kv.length > 1) bodyByteArrays.add((kv[1] + "\r\n").getBytes());
                    }
                }
                bodyByteArrays.add(("--" + boundary + "--\r\n").getBytes());
                requestBuilder.header("Content-Type", "multipart/form-data; boundary=" + boundary);
                requestBuilder.method(
                        method.toUpperCase(),
                        HttpRequest.BodyPublishers.ofByteArrays(bodyByteArrays));
            } else if (!dataUrlEncoded.isEmpty()) {
                String encodedBody =
                        dataUrlEncoded.stream()
                                .map(
                                        s -> {
                                            String[] parts = s.split("=", 2);
                                            return URLEncoder.encode(
                                                            parts[0], StandardCharsets.UTF_8)
                                                    + "="
                                                    + URLEncoder.encode(
                                                            parts.length > 1 ? parts[1] : "",
                                                            StandardCharsets.UTF_8);
                                        })
                                .collect(Collectors.joining("&"));
                if (requestBuilder.build().headers().firstValue("Content-Type").isEmpty())
                    requestBuilder.header("Content-Type", "application/x-www-form-urlencoded");
                requestBuilder.method(
                        method.toUpperCase(), HttpRequest.BodyPublishers.ofString(encodedBody));
            } else if (rawData != null) {
                requestBuilder.method(
                        method.toUpperCase(), HttpRequest.BodyPublishers.ofString(rawData));
            } else {
                requestBuilder.method(method.toUpperCase(), HttpRequest.BodyPublishers.noBody());
            }
            HttpRequest request = requestBuilder.build();
            HttpResponse<?> response =
                    (outputFile != null)
                            ? client.send(request, HttpResponse.BodyHandlers.ofFile(outputFile))
                            : client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() >= 200 && response.statusCode() < 400;
            String finalOutput;
            JsonElement jsonBody = null;
            StringBuilder outputBuilder = new StringBuilder();
            if (isVerbose) {
                outputBuilder.append("*   Trying ").append(request.uri().getHost()).append("...\n");
                outputBuilder
                        .append("> ")
                        .append(request.method())
                        .append(" ")
                        .append(request.uri().getPath())
                        .append(" HTTP/")
                        .append(client.version())
                        .append("\n");
                outputBuilder.append("> Host: ").append(request.uri().getHost()).append("\n");
                request.headers()
                        .map()
                        .forEach(
                                (k, v) ->
                                        outputBuilder
                                                .append("> ")
                                                .append(k)
                                                .append(": ")
                                                .append(String.join(", ", v))
                                                .append("\n"));
                outputBuilder.append("\n");
                outputBuilder
                        .append("< HTTP/")
                        .append(response.version())
                        .append(" ")
                        .append(response.statusCode())
                        .append("\n");
                response.headers()
                        .map()
                        .forEach(
                                (k, v) ->
                                        outputBuilder
                                                .append("< ")
                                                .append(k)
                                                .append(": ")
                                                .append(String.join(", ", v))
                                                .append("\n"));
                outputBuilder.append("\n");
            }
            if (includeHeaders) {
                outputBuilder
                        .append("HTTP/")
                        .append(response.version())
                        .append(" ")
                        .append(response.statusCode())
                        .append("\n");
                response.headers()
                        .map()
                        .forEach(
                                (k, v) ->
                                        outputBuilder
                                                .append(k)
                                                .append(": ")
                                                .append(String.join(", ", v))
                                                .append("\n"));
                outputBuilder.append("\n");
            }
            if (outputFile != null) {
                finalOutput =
                        "Request finished with status "
                                + response.statusCode()
                                + ". Output saved to "
                                + outputFile.toAbsolutePath();
            } else {
                String body = response.body().toString();
                outputBuilder.append(body);
                finalOutput = outputBuilder.toString();
                try {
                    jsonBody = JsonParser.parseString(body);
                } catch (JsonSyntaxException e) {
                    /* Not JSON */
                }
            }
            return CommandResult.of(success, finalOutput, null, jsonBody);
        } catch (Exception e) {
            return CommandResult.of(
                    false,
                    "Error during HTTP request: "
                            + e.getClass().getSimpleName()
                            + " - "
                            + e.getMessage());
        }
    }

    private SSLContext createInsecureSslContext() {
        try {
            TrustManager[] trustAllCerts =
                    new TrustManager[] {
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(
                                    X509Certificate[] certs, String authType) {}

                            public void checkServerTrusted(
                                    X509Certificate[] certs, String authType) {}
                        }
                    };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create insecure SSL context", e);
        }
    }

    @Override
    public Map<String, ArgumentNode[]> getFlagPools() {
        ArgumentNode[] flags =
                new ArgumentNode[] {
                    ArgumentNode.literal("-X", "Alias for --request.")
                            .then(ArgumentNode.argument("<method>", "e.g., POST, DELETE")),
                    ArgumentNode.literal("--request", "Specify request method.")
                            .then(ArgumentNode.argument("<method>", "e.g., POST, DELETE")),
                    ArgumentNode.literal("-H", "Alias for --header.")
                            .then(
                                    ArgumentNode.argument(
                                            "<header>", "'Content-Type: application/json'")),
                    ArgumentNode.literal("--header", "Add a request header.")
                            .then(
                                    ArgumentNode.argument(
                                            "<header>", "'Content-Type: application/json'")),
                    ArgumentNode.literal("-d", "Alias for --data-raw.")
                            .then(ArgumentNode.argument("<data>", "The HTTP POST data.")),
                    ArgumentNode.literal("--data", "Alias for --data-raw.")
                            .then(ArgumentNode.argument("<data>", "The HTTP POST data.")),
                    ArgumentNode.literal("--data-raw", "Raw HTTP POST data.")
                            .then(ArgumentNode.argument("<data>", "The raw data to send.")),
                    ArgumentNode.literal("--data-urlencode", "URL-encoded POST data.")
                            .then(ArgumentNode.argument("<data>", "e.g., 'name=user'")),
                    ArgumentNode.literal("-F", "Alias for --form.")
                            .then(
                                    ArgumentNode.argument(
                                            "<data>", "e.g., 'name=user' or 'file=@path'")),
                    ArgumentNode.literal("--form", "Specify multipart POST data.")
                            .then(
                                    ArgumentNode.argument(
                                            "<data>", "e.g., 'name=user' or 'file=@path'")),
                    ArgumentNode.literal("-o", "Write output to file.")
                            .then(ArgumentNode.argument("<file>", "The file path.")),
                    ArgumentNode.literal("--output", "Write output to file.")
                            .then(ArgumentNode.argument("<file>", "The file path.")),
                    ArgumentNode.literal("-A", "Set User-Agent.")
                            .then(ArgumentNode.argument("<agent>", "The User-Agent string.")),
                    ArgumentNode.literal("--user-agent", "Set User-Agent.")
                            .then(ArgumentNode.argument("<agent>", "The User-Agent string.")),
                    ArgumentNode.literal("-u", "Set user and password.")
                            .then(ArgumentNode.argument("<user:password>", "The credentials.")),
                    ArgumentNode.literal("--user", "Set user and password.")
                            .then(ArgumentNode.argument("<user:password>", "The credentials.")),
                    ArgumentNode.literal("--connect-timeout", "Connection timeout.")
                            .then(ArgumentNode.argument("<seconds>", "Timeout value.")),
                    ArgumentNode.literal("-L", "Follow redirects."),
                    ArgumentNode.literal("--location", "Follow redirects."),
                    ArgumentNode.literal("-i", "Include response headers."),
                    ArgumentNode.literal("--include", "Include response headers."),
                    ArgumentNode.literal("-v", "Make the operation more talkative."),
                    ArgumentNode.literal("--verbose", "Make the operation more talkative."),
                    ArgumentNode.literal("-k", "Allow insecure connections."),
                    ArgumentNode.literal("--insecure", "Allow insecure connections.")
                };
        return Map.of("curlFlags", flags);
    }

    @Override
    public ArgumentNode getArgumentSchema() {
        return ArgumentNode.literal("curl", "Transfer data from or to a server.")
                .then(
                        ArgumentNode.argument("<url>", "The URL to interact with.")
                                .then(
                                        ArgumentNode.flagSet(
                                                "curlFlags",
                                                "Optional flags for the curl command.")));
    }

    @Override
    public String manual() {
        return """
                NAME
                    curl - Transfer data from or to a server

                SYNOPSIS
                    curl [options] <url> [options]

                DESCRIPTION
                    A utility to transfer data, supporting various options via named flags that can be in any order. The URL is identified as the single non-flag argument.

                OPTIONS
                    -X, --request <method>
                        The request method to use (e.g., GET, POST, PUT, DELETE).

                    -H, --header <header>
                        Extra header for the request. Specify multiple times for multiple headers.
                        Example: curl <url> -H "Content-Type: application/json" -H "Accept: */*"

                    -d, --data, --data-raw <data>
                        Sends the specified data in the request body.

                    --data-urlencode <data>
                        Sends URL-encoded data. Specify multiple times for multiple fields.
                        Example: curl <url> --data-urlencode "name=John Doe" --data-urlencode "project=curl"

                    -F, --form <name=content>
                        Specify multipart form data. For file uploads, use '@' prefix.
                        Example: curl <url> -F "name=John" -F "cv=@/path/to/resume.pdf"

                    -L, --location
                        Follow server redirects.

                    -i, --include
                        Include the HTTP-response headers in the output.

                    -v, --verbose
                        Provides detailed information about the transfer.

                    -o, --output <file>
                        Write response body to a file instead of the console.

                    -A, --user-agent <string>
                        Specify the User-Agent string to send to the server.

                    -u, --user <user:password>
                        Specify the user and password for server authentication.

                    -k, --insecure
                        Allows connections to SSL sites without certs. Use with caution.

                    --connect-timeout <seconds>
                        Maximum time in seconds that you allow the connection to the server to take.

                EXAMPLES
                    # A GET request with the URL at the end
                    curl -v --location https://www.google.com

                    # POST JSON data, with the URL in the middle
                    curl -X POST "https://httpbin.org/post" --data-raw "{\\"name\\":\\"Test\\"}" -H "Content-Type: application/json"

                    # Upload a file using a form
                    curl "https://httpbin.org/post" -F "user=testuser" -F "upload=@/home/user/avatar.jpg"
                """;
    }
}
