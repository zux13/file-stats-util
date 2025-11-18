package dev.zux13.filestatistics.output.formatter;

import dev.zux13.filestatistics.analysis.StatisticsAggregator;
import dev.zux13.filestatistics.analysis.model.ExtensionStatistics;

import java.util.Map;
import java.util.stream.Collectors;

public class PlaintextFormatter implements OutputFormatter {

    @Override
    public String format(StatisticsAggregator aggregator) {
        Map<String, ExtensionStatistics> statsMap = aggregator.getStatisticsByExtension();

        return statsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(this::formatExtension)
                .collect(Collectors.joining("\n---\n"));
    }

    private String formatExtension(Map.Entry<String, ExtensionStatistics> entry) {
        String extension = entry.getKey().isEmpty()
                ? "Без расширения"
                : entry.getKey();

        ExtensionStatistics stats = entry.getValue();

        String template = """
            Расширение: %s
              Количество файлов: %d
              Размер в байтах: %d
              Количество строк всего: %d
              Количество не пустых строк: %d
              Количество строк с комментариями: %d
            """;

        return template.formatted(
                extension,
                stats.getFileCount(),
                stats.getTotalSizeInBytes(),
                stats.getTotalLines(),
                stats.getTotalNonEmptyLines(),
                stats.getTotalCommentLines()
        );
    }
}