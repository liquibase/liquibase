package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.grammar.SimpleSqlGrammar;
import liquibase.util.grammar.SimpleSqlGrammarConstants;
import liquibase.util.grammar.Token;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
                        String comment = token.image;
                        if (!preserveWhitespace && token.kind == SimpleSqlGrammarConstants.LINE_COMMENT) {
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

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return clauses;
    }
}
