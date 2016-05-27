package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.grammar.SimpleSqlGrammar;
import liquibase.util.grammar.SimpleSqlGrammarConstants;
import liquibase.util.grammar.Token;

import java.io.StringReader;

public class SqlParser {

    public static StringClauses parse(String sqlBlock) {
        return parse(sqlBlock, false, false);
    }

    public static StringClauses parse(String sqlBlock, boolean preserveWhitespace, boolean preserveComments) {
        StringClauses clauses = new StringClauses(preserveWhitespace?"":" ");

        SimpleSqlGrammar t = new SimpleSqlGrammar(new StringReader(sqlBlock));
        try {
            Token token = t.getNextToken();
            while (!token.toString().equals("")) {
                if (token.kind == SimpleSqlGrammarConstants.WHITESPACE) {
                    if (preserveWhitespace) {
                        clauses.append(new StringClauses.Whitespace(token.image));
                    }
                } else if (token.kind == SimpleSqlGrammarConstants.LINE_COMMENT || token.kind == SimpleSqlGrammarConstants.MULTI_LINE_COMMENT) {
                    if (preserveComments) {
                        clauses.append(new StringClauses.Comment(token.image));
                    }
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
