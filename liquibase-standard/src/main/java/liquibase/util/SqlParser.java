package liquibase.util;

import liquibase.Scope;
import liquibase.parser.LiquibaseSqlParser;
import liquibase.parser.SqlParserFactory;

/**
 * @deprecated load the {@link LiquibaseSqlParser} using the {@link SqlParserFactory} directly.
 */
@Deprecated
public class SqlParser {

    /**
     * @deprecated load the {@link LiquibaseSqlParser} using the {@link SqlParserFactory} directly.
     */
    @Deprecated
    public static StringClauses parse(String sqlBlock) {
        SqlParserFactory sqlParserFactory = Scope.getCurrentScope().getSingleton(SqlParserFactory.class);
        LiquibaseSqlParser sqlParser = sqlParserFactory.getSqlParser();
        return sqlParser.parse(sqlBlock);
    }

    /**
     * @deprecated load the {@link LiquibaseSqlParser} using the {@link SqlParserFactory} directly.
     */
    @Deprecated
    public static StringClauses parse(String sqlBlock, boolean preserveWhitespace, boolean preserveComments) {
        SqlParserFactory sqlParserFactory = Scope.getCurrentScope().getSingleton(SqlParserFactory.class);
        LiquibaseSqlParser sqlParser = sqlParserFactory.getSqlParser();
        return sqlParser.parse(sqlBlock, preserveWhitespace, preserveComments);
    }
}
