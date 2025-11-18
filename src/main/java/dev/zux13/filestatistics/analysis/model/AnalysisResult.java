package dev.zux13.filestatistics.analysis.model;

import java.util.Map;

public record AnalysisResult(
        Map<String, ExtensionStatistics> extensionStatistics,
        long ignoredBinaryOrEncodingCount
) {}
