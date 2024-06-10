package liquibase.parser.core.formattedsql;

import liquibase.change.AbstractSQLChange;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.AbstractFormattedChangeLogParser;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.SqlPrecondition;
import liquibase.precondition.core.TableExistsPrecondition;
import liquibase.precondition.core.ViewExistsPrecondition;
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
                switch (name) {
                    case "sql-check":
                        changeSet.getPreconditions().addNestedPrecondition(
                                parseSqlCheckCondition(changeLogParameters.expandExpressions(StringUtil.trimToNull(body), changeSet.getChangeLog()))
                        );
                        break;
                    case "table-exists":
                        changeSet.getPreconditions().addNestedPrecondition(
                                parseTableExistsCondition(changeLogParameters.expandExpressions(StringUtil.trimToNull(body), changeSet.getChangeLog()))
                        );
                        break;
                    case "view-exists":
                        changeSet.getPreconditions().addNestedPrecondition(
                                parseViewExistsCondition(changeLogParameters.expandExpressions(StringUtil.trimToNull(body), changeSet.getChangeLog()))
                        );
                        break;
                    default:
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

    @Override
    protected void handleInvalidEmptyPreconditionCase(ChangeLogParameters changeLogParameters, ChangeSet changeSet, Matcher preconditionMatcher) throws ChangeLogParseException {
        if (preconditionMatcher.groupCount() == 1) {
            String name = StringUtil.trimToNull(preconditionMatcher.group(1));
            if (name != null) {
                switch (name) {
                    case "sql-check":
                        throw new ChangeLogParseException("Precondition sql check failed because of missing required expectedResult and sql parameters.");
                    case "table-exists":
                        throw new ChangeLogParseException("Precondition table exists failed because of missing required table name parameter.");
                    case "view-exists":
                        throw new ChangeLogParseException("Precondition view exists failed because of missing required view name parameter.");
                    default:
                        throw new ChangeLogParseException("The '" + name + "' precondition type is not supported.");
                }
            }
        }
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

    private TableExistsPrecondition parseTableExistsCondition(String body) throws ChangeLogParseException {
        Matcher tableMatcher = TABLE_NAME_STATEMENT_PATTERN.matcher(body);
        Matcher schemaMatcher = SCHEMA_NAME_STATEMENT_PATTERN.matcher(body);
        TableExistsPrecondition tableExistsPrecondition = new TableExistsPrecondition();

        if (tableMatcher.matches()) {
            if (tableMatcher.groupCount() == 0) {
                throw new ChangeLogParseException("Table name was not specified in tableExists precondition but is required '" + body + "'.");
            }

            if (tableMatcher.groupCount() > 1) {
                throw new ChangeLogParseException("Multiple table names were specified in tableExists precondition '" + body + "'.");
            }

            tableExistsPrecondition.setTableName(tableMatcher.group(1));
        } else {
            throw new ChangeLogParseException("Table name was not specified correctly in tableExists precondition.");
        }

        if (schemaMatcher.matches() && schemaMatcher.groupCount() == 1) {
            if (schemaMatcher.groupCount() > 1) {
                throw new ChangeLogParseException("Multiple schema names were specified in tableExists precondition '" + body + "'.");
            }

            tableExistsPrecondition.setSchemaName(schemaMatcher.group(1));
        }

        return tableExistsPrecondition;
    }

    private ViewExistsPrecondition parseViewExistsCondition(String body) throws ChangeLogParseException {
        Matcher viewMatcher = VIEW_NAME_STATEMENT_PATTERN.matcher(body);
        Matcher schemaMatcher = SCHEMA_NAME_STATEMENT_PATTERN.matcher(body);
        ViewExistsPrecondition viewExistsPrecondition = new ViewExistsPrecondition();

        if (viewMatcher.matches()) {
            if (viewMatcher.groupCount() == 0) {
                throw new ChangeLogParseException("View name was not specified in viewExists precondition but is required '" + body + "'.");
            }

            if (viewMatcher.groupCount() > 1) {
                throw new ChangeLogParseException("Multiple view names were specified in viewExists precondition '" + body + "'.");
            }

            viewExistsPrecondition.setViewName(viewMatcher.group(1));
        } else {
            throw new ChangeLogParseException("View name was not specified correctly in viewExists precondition.");
        }

        if (schemaMatcher.matches()) {
            if (schemaMatcher.groupCount() > 1) {
                throw new ChangeLogParseException("Multiple schema names were specified in viewExists precondition '" + body + "'.");
            }

            viewExistsPrecondition.setSchemaName(schemaMatcher.group(1));
        }

        return viewExistsPrecondition;
    }
}
