package dev.zux13.filestatistics.cli;

import java.nio.file.Path;
import java.util.List;

public record Config(
        Path path,
        boolean recursive,
        int maxDepth,
        int numThreads,
        List<String> includeExtensions,
        List<String> excludeExtensions,
        boolean useGitIgnore,
        boolean verbose,
        String outputFormat
) {}