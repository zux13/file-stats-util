package dev.zux13.filestatistics.analysis.definition;

import dev.zux13.filestatistics.analysis.AnalysisState;

public class BashCommentDefinition implements CommentDefinition {

    @Override
    public boolean isComment(String line, AnalysisState state) {
        return line.stripLeading().startsWith("#");
    }
}
