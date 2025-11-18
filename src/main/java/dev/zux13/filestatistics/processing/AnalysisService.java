package dev.zux13.filestatistics.processing;

import dev.zux13.filestatistics.analysis.CommentAnalyzer;
import dev.zux13.filestatistics.analysis.FileAnalyzer;
import dev.zux13.filestatistics.analysis.model.AnalysisResult;
import dev.zux13.filestatistics.analysis.model.ExtensionStatistics;
import dev.zux13.filestatistics.analysis.model.FileStatistics;

import dev.zux13.filestatistics.scan.model.IgnoredFilesStats;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class AnalysisService {

    private final int numThreads;
    private final FileAnalyzer fileAnalyzer;

    public AnalysisService(int numThreads) {
        this.numThreads = numThreads;
        this.fileAnalyzer = new FileAnalyzer(new CommentAnalyzer());
    }

    public AnalysisResult processFiles(List<Path> filesToProcess, IgnoredFilesStats ignoredFilesStats) {

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        ConcurrentMap<String, ExtensionStatistics> extensionStatisticsMap = new ConcurrentHashMap<>();

        for (Path file : filesToProcess) {
            executor.submit(analyzeFileTask(file, extensionStatisticsMap, ignoredFilesStats));
        }

        shutdownAndAwaitTermination(executor);

        return new AnalysisResult(extensionStatisticsMap, ignoredFilesStats.getIgnoredBinaryOrEncoding());
    }

    private void shutdownAndAwaitTermination(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private Runnable analyzeFileTask(Path file,
                                     ConcurrentMap<String, ExtensionStatistics> extensionStatisticsMap,
                                     IgnoredFilesStats ignoredFilesStats) {
        return () -> {
            try {
                FileStatistics stats = fileAnalyzer.analyzeFile(file);
                if (stats != null) {
                    String extension = getExtension(file);
                    extensionStatisticsMap.computeIfAbsent(extension, k -> new ExtensionStatistics())
                            .addFileStats(stats);
                } else {
                    ignoredFilesStats.incrementIgnoredBinaryOrEncoding();
                }
            } catch (IOException e) {
                log.error("Failed to analyze file {}: {}", file, e.getMessage());
            }
        };
    }

    private String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
}
