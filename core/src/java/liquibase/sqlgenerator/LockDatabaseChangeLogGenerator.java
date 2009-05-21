package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.statement.UpdateStatement;
import liquibase.statement.LockDatabaseChangeLogStatement;
import liquibase.util.NetUtil;

import java.net.InetAddress;
import java.sql.Timestamp;

public class LockDatabaseChangeLogGenerator implements SqlGenerator<LockDatabaseChangeLogStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(LockDatabaseChangeLogStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(LockDatabaseChangeLogStatement statement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(LockDatabaseChangeLogStatement statement, Database database) {
    	String liquibaseSchema = null;
    	try {
    		liquibaseSchema = database.getLiquibaseSchemaName();
    	} catch (JDBCException e) {
            throw new UnexpectedLiquibaseException(e);
    	}

        InetAddress localHost;
    	try {
            localHost = NetUtil.getLocalHost();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

        UpdateStatement updateStatement = new UpdateStatement(liquibaseSchema, database.getDatabaseChangeLogLockTableName());
        updateStatement.addNewColumnValue("LOCKED", true);
        updateStatement.addNewColumnValue("LOCKGRANTED", new Timestamp(new java.util.Date().getTime()));
        updateStatement.addNewColumnValue("LOCKEDBY", localHost.getHostName() + " (" + localHost.getHostAddress() + ")");
        updateStatement.setWhereClause("ID  = 1");

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database); 

    }
}