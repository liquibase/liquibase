package liquibase.parser.core.formattedsql;

import liquibase.change.AbstractSQLChange;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.AbstractFormattedChangeLogParser;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.SqlPrecondition;
import liquibase.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FormattedSqlChangeLogParser extends AbstractFormattedChangeLogParser {

    private static final String ON_UPDATE_SQL_REGEX = ".*onUpdateSQL:(\\w+).*";
    private static final Pattern ON_UPDATE_SQL_PATTERN = Pattern.compile(ON_UPDATE_SQL_REGEX, Pattern.CASE_INSENSITIVE);

    private static final String ON_SQL_OUTPUT_REGEX = ".*onSqlOutput:(\\w+).*";
    private static final Pattern ON_SQL_OUTPUT_PATTERN = Pattern.compile(ON_SQL_OUTPUT_REGEX, Pattern.CASE_INSENSITIVE);


    @Override
    protected String getSingleLineCommentOneCharacter() {
        return "-";
    }

    @Override
    protected String getStartMultiLineCommentSequence() {
        return "\\/\\*";
    }

    @Override
    protected String getEndMultiLineCommentSequence() {
        return "\\*\\/";
    }

    @Override
    protected String getSingleLineCommentSequence() {
        return "\\-\\-";
    }

    @Override
    protected boolean supportsExtension(String changelogFile) {
        return changelogFile.toLowerCase().endsWith(".sql");
    }

    @Override
    protected void handlePreconditionsCase(ChangeSet changeSet, int count, Matcher preconditionsMatcher) throws ChangeLogParseException {
        if (preconditionsMatcher.groupCount() == 0) {
            String message = String.format("Unexpected formatting at line %d. Formatted %s changelogs require known formats, such as '--preconditions <onFail>|<onError>|<onUpdate>' and others to be recognized and run. Learn all the options at %s", count, getSequenceName(), getDocumentationLink());
            throw new ChangeLogParseException("\n" + message);
        }
        if (preconditionsMatcher.groupCount() == 1) {
            String body = preconditionsMatcher.group(1);
            Matcher onFailMatcher = ON_FAIL_PATTERN.matcher(body);
            Matcher onErrorMatcher = ON_ERROR_PATTERN.matcher(body);
            Matcher onUpdateSqlMatcher = ON_UPDATE_SQL_PATTERN.matcher(body);
            Matcher onSqlOutputMatcher = ON_SQL_OUTPUT_PATTERN.matcher(body);

            PreconditionContainer pc = new PreconditionContainer();
            pc.setOnFail(StringUtil.trimToNull(parseString(onFailMatcher)));
            pc.setOnError(StringUtil.trimToNull(parseString(onErrorMatcher)));

            if (onSqlOutputMatcher.matches() && onUpdateSqlMatcher.matches()) {
                throw new IllegalArgumentException("Please modify the changelog to have preconditions set with either " +
                        "'onUpdateSql' or 'onSqlOutput', and not with both.");
            }
            if (onSqlOutputMatcher.matches()) {
                pc.setOnSqlOutput(StringUtil.trimToNull(parseString(onSqlOutputMatcher)));
            } else {
                pc.setOnSqlOutput(StringUtil.trimToNull(parseString(onUpdateSqlMatcher)));
            }
            changeSet.setPreconditions(pc);
        }
    }

    @Override
    protected void handlePreconditionCase(ChangeLogParameters changeLogParameters, ChangeSet changeSet, Matcher preconditionMatcher) throws ChangeLogParseException {
        if (changeSet.getPreconditions() == null) {
            // create the defaults
            changeSet.setPreconditions(new PreconditionContainer());
        }
        if (preconditionMatcher.groupCount() == 2) {
            String name = StringUtil.trimToNull(preconditionMatcher.group(1));
            if (name != null) {
                String body = preconditionMatcher.group(2).trim();
                if ("sql-check".equals(name)) {
                    changeSet.getPreconditions().addNestedPrecondition(parseSqlCheckCondition(changeLogParameters.expandExpressions(StringUtil.trimToNull(body), changeSet.getChangeLog())));
                } else {
                    throw new ChangeLogParseException("The '" + name + "' precondition type is not supported.");
                }
            }
        }
    }

    @Override
    protected AbstractSQLChange getChange() {
        return new RawSQLChange();
    }

    @Override
    protected String getDocumentationLink() {
        return "https://docs.liquibase.com/concepts/changelogs/sql-format.html";
    }

    @Override
    protected String getSequenceName() {
        return "SQL";
    }

    @Override
    protected void setChangeSequence(AbstractSQLChange change, String finalCurrentSequence) {
        change.setSql(finalCurrentSequence);
    }

    @Override
    protected boolean isNotEndDelimiter(AbstractSQLChange change) {
        if (change instanceof RawSQLChange) {
            return (change.getEndDelimiter() == null) && StringUtil.trimToEmpty(change.getSql()).endsWith("\n/");
        }
        return false;
    }

    @Override
    protected void setChangeSequence(ChangeLogParameters changeLogParameters, StringBuilder currentSequence, ChangeSet changeSet, AbstractSQLChange change) {
        change.setSql(changeLogParameters.expandExpressions(StringUtil.trimToNull(currentSequence.toString()), changeSet.getChangeLog()));
    }

    private SqlPrecondition parseSqlCheckCondition(String body) throws ChangeLogParseException {
        for (Pattern pattern : WORD_AND_QUOTING_PATTERNS) {
            Matcher matcher = pattern.matcher(body);
            if (matcher.matches() && (matcher.groupCount() == 2)) {
                SqlPrecondition p = new SqlPrecondition();
                p.setExpectedResult(matcher.group(1));
                p.setSql(matcher.group(2));
                return p;
            }
        }
        throw new ChangeLogParseException("Could not parse a SqlCheck precondition from '" + body + "'.");
    }
}