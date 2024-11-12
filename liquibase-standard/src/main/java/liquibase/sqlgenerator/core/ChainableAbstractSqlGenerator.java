package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ChainableAbstractSqlGenerator<T extends SqlStatement> extends AbstractSqlGenerator<T> {

    @Override
    public Sql[] generateSql(final T statement, final Database database, final SqlGeneratorChain<T> sqlGeneratorChain) {
        List<Sql> sqls = new ArrayList<>();
        sqls.addAll(Arrays.asList(generateSql(statement, database)));
        sqls.addAll(Arrays.asList(sqlGeneratorChain.generateSql(statement, database)));
        return sqls.toArray(EMPTY_SQL);
    }

    public abstract Sql[] generateSql(final T statement, final Database database);
}
