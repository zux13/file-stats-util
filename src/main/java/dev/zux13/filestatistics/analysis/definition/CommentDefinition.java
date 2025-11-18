package dev.zux13.filestatistics.analysis.definition;

import dev.zux13.filestatistics.analysis.AnalysisState;

public interface CommentDefinition {
    boolean isComment(String line, AnalysisState state);
}
