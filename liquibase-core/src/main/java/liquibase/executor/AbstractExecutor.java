package liquibase.executor;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

import java.util.List;
import java.util.Set;

public abstract class AbstractExecutor {
    protected Database database;

    public void setDatabase(Database database) {
        this.database = database;
    }

    protected String[] applyVisitors(SqlStatement statement, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        if (sql == null) {
            return new String[0];
        }
        String[] returnSql = new String[sql.length];

        for (int i=0; i<sql.length; i++) {
            if (sql[i] == null) {
                continue;
            }
            returnSql[i] = sql[i].toSql();
            if (sqlVisitors != null) {
                for (SqlVisitor visitor : sqlVisitors) {
                    returnSql[i] = visitor.modifySql(returnSql[i], database);
                }
            }

        }
        return returnSql;
    }

}
