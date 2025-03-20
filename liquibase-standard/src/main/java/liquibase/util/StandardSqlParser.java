package liquibase.util;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeSet;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.LiquibaseSqlParser;
import liquibase.util.grammar.*;
import org.apache.commons.lang3.StringUtils;

import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardSqlParser implements LiquibaseSqlParser {

    @Override
    public StringClauses parse(String sqlBlock) {
        return parse(sqlBlock, false, false);
    }

    @Override
    public StringClauses parse(String sqlBlock, boolean preserveWhitespace, boolean preserveComments) {
        return parse(sqlBlock, preserveWhitespace, preserveComments, null);
    }

    @Override
    public StringClauses parse(String sqlBlock, boolean preserveWhitespace, boolean preserveComments, ChangeSet changeSet) {
        StringClauses clauses = new StringClauses(preserveWhitespace?"":" ");

        SimpleSqlGrammarTokenManager tokenManager = new SimpleSqlGrammarTokenManager(new SimpleCharStream(new StringReader(sqlBlock)));
        SimpleSqlGrammar t = new SimpleSqlGrammar(tokenManager);
        try {
            Token token = t.getNextToken();
            while (token != null && !token.toString().isEmpty()) {
                if (token.kind == SimpleSqlGrammarConstants.WHITESPACE) {
                    if (preserveWhitespace) {
                        clauses.append(new StringClauses.Whitespace(token.image));
                    }
                } else if ((token.kind == SimpleSqlGrammarConstants.LINE_COMMENT) || (token.kind ==
                        SimpleSqlGrammarConstants.MULTI_LINE_COMMENT)) {
                    if (preserveComments) {
                        String comment = token.image;
                        if (!preserveWhitespace && (token.kind == SimpleSqlGrammarConstants.LINE_COMMENT)) {
                            if (!comment.endsWith("\n")) {
                                comment = comment + "\n";
                            }
                        }
                        clauses.append(new StringClauses.Comment(comment));
                    }
                } else {
                    clauses.append(token.image);
                }
                token = t.getNextToken();
            }
        } catch (Throwable e) {
            if (changeSet != null) {
                Change change = Scope.getCurrentScope().get(ChangeSet.CHANGE_KEY, Change.class);
                String message = enhanceExceptionMessage(changeSet, change, e);
                throw new UnexpectedLiquibaseException(message, e);
            } else {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return clauses;
    }

    /**
     *
     * If this is a RawSQLChange, then add information about the real position of the SQL in the
     * formatted SQL changelog
     *
     * @param  changeSet     The current change set
     * @param  change        The change that generated the SQL
     * @param  e             The thrown exception
     * @return String        The message to display
     *
     */
    private static String enhanceExceptionMessage(ChangeSet changeSet, Change change, Throwable e) {
        if (! (change instanceof RawSQLChange) || e.getMessage() == null) {
            return changeSet.toString();
        }
        String message = changeSet.toString();
        try {
            String exceptionMessage = e.getMessage();
            Pattern p = Pattern.compile("(?i).* line ([\\d]+).*");
            Matcher m = p.matcher(exceptionMessage);
            String atLine = "";
            if (m.matches()) {
                atLine = m.group(1);
            }
            int startLine = ((RawSQLChange) change).getSqlStartLine();
            int endLine = ((RawSQLChange) change).getSqlEndLine();
            if (StringUtils.isEmpty(atLine)) {
                message = String.format("%s (lines %d-%d)", changeSet, startLine, endLine);
            } else {
                int actualAtLine = Integer.parseInt(atLine) + startLine - 1;
                message = String.format("%s (issue at line %d of lines %d-%d)", changeSet, actualAtLine, startLine, endLine);
            }
        } catch (Exception ignored) {
            // consume and ignore.  We'll just return and use the changeset toString()
        }
        return message;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
}
