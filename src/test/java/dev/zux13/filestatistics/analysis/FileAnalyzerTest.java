package dev.zux13.filestatistics.analysis;

import dev.zux13.filestatistics.analysis.model.FileStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileAnalyzerTest {

    @TempDir
    Path tempDir;

    private FileAnalyzer fileAnalyzer;

    @BeforeEach
    void setUp() {
        fileAnalyzer = new FileAnalyzer(new CommentAnalyzer());
    }

    @Test
    @DisplayName("Analyzes Java file correctly")
    void shouldAnalyzeJavaFile_whenFileIsJava() throws IOException {
        String content = """
                package com.example;

                // This is a single-line comment
                public class MyClass {
                    /*
                     * This is a multi-line comment
                     */
                    private int value = 0; // Inline comment

                    public void doSomething() {
                        // Another comment
                        System.out.println("Hello");
                    }
                }
                """;
        Path javaFile = tempDir.resolve("MyClass.java");
        Files.writeString(javaFile, content);

        FileStatistics stats = fileAnalyzer.analyzeFile(javaFile);

        assertNotNull(stats);
        assertEquals(content.getBytes().length, stats.sizeInBytes());
        assertEquals(14, stats.totalLines());
        assertEquals(12, stats.nonEmptyLines());
        assertEquals(5, stats.commentLines());
    }

    @Test
    @DisplayName("Analyzes Bash script correctly")
    void shouldAnalyzeBashFile_whenFileIsBashScript() throws IOException {
        String content = """
                #!/bin/bash
                # This is a comment

                echo "Hello, World!" # Inline comment

                # Another comment
                VAR="value"
                """;
        Path bashFile = tempDir.resolve("script.sh");
        Files.writeString(bashFile, content);

        FileStatistics stats = fileAnalyzer.analyzeFile(bashFile);

        assertNotNull(stats);
        assertEquals(content.getBytes().length, stats.sizeInBytes());
        assertEquals(7, stats.totalLines());
        assertEquals(5, stats.nonEmptyLines());
        assertEquals(3, stats.commentLines());
    }

    @Test
    @DisplayName("Analyzes empty file correctly")
    void shouldAnalyzeFile_whenFileIsEmpty() throws IOException {
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);

        FileStatistics stats = fileAnalyzer.analyzeFile(emptyFile);

        assertNotNull(stats);
        assertEquals(0, stats.sizeInBytes());
        assertEquals(0, stats.totalLines());
        assertEquals(0, stats.nonEmptyLines());
        assertEquals(0, stats.commentLines());
    }

    @Test
    @DisplayName("Analyzes file with no extension")
    void shouldAnalyzeFile_whenFileHasNoExtension() throws IOException {
        String content = """
                line 1
                line 2
                # This is a comment, but default definition doesn't count it
                """;
        Path noExtFile = tempDir.resolve("README");
        Files.writeString(noExtFile, content);

        FileStatistics stats = fileAnalyzer.analyzeFile(noExtFile);

        assertNotNull(stats);
        assertEquals(content.getBytes().length, stats.sizeInBytes());
        assertEquals(3, stats.totalLines());
        assertEquals(3, stats.nonEmptyLines());
        assertEquals(0, stats.commentLines());
    }

    @Test
    @DisplayName("Returns null for binary file")
    void shouldReturnNull_whenFileIsBinary() throws IOException {
        Path binaryFile = tempDir.resolve("binary.bin");
        Files.write(binaryFile, new byte[]{ (byte) 0xFF, (byte) 0xFE, (byte) 0xFD });

        FileStatistics stats = fileAnalyzer.analyzeFile(binaryFile);

        assertNull(stats);
    }

    @Test
    @DisplayName("Uses default comment definition for unknown extension")
    void shouldUseDefaultCommentDefinition_whenExtensionIsUnknown() throws IOException {
        String content = """
                line 1
                // This is a comment in an unknown language
                /* Another comment */
                """;
        Path unknownExtFile = tempDir.resolve("config.xyz");
        Files.writeString(unknownExtFile, content);

        FileStatistics stats = fileAnalyzer.analyzeFile(unknownExtFile);

        assertNotNull(stats);
        assertEquals(content.getBytes().length, stats.sizeInBytes());
        assertEquals(3, stats.totalLines());
        assertEquals(3, stats.nonEmptyLines());
        assertEquals(0, stats.commentLines());
    }
}
