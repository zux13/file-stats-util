package dev.zux13.filestatistics.scan;

import dev.zux13.filestatistics.cli.Config;
import dev.zux13.filestatistics.scan.model.IgnoredFilesStats;
import dev.zux13.filestatistics.scan.model.ScanResult;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileScanner {

    private final Config config;
    private final GitIgnoreManager gitIgnoreManager;

    public FileScanner(Config config, GitIgnoreManager gitIgnoreManager) {
        this.config = config;
        this.gitIgnoreManager = gitIgnoreManager;
    }

    public ScanResult scanFiles() throws IOException {
        IgnoredFilesStats ignoredFilesStats = new IgnoredFilesStats();
        List<Path> files;

        Predicate<Path> combinedFilter = ((Predicate<Path>) path -> !path.getFileName().toString().equals(".gitignore"))
                .and(gitIgnoreFilter(ignoredFilesStats))
                .and(extensionFilter(ignoredFilesStats));

        try (Stream<Path> walk = Files.walk(config.path(), resolveMaxDepth(), FileVisitOption.FOLLOW_LINKS)) {
            files = walk
                    .filter(Files::isRegularFile)
                    .filter(combinedFilter)
                    .toList();
        }

        return new ScanResult(files, ignoredFilesStats);
    }

    private int resolveMaxDepth() {
        if (!config.recursive()) {
            return 1;
        }
        return config.maxDepth() == -1
                ? Integer.MAX_VALUE
                : config.maxDepth();
    }

    private Predicate<Path> gitIgnoreFilter(IgnoredFilesStats stats) {
        return path -> {
            if (gitIgnoreManager != null && gitIgnoreManager.shouldBeIgnored(path)) {
                stats.incrementIgnoredByGitignore();
                return false;
            }
            return true;
        };
    }

    private Predicate<Path> extensionFilter(IgnoredFilesStats stats) {
        return path -> {
            String extension = getExtension(path);
            if (isExcludedByExtension(extension)) {
                stats.incrementIgnoredByExtension();
                return false;
            }
            return true;
        };
    }

    private boolean isExcludedByExtension(String extension) {
        if (extension == null) {
            return !config.includeExtensions().isEmpty() || config.excludeExtensions().contains("");
        }

        boolean isIncluded = config.includeExtensions().isEmpty() || config.includeExtensions().contains(extension);
        boolean isExcluded = config.excludeExtensions().contains(extension);

        return !isIncluded || isExcluded;
    }

    private String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return null;
    }
}