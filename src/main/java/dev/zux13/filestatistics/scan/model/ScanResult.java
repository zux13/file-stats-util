package dev.zux13.filestatistics.scan.model;

import java.nio.file.Path;
import java.util.List;

public record ScanResult(List<Path> filesToAnalyze, IgnoredFilesStats ignoredFilesStats) {}
