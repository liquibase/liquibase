package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.grammar.*;

import java.io.StringReader;

public class SqlParser {

    public static StringClauses parse(String sqlBlock) {
        return parse(sqlBlock, false, false);
    }

    public static StringClauses parse(String sqlBlock, boolean preserveWhitespace, boolean preserveComments) {
        StringClauses clauses = new StringClauses(preserveWhitespace?"":" ");

        SimpleSqlGrammarTokenManager tokenManager = new SimpleSqlGrammarTokenManager(new SimpleCharStream(new StringReader(sqlBlock)));
        SimpleSqlGrammar t = new SimpleSqlGrammar(tokenManager);
        try {
            Token token = t.getNextToken();
            while (!token.toString().equals("")) {
                if (token.kind == SimpleSqlGrammarConstants.WHITESPACE) {
                    if (preserveWhitespace) {
                        clauses.append(new StringClauses.Whitespace(token.image));
                    }
                } else if (token.kind == SimpleSqlGrammarConstants.LINE_COMMENT || token.kind == SimpleSqlGrammarConstants.MULTI_LINE_COMMENT) {
                    if (preserveComments) {
                        String comment = token.image;
                        if (!preserveWhitespace && token.kind == SimpleSqlGrammarConstants.LINE_COMMENT) {
                            if (!comment.endsWith("\n")) {
                                comment = comment + "\n";
                            }
                        }
                        clauses.append(new StringClauses.Comment(comment));
                    }
                } else if (token.kind == SimpleSqlGrammarConstants.QUOTED_IDENTIFIER) {
                    clauses.append(new StringClauses.QuotedIdentifier(token.image));
                } else if (token.kind == SimpleSqlGrammarConstants.QUOTED_STRING) {
                    clauses.append(new StringClauses.QuotedString(token.image));
                } else {
                    clauses.append(token.image);
                }
                token = t.getNextToken();
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return clauses;
    }
}
