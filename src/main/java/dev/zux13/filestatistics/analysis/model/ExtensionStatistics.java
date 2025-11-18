package dev.zux13.filestatistics.analysis.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "extension")
public class ExtensionStatistics {
    private long fileCount = 0;
    private long totalSizeInBytes = 0;
    private long totalLines = 0;
    private long totalNonEmptyLines = 0;
    private long totalCommentLines = 0;

    public void addFileStats(FileStatistics stats) {
        this.fileCount++;
        this.totalSizeInBytes += stats.sizeInBytes();
        this.totalLines += stats.totalLines();
        this.totalNonEmptyLines += stats.nonEmptyLines();
        this.totalCommentLines += stats.commentLines();
    }

    @XmlElement
    public long getFileCount() {
        return fileCount;
    }

    @XmlElement
    public long getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    @XmlElement
    public long getTotalLines() {
        return totalLines;
    }

    @XmlElement
    public long getTotalNonEmptyLines() {
        return totalNonEmptyLines;
    }

    @XmlElement
    public long getTotalCommentLines() {
        return totalCommentLines;
    }
}