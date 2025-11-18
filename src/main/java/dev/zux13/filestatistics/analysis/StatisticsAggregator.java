package dev.zux13.filestatistics.analysis;

import dev.zux13.filestatistics.analysis.model.ExtensionStatistics;
import dev.zux13.filestatistics.output.dto.ExtensionStatDto;
import dev.zux13.filestatistics.output.dto.OutputStatistics;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class StatisticsAggregator {

    private Map<String, ExtensionStatistics> statisticsByExtension;
    private long ignoredByGitignoreCount;
    private long ignoredByExtensionCount;
    private long ignoredBinaryOrEncodingCount;

    public OutputStatistics getOutputStatistics() {
        if (statisticsByExtension == null) {
            return new OutputStatistics(List.of());
        }
        List<ExtensionStatDto> dtoList = statisticsByExtension.entrySet().stream()
                .map(entry -> new ExtensionStatDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ExtensionStatDto::getName))
                .toList();

        return new OutputStatistics(dtoList);
    }

}