package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.grammer.SimpleSqlGrammer;
import liquibase.util.grammer.SimpleSqlGrammerConstants;
import liquibase.util.grammer.Token;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SqlParser {

    public static StringClauses[] parse(String sqlBlock) {
        return parse(sqlBlock, true, false, false, false);
    }

    public static StringClauses[] parse(String sqlBlock, boolean split, boolean preserveWhitespace, boolean preserveComments, boolean includeDelimiters) {
        List<StringClauses> returnList = new ArrayList<StringClauses>();

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
                } else if (token.kind == SimpleSqlGrammerConstants.DELIMITER) {
                    if (split) {
                        if (!clauses.isEmpty()) {
                            if (includeDelimiters) {
                                clauses.append(new StringClauses.Delimiter(token.image));
                            }
                            returnList.add(clauses);
                            clauses = new StringClauses(preserveWhitespace ? "" : " ");
                        }
                    } else {
                        if (includeDelimiters) {
                            clauses.append(new StringClauses.Delimiter(token.image));
                        }
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

            if (!clauses.isEmpty()) {
                returnList.add(clauses);
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return returnList.toArray(new StringClauses[returnList.size()]);
    }
}
