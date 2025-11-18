package dev.zux13.filestatistics.processing;

import dev.zux13.filestatistics.analysis.model.AnalysisResult;
import dev.zux13.filestatistics.scan.model.IgnoredFilesStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisServiceTest {

    @TempDir
    Path tempDir;

    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisService(4);
    }

    @Test
    @DisplayName("Processes files and aggregates statistics")
    void shouldAggregateStatistics_whenProcessingMultipleFiles() throws Exception {
        Path javaFile = Files.createFile(tempDir.resolve("Test.java"));
        Files.writeString(javaFile, "class Test {} // comment");

        Path txtFile = Files.createFile(tempDir.resolve("notes.txt"));
        Files.writeString(txtFile, "some text");

        List<Path> filesToProcess = List.of(javaFile, txtFile);
        IgnoredFilesStats ignoredFilesStats = new IgnoredFilesStats();

        AnalysisResult result = analysisService.processFiles(filesToProcess, ignoredFilesStats);

        assertAll(
                () -> assertEquals(2, result.extensionStatistics().size()),
                () -> assertTrue(result.extensionStatistics().containsKey("java")),
                () -> assertTrue(result.extensionStatistics().containsKey("txt")),
                () -> assertEquals(1, result.extensionStatistics().get("java").getFileCount()),
                () -> assertEquals(0, result.extensionStatistics().get("java").getTotalCommentLines()),
                () -> assertEquals(1, result.extensionStatistics().get("txt").getFileCount()),
                () -> assertEquals(0, result.extensionStatistics().get("txt").getTotalCommentLines()),
                () -> assertEquals(0, ignoredFilesStats.getIgnoredBinaryOrEncoding())
        );
    }

    @Test
    @DisplayName("Returns empty statistics when list of files is empty")
    void shouldReturnEmptyStatistics_whenFilesListIsEmpty() {
        List<Path> filesToProcess = Collections.emptyList();
        IgnoredFilesStats ignoredFilesStats = new IgnoredFilesStats();

        AnalysisResult result = analysisService.processFiles(filesToProcess, ignoredFilesStats);

        assertTrue(result.extensionStatistics().isEmpty());
        assertEquals(0, ignoredFilesStats.getIgnoredBinaryOrEncoding());
    }

    @Test
    @DisplayName("Skips binary files and increments ignore counter")
    void shouldIgnoreBinaryFiles_whenEncounteredDuringProcessing() throws Exception {
        Path binaryFile = Files.createFile(tempDir.resolve("archive.zip"));
        Files.write(binaryFile, new byte[]{
                (byte) 0x50, (byte) 0x4B, (byte) 0x00, (byte) 0x01, (byte) 0x02
        });

        Path textFile = Files.createFile(tempDir.resolve("another.txt"));
        Files.writeString(textFile, "valid text");

        List<Path> filesToProcess = List.of(binaryFile, textFile);
        IgnoredFilesStats ignoredFilesStats = new IgnoredFilesStats();

        AnalysisResult result = analysisService.processFiles(filesToProcess, ignoredFilesStats);

        assertAll(
                () -> assertEquals(1, result.extensionStatistics().size()),
                () -> assertTrue(result.extensionStatistics().containsKey("txt")),
                () -> assertEquals(1, ignoredFilesStats.getIgnoredBinaryOrEncoding())
        );
    }
}
