package dev.zux13.filestatistics.analysis;

import dev.zux13.filestatistics.analysis.model.FileStatistics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileAnalyzer {

    private static final int SAMPLE_SIZE = 512;

    private final CommentAnalyzer commentAnalyzer;

    public FileAnalyzer(CommentAnalyzer commentAnalyzer) {
        this.commentAnalyzer = commentAnalyzer;
    }

    public FileStatistics analyzeFile(Path filePath) throws IOException {
        if (isBinary(filePath)) {
            return null;
        }

        long sizeInBytes = Files.size(filePath);

        List<String> lines = readAllLinesSafely(filePath);

        long totalLines = lines.size();
        long nonEmptyLines = lines.stream().map(String::strip).filter(s -> !s.isEmpty()).count();
        long commentLines = commentAnalyzer.countCommentLines(lines, filePath);

        return new FileStatistics(sizeInBytes, totalLines, nonEmptyLines, commentLines);
    }

    private List<String> readAllLinesSafely(Path file) {
        try (InputStream in = Files.newInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;

        } catch (IOException e) {
            return List.of();
        }
    }

    private boolean isBinary(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[SAMPLE_SIZE];
            int read = in.read(buffer);
            for (int i = 0; i < read; i++) {
                byte b = buffer[i];
                if (b < 0x09) return true;
                if (b > 0x0D && b < 0x20) return true;
            }
        }
        return false;
    }
}
