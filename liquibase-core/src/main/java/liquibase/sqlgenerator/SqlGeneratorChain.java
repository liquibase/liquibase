package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;

import java.util.Iterator;
import java.util.SortedSet;

public class SqlGeneratorChain {
    private Iterator<SqlGenerator> sqlGenerators;

    public SqlGeneratorChain(SortedSet<SqlGenerator> sqlGenerators) {
        if (sqlGenerators != null) {
            this.sqlGenerators = sqlGenerators.iterator();
        }
    }

    public Sql[] generateSql(SqlStatement statement, Database database) {
        if (sqlGenerators == null) {
            return null;
        }

        if (!sqlGenerators.hasNext()) {
            return new Sql[0];
        }

        return sqlGenerators.next().generateSql(statement, database, this);
    }

    public Warnings warn(SqlStatement statement, Database database) {
        if (sqlGenerators == null || !sqlGenerators.hasNext()) {
            return new Warnings();
        }

        return sqlGenerators.next().warn(statement, database, this);
    }

    public ValidationErrors validate(SqlStatement statement, Database database) {
        if (sqlGenerators == null || !sqlGenerators.hasNext()) {
            return new ValidationErrors();
        }

        return sqlGenerators.next().validate(statement, database, this);
    }
}
