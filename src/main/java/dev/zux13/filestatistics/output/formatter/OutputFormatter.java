package dev.zux13.filestatistics.output.formatter;

import dev.zux13.filestatistics.analysis.StatisticsAggregator;

public interface OutputFormatter {
    String format(StatisticsAggregator aggregator);
}