package liquibase.executor;

import liquibase.statement.SqlStatement;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sql.Sql;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.database.Database;

import java.util.List;

public abstract class AbstractExecutor {
    protected Database database;

    protected AbstractExecutor(Database database) {
        this.database = database;
    }

    protected String[] applyVisitors(SqlStatement statement, List<SqlVisitor> sqlVisitors) throws StatementNotSupportedOnDatabaseException, JDBCException {
        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        if (sql == null) {
            return new String[0];
        }
        String[] returnSql = new String[sql.length];

        for (int i=0; i<sql.length; i++) {
            returnSql[i] = sql[i].toSql();
            for (SqlVisitor visitor : sqlVisitors) {
                if (visitor.isApplicable(database)) {
                    returnSql[i] = visitor.modifySql(returnSql[i], database);
                }
            }

        }
        return returnSql;
    }

}
