package dev.zux13.filestatistics.analysis;

import dev.zux13.filestatistics.analysis.definition.*;

import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

public class CommentAnalyzer {

    private static final Map<String, Function<Void, CommentDefinition>> DEFINITION_MAP = new HashMap<>();

    static {
        DEFINITION_MAP.put("java", v -> new JavaCommentDefinition());
        DEFINITION_MAP.put("bash", v -> new BashCommentDefinition());
        DEFINITION_MAP.put("sh", v -> new BashCommentDefinition());
    }

    private final CommentDefinition defaultDefinition = new DefaultCommentDefinition();

    public long countCommentLines(Iterable<String> lines, Path filePath) {
        String extension = getExtension(filePath);
        Function<Void, CommentDefinition> definitionFunc = DEFINITION_MAP.getOrDefault(extension, v -> defaultDefinition);
        CommentDefinition definition = definitionFunc.apply(null);

        AnalysisState state = new AnalysisState();
        long count = 0;
        for (String line : lines) {
            if (definition.isComment(line, state)) {
                count++;
            }
        }
        return count;
    }

    private String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return null;
    }
}
