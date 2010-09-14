package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.NetUtil;

import java.net.InetAddress;
import java.sql.Timestamp;

public class LockDatabaseChangeLogGenerator extends AbstractSqlGenerator<LockDatabaseChangeLogStatement> {

    public ValidationErrors validate(LockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(LockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	String liquibaseSchema = null;
    		liquibaseSchema = database.getLiquibaseSchemaName();

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
        updateStatement.setWhereClause(database.escapeColumnName(liquibaseSchema, database.getDatabaseChangeLogTableName(), "ID") + " = 1");

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);

    }
}