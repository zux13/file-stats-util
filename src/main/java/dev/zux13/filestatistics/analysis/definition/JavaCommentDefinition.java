package dev.zux13.filestatistics.analysis.definition;

import dev.zux13.filestatistics.analysis.AnalysisState;

public class JavaCommentDefinition implements CommentDefinition {

    @Override
    public boolean isComment(String line, AnalysisState state) {
        String trimmedLine = line.stripLeading();

        if (state.isInMultiLineComment()) {
            return handleMultiLineComment(trimmedLine, state);
        }

        return isSingleLineComment(trimmedLine) || handleBlockComment(trimmedLine, state);
    }

    private boolean handleMultiLineComment(String trimmedLine, AnalysisState state) {
        if (trimmedLine.contains("*/")) {
            state.setInMultiLineComment(false);
        }
        return true;
    }

    private boolean isSingleLineComment(String trimmedLine) {
        return trimmedLine.startsWith("//");
    }

    private boolean handleBlockComment(String trimmedLine, AnalysisState state) {
        if (trimmedLine.startsWith("/*")) {
            if (!trimmedLine.contains("*/")) {
                state.setInMultiLineComment(true);
            }
            return true;
        }
        return false;
    }
}
