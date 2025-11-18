package dev.zux13.filestatistics.output.dto;

import dev.zux13.filestatistics.analysis.model.ExtensionStatistics;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ExtensionStatDto {

    private String name;
    private ExtensionStatistics stats;

    public ExtensionStatDto(String name, ExtensionStatistics stats) {
        this.name = name;
        this.stats = stats;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlElement
    public long getFileCount() {
        return stats.getFileCount();
    }

    @XmlElement
    public long getTotalSizeInBytes() {
        return stats.getTotalSizeInBytes();
    }

    @XmlElement
    public long getTotalLines() {
        return stats.getTotalLines();
    }

    @XmlElement
    public long getTotalNonEmptyLines() {
        return stats.getTotalNonEmptyLines();
    }

    @XmlElement
    public long getTotalCommentLines() {
        return stats.getTotalCommentLines();
    }
}