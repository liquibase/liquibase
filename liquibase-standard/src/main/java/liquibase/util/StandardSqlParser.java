package liquibase.util;

import liquibase.changelog.ChangeSet;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.LiquibaseSqlParser;
import liquibase.util.grammar.*;

import java.io.StringReader;

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
            while (!"".equals(token.toString())) {
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
                throw new UnexpectedLiquibaseException(changeSet.toString(), e);
            } else {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return clauses;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
}
