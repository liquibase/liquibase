package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.statement.UpdateStatement;
import liquibase.util.NetUtil;

import java.net.InetAddress;
import java.sql.Timestamp;

public class LockDatabaseChangeLogGenerator implements SqlGenerator {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(SqlStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(SqlStatement statement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(SqlStatement statement, Database database) {
        InetAddress localHost;
        try {
            localHost = NetUtil.getLocalHost();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

        UpdateStatement updateStatement = new UpdateStatement(database.getDefaultSchemaName(), database.getDatabaseChangeLogLockTableName());
        updateStatement.addNewColumnValue("LOCKED", true);
        updateStatement.addNewColumnValue("LOCKGRANTED", new Timestamp(new java.util.Date().getTime()));
        updateStatement.addNewColumnValue("LOCKEDBY", localHost.getHostName() + " (" + localHost.getHostAddress() + ")");
        updateStatement.setWhereClause("ID  = 1");

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database); 

    }
}