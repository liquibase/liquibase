package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;

import java.sql.SQLException;

public abstract class AbstractSqlStatementTest {

    protected void dropAndCreateTable(CreateTableStatement statement, Database database) throws SQLException, JDBCException {
        String schema = "";
        if (statement.getSchemaName() != null) {
            schema = statement.getSchemaName() + ".";
        }

        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

        try {
            new JdbcTemplate(database).execute(new RawSqlStatement("drop table " + schema + statement.getTableName()));
        } catch (JDBCException e) {
            if (!database.getConnection().getAutoCommit()) {
                database.getConnection().rollback();
            }
        }
        new JdbcTemplate(database).execute(statement);

        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

    }
}
