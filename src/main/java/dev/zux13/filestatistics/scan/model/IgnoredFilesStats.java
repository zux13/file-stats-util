package dev.zux13.filestatistics.scan.model;

import lombok.Getter;

@Getter
public class IgnoredFilesStats {
    private long ignoredByGitignore;
    private long ignoredByExtension;
    private long ignoredBinaryOrEncoding;

    public void incrementIgnoredByGitignore() {
        ignoredByGitignore++;
    }

    public void incrementIgnoredByExtension() {
        ignoredByExtension++;
    }

    public void incrementIgnoredBinaryOrEncoding() {
        ignoredBinaryOrEncoding++;
    }
}
