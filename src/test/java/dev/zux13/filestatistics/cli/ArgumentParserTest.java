package dev.zux13.filestatistics.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentParserTest {

    private ArgumentParser parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new ArgumentParser();
    }

    @Test
    @DisplayName("Parses all valid arguments")
    void shouldParseAllValidArguments_whenArgumentsAreValid() {
        String[] args = {
                tempDir.toString(),
                "--recursive",
                "--max-depth=10",
                "--thread=8",
                "--include-ext=java,xml",
                "--exclude-ext=log,tmp",
                "--git-ignore",
                "--output=json",
                "--verbose"
        };

        Config config = parser.parse(args);

        assertAll("Config properties should be parsed correctly",
                () -> assertEquals(tempDir.toAbsolutePath().normalize(), config.path()),
                () -> assertTrue(config.recursive()),
                () -> assertEquals(10, config.maxDepth()),
                () -> assertEquals(8, config.numThreads()),
                () -> assertEquals(List.of("java", "xml"), config.includeExtensions()),
                () -> assertEquals(List.of("log", "tmp"), config.excludeExtensions()),
                () -> assertTrue(config.useGitIgnore()),
                () -> assertEquals("json", config.outputFormat()),
                () -> assertTrue(config.verbose())
        );
    }

    @Test
    @DisplayName("Uses default values when optional arguments are missing")
    void shouldUseDefaultValues_whenOptionalArgumentsAreMissing() {
        String[] args = {tempDir.toString()};

        Config config = parser.parse(args);

        assertAll("Config should have default values for optional args",
                () -> assertFalse(config.recursive()),
                () -> assertEquals(-1, config.maxDepth()),
                () -> assertEquals(1, config.numThreads()),
                () -> assertTrue(config.includeExtensions().isEmpty()),
                () -> assertTrue(config.excludeExtensions().isEmpty()),
                () -> assertFalse(config.useGitIgnore()),
                () -> assertEquals("plain", config.outputFormat()),
                () -> assertFalse(config.verbose())
        );
    }

    @Test
    @DisplayName("Throws exception for unknown argument")
    void shouldThrowException_whenArgumentIsUnknown() {
        String[] args = {tempDir.toString(), "--unknown-arg"};

        var exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(args));
        assertEquals("Unknown argument: --unknown-arg", exception.getMessage());
    }

    @Test
    @DisplayName("Throws exception when path is missing")
    void shouldThrowException_whenPathIsMissing() {
        String[] args = {"--recursive"};

        var exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(args));
        assertEquals("A required argument is missing: <path>", exception.getMessage());
    }

    @Test
    @DisplayName("Throws exception when path does not exist")
    void shouldThrowException_whenPathDoesNotExist() {
        String nonExistentPath = tempDir.resolve("nonexistent").toString();
        String[] args = {nonExistentPath};

        var exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(args));
        assertTrue(exception.getMessage().contains("The specified path does not exist"));
    }

    @Test
    @DisplayName("Throws exception when path is a file")
    void shouldThrowException_whenPathIsAFile() throws IOException {
        Path file = Files.createFile(tempDir.resolve("file.txt"));
        String[] args = {file.toString()};

        var exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(args));
        assertTrue(exception.getMessage().contains("is not a directory"));
    }

    @Test
    @DisplayName("Throws exception for invalid max-depth value")
    void shouldThrowException_whenMaxDepthValueIsInvalid() {
        String[] args = {tempDir.toString(), "--max-depth=abc"};

        var exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(args));
        assertTrue(exception.getMessage().contains("Invalid value for --max-depth"));
    }

    @Test
    @DisplayName("Throws exception when extensions overlap between include and exclude")
    void shouldThrowException_whenExtensionsOverlapInIncludeAndExclude() {
        String[] args = {tempDir.toString(), "--include-ext=java,xml", "--exclude-ext=xml,log"};

        var exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(args));
        assertTrue(exception.getMessage().contains("Extensions cannot be both included and excluded"));
    }
}
