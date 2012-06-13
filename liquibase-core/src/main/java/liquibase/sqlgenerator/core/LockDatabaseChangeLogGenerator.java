package liquibase.sqlgenerator.core;

import java.net.InetAddress;
import java.sql.Timestamp;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.NetUtil;

public class LockDatabaseChangeLogGenerator extends AbstractSqlGenerator<LockDatabaseChangeLogStatement> {

    public ValidationErrors validate(LockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(LockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	String liquibaseSchema = database.getLiquibaseSchemaName();
        String liquibaseCatalog = database.getLiquibaseCatalogName();

        InetAddress localHost;
    	try {
            localHost = NetUtil.getLocalHost();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

        UpdateStatement updateStatement = new UpdateStatement(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogLockTableName());
        updateStatement.addNewColumnValue("LOCKED", true);
        updateStatement.addNewColumnValue("LOCKGRANTED", new Timestamp(new java.util.Date().getTime()));
        updateStatement.addNewColumnValue("LOCKEDBY", localHost.getHostName() + " (" + localHost.getHostAddress() + ")");
        updateStatement.setWhereClause(database.escapeColumnName(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogTableName(), "ID") + " = 1 AND " + database.escapeColumnName(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogTableName(), "LOCKED") + " = "+ database.getDataTypeFactory().fromDescription("boolean").objectToString(false, database));

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);

    }
}