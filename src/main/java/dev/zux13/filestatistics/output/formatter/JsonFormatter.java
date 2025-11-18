package dev.zux13.filestatistics.output.formatter;

import dev.zux13.filestatistics.analysis.StatisticsAggregator;
import dev.zux13.filestatistics.output.dto.OutputStatistics;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class JsonFormatter implements OutputFormatter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String format(StatisticsAggregator aggregator) {
        try {
            OutputStatistics outputStats = aggregator.getOutputStatistics();
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(outputStats);
        } catch (Exception e) {
            log.error("Error during JSON serialization: {}", e.getMessage());
            return "{}";
        }
    }
}