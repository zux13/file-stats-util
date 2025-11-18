package dev.zux13.filestatistics.cli;

import dev.zux13.filestatistics.output.ConsolePrinter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArgumentParser {

    private static final String HELP_FLAG_SHORT = "-h";
    private static final String HELP_FLAG_LONG = "--help";
    private static final Set<String> VALID_OUTPUT_FORMATS = Set.of("plain", "xml", "json");

    private String pathStr;
    private boolean recursive;
    private int maxDepth;
    private int numThreads;
    private List<String> includeExtensions;
    private List<String> excludeExtensions;
    private boolean useGitIgnore;
    private String outputFormat;
    private boolean verbose;

    public ArgumentParser() {
        reset();
    }

    private void reset() {
        pathStr = null;
        recursive = false;
        maxDepth = -1;
        numThreads = 1;
        includeExtensions = new ArrayList<>();
        excludeExtensions = new ArrayList<>();
        useGitIgnore = false;
        outputFormat = "plain";
        verbose = false;
    }

    public Config parse(String[] args) throws IllegalArgumentException {
        if (args.length == 0 || Arrays.asList(args).contains(HELP_FLAG_SHORT) || Arrays.asList(args).contains(HELP_FLAG_LONG)) {
            ConsolePrinter.printHelp();
            System.exit(0);
        }

        reset();

        for (String arg : args) {
            parseArgument(arg);
        }

        return buildConfig();
    }

    private void parseArgument(String arg) {
        if (arg.startsWith("--")) {
            parseNamedArgument(arg);
        } else if (arg.startsWith("-")) {
            parseShortArgument(arg);
        } else {
            parsePathArgument(arg);
        }
    }

    private void parseNamedArgument(String arg) {
        String[] parts = arg.substring(2).split("=", 2);
        String flag = parts[0];
        String value = parts.length > 1 ? parts[1] : null;

        switch (flag) {
            case "recursive" -> handleRecursiveFlag(value);
            case "max-depth" -> handleMaxDepthFlag(value);
            case "thread" -> handleThreadFlag(value);
            case "include-ext" -> handleIncludeExtFlag(value);
            case "exclude-ext" -> handleExcludeExtFlag(value);
            case "git-ignore" -> handleGitIgnoreFlag(value);
            case "output" -> handleOutputFlag(value);
            case "verbose" -> handleVerboseFlag(value);
            default -> throw new IllegalArgumentException("Unknown argument: " + arg);
        }
    }

    private void parseShortArgument(String arg) {
        if (arg.equals(HELP_FLAG_SHORT)) {
            ConsolePrinter.printHelp();
            System.exit(0);
        } else {
            throw new IllegalArgumentException("Unknown short argument: " + arg);
        }
    }

    private void parsePathArgument(String arg) {
        if (this.pathStr == null) {
            this.pathStr = arg;
        } else {
            String message = String.format(
                    "Invalid argument: %s. Only one directory path is expected.", arg
            );
            throw new IllegalArgumentException(message);
        }
    }

    private void handleRecursiveFlag(String value) {
        if (value != null) {
            throw new IllegalArgumentException("The --recursive flag does not take a value. Usage: --recursive");
        }
        this.recursive = true;
    }

    private void handleMaxDepthFlag(String value) {
        if (value == null) {
            throw new IllegalArgumentException("The --max-depth parameter requires a numeric value. Usage: --max-depth=<number>");
        }
        try {
            int depth = Integer.parseInt(value);
            if (depth < 0) {
                throw new NumberFormatException();
            }
            this.maxDepth = depth;
        } catch (NumberFormatException e) {
            String message = String.format(
                    "Invalid value for --max-depth: '%s'. A positive integer is expected.", value
            );
            throw new IllegalArgumentException(message);
        }
    }

    private void handleThreadFlag(String value) {
        if (value == null) {
            throw new IllegalArgumentException("The --thread parameter requires a numeric value. Usage: --thread=<number>");
        }
        try {
            int threads = Integer.parseInt(value);
            if (threads <= 0) {
                throw new NumberFormatException();
            }
            this.numThreads = threads;
        } catch (NumberFormatException e) {
            String message = String.format(
                    "Invalid value for --thread: '%s'. A positive integer is expected.", value
            );
            throw new IllegalArgumentException(message);
        }
    }

    private void handleIncludeExtFlag(String value) {
        if (value == null) {
            throw new IllegalArgumentException("The --include-ext parameter requires a list of extensions. Usage: --include-ext=<ext1,ext2,..>");
        }
        for (String ext : value.split(",")) {
            String trimmedExt = ext.trim();
            if (!trimmedExt.isEmpty()) {
                this.includeExtensions.add(trimmedExt.toLowerCase());
            }
        }
    }

    private void handleExcludeExtFlag(String value) {
        if (value == null) {
            throw new IllegalArgumentException("The --exclude-ext parameter requires a list of extensions. Usage: --exclude-ext=<ext1,ext2,..>");
        }
        for (String ext : value.split(",")) {
            String trimmedExt = ext.trim();
            if (!trimmedExt.isEmpty()) {
                this.excludeExtensions.add(trimmedExt.toLowerCase());
            }
        }
    }

    private void handleGitIgnoreFlag(String value) {
        if (value != null) {
            throw new IllegalArgumentException("The --git-ignore flag does not take a value. Usage: --git-ignore");
        }
        this.useGitIgnore = true;
    }

    private void handleOutputFlag(String value) {
        if (value == null) {
            throw new IllegalArgumentException("The --output parameter requires a value. Usage: --output=<plain,xml,json>");
        }
        if (!VALID_OUTPUT_FORMATS.contains(value.toLowerCase())) {
            String allowed = String.join(", ", VALID_OUTPUT_FORMATS);
            String message = String.format(
                    "Invalid value for --output: '%s'. Allowed values are: %s", value, allowed
            );
            throw new IllegalArgumentException(message);
        }
        this.outputFormat = value.toLowerCase();
    }

    private void handleVerboseFlag(String value) {
        if (value != null) {
            throw new IllegalArgumentException("The --verbose flag does not take a value. Usage: --verbose");
        }
        this.verbose = true;
    }

    private Config buildConfig() {
        validatePath();
        validateExtensions();
        Path path = Paths.get(pathStr).toAbsolutePath().normalize();
        return new Config(path, recursive, maxDepth, numThreads, includeExtensions, excludeExtensions, useGitIgnore, verbose, outputFormat);
    }

    private void validatePath() {
        if (pathStr == null) {
            throw new IllegalArgumentException("A required argument is missing: <path>");
        }
        Path path = Paths.get(pathStr);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("The specified path does not exist: " + pathStr);
        }
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("The specified path is not a directory: " + pathStr);
        }
    }

    private void validateExtensions() {
        Set<String> includeSet = new HashSet<>(includeExtensions);
        includeSet.retainAll(new HashSet<>(excludeExtensions));
        if (!includeSet.isEmpty()) {
            throw new IllegalArgumentException("Extensions cannot be both included and excluded: " + includeSet);
        }
    }

}