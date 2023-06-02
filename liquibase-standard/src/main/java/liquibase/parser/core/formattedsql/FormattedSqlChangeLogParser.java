package liquibase.parser.core.formattedsql;

import liquibase.parser.FormattedChangeLogParser;


public class FormattedSqlChangeLogParser extends FormattedChangeLogParser {

    @Override
    protected String getCommentSequence() {
        return "\\-\\-";
    }

    @Override
    protected boolean supportsExtension(String changelogFile) {
        return changelogFile.toLowerCase().endsWith(".sql");
    }
}