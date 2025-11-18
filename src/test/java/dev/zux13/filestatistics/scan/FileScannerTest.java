package dev.zux13.filestatistics.scan;

import dev.zux13.filestatistics.cli.Config;
import dev.zux13.filestatistics.scan.model.ScanResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileScannerTest {

    @TempDir
    Path tempDir;

    private Path file1;
    private Path file2;
    private Path file3;
    private Path file4;

    @BeforeEach
    void setUp() throws IOException {
        Path dir1 = Files.createDirectory(tempDir.resolve("dir1"));
        Path dir2 = Files.createDirectory(dir1.resolve("dir2"));

        file1 = Files.createFile(tempDir.resolve("file1.java"));
        file2 = Files.createFile(tempDir.resolve("file2.log"));
        file3 = Files.createFile(dir1.resolve("file3.java"));
        file4 = Files.createFile(dir2.resolve("file4.tmp"));

        Files.writeString(tempDir.resolve(".gitignore"), "*.log\n**/*.tmp");
    }

    @Test
    @DisplayName("Scans recursively and returns all non-ignored files")
    void shouldReturnAllFiles_whenScanningRecursivelyWithoutIgnoreRules() throws Exception {
        Config config = new Config(tempDir, true, -1, 1, List.of(), List.of(), false, false, "plain");
        FileScanner scanner = new FileScanner(config, null);

        ScanResult result = scanner.scanFiles();

        assertAll(
                () -> assertEquals(4, result.filesToAnalyze().size()),
                () -> assertTrue(result.filesToAnalyze().containsAll(List.of(file1, file2, file3, file4))),
                () -> assertEquals(0, result.ignoredFilesStats().getIgnoredByGitignore()),
                () -> assertEquals(0, result.ignoredFilesStats().getIgnoredByExtension())
        );
    }

    @Test
    @DisplayName("Applies .gitignore rules")
    void shouldApplyGitIgnoreRules_whenGitIgnoreIsEnabled() throws Exception {
        Config config = new Config(tempDir, true, -1, 1, List.of(), List.of(), true, false, "plain");
        GitIgnoreManager gitIgnoreManager = new GitIgnoreManager(tempDir);
        FileScanner scanner = new FileScanner(config, gitIgnoreManager);

        ScanResult result = scanner.scanFiles();

        assertAll(
                () -> assertEquals(2, result.filesToAnalyze().size()),
                () -> assertTrue(result.filesToAnalyze().containsAll(List.of(file1, file3))),
                () -> assertEquals(2, result.ignoredFilesStats().getIgnoredByGitignore()),
                () -> assertEquals(0, result.ignoredFilesStats().getIgnoredByExtension())
        );
    }

    @Test
    @DisplayName("Filters files by included extensions")
    void shouldIncludeOnlySpecifiedExtensions_whenIncludeListIsProvided() throws Exception {
        Config config = new Config(tempDir, true, -1, 1, List.of("java"), List.of(), false, false, "plain");
        FileScanner scanner = new FileScanner(config, null);

        ScanResult result = scanner.scanFiles();

        assertAll(
                () -> assertEquals(2, result.filesToAnalyze().size()),
                () -> assertTrue(result.filesToAnalyze().containsAll(List.of(file1, file3))),
                () -> assertEquals(0, result.ignoredFilesStats().getIgnoredByGitignore()),
                () -> assertEquals(2, result.ignoredFilesStats().getIgnoredByExtension())
        );
    }

    @Test
    @DisplayName("Filters files by excluded extensions")
    void shouldExcludeSpecifiedExtensions_whenExcludeListIsProvided() throws Exception {
        Config config = new Config(tempDir, true, -1, 1, List.of(), List.of("log", "tmp"), false, false, "plain");
        FileScanner scanner = new FileScanner(config, null);

        ScanResult result = scanner.scanFiles();

        assertAll(
                () -> assertEquals(2, result.filesToAnalyze().size()),
                () -> assertTrue(result.filesToAnalyze().containsAll(List.of(file1, file3))),
                () -> assertEquals(0, result.ignoredFilesStats().getIgnoredByGitignore()),
                () -> assertEquals(2, result.ignoredFilesStats().getIgnoredByExtension())
        );
    }

    @Test
    @DisplayName("Respects max-depth configuration")
    void shouldLimitScanDepth_whenMaxDepthIsConfigured() throws Exception {
        Config config = new Config(tempDir, true, 2, 1, List.of(), List.of(), false, false, "plain");
        FileScanner scanner = new FileScanner(config, null);

        ScanResult result = scanner.scanFiles();

        assertAll(
                () -> assertEquals(3, result.filesToAnalyze().size()),
                () -> assertTrue(result.filesToAnalyze().containsAll(List.of(file1, file2, file3))),
                () -> assertFalse(result.filesToAnalyze().contains(file4))
        );
    }

    @Test
    @DisplayName("Scans only top-level directory when recursive = false")
    void shouldScanOnlyTopLevelDirectory_whenRecursiveDisabled() throws Exception {
        Config config = new Config(tempDir, false, -1, 1, List.of(), List.of(), false, false, "plain");
        FileScanner scanner = new FileScanner(config, null);

        ScanResult result = scanner.scanFiles();

        assertAll(
                () -> assertEquals(2, result.filesToAnalyze().size()),
                () -> assertTrue(result.filesToAnalyze().containsAll(List.of(file1, file2)))
        );
    }
}
