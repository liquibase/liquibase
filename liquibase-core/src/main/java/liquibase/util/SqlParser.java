package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.grammer.SimpleSqlGrammer;
import liquibase.util.grammer.SimpleSqlGrammerConstants;
import liquibase.util.grammer.Token;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SqlParser {

    public static StringClauses parse(String sqlBlock) {
        return parse(sqlBlock, false, false);
    }

    public static StringClauses parse(String sqlBlock, boolean preserveWhitespace, boolean preserveComments) {
        StringClauses clauses = new StringClauses(preserveWhitespace?"":" ");

        int i =0;
        SimpleSqlGrammer t = new SimpleSqlGrammer(new StringReader(sqlBlock));
        try {
            Token token = t.getNextToken();
            while (!token.toString().equals("")) {
                if (token.kind == SimpleSqlGrammerConstants.WHITESPACE) {
                    if (preserveWhitespace) {
                        clauses.append(new StringClauses.Whitespace(token.image));
                    }
                } else if (token.kind == SimpleSqlGrammerConstants.LINE_COMMENT || token.kind == SimpleSqlGrammerConstants.MULTI_LINE_COMMENT) {
                    if (preserveComments) {
                        clauses.append(new StringClauses.Comment(token.image));
                    }
                } else {
                    clauses.append(token.image+":"+i++, token.image);
                }
                token = t.getNextToken();
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return clauses;
    }
}
