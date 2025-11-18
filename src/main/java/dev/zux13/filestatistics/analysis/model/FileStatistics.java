package dev.zux13.filestatistics.analysis.model;

public record FileStatistics(
        long sizeInBytes,
        long totalLines,
        long nonEmptyLines,
        long commentLines
) {}