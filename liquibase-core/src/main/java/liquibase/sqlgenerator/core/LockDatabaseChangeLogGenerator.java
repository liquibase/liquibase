package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.NetUtil;

import java.sql.Timestamp;

public class LockDatabaseChangeLogGenerator extends AbstractSqlGenerator<LockDatabaseChangeLogStatement> {

    @Override
    public ValidationErrors validate(LockDatabaseChangeLogStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new ValidationErrors();
    }

    protected static final String hostname;
    protected static final String hostaddress;

    static {
        try {
            hostname = NetUtil.getLocalHostName();
            hostaddress = NetUtil.getLocalHostAddress();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    public Action[] generateActions(LockDatabaseChangeLogStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

    	String liquibaseSchema = database.getLiquibaseSchemaName();
        String liquibaseCatalog = database.getLiquibaseCatalogName();



        UpdateStatement updateStatement = new UpdateStatement(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogLockTableName());
        updateStatement.addNewColumnValue("LOCKED", true);
        updateStatement.addNewColumnValue("LOCKGRANTED", new Timestamp(new java.util.Date().getTime()));
        updateStatement.addNewColumnValue("LOCKEDBY", hostname + " (" + hostaddress + ")");
        updateStatement.setWhereClause(database.escapeColumnName(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogTableName(), "ID") + " = 1 AND " + database.escapeColumnName(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogTableName(), "LOCKED") + " = "+ DataTypeFactory.getInstance().fromDescription("boolean", database).objectToSql(false, database));

        return StatementLogicFactory.getInstance().generateActions(updateStatement, env);

    }
}