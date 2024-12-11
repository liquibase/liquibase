package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.RefreshLockDatabaseChangeLogStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.NetUtil;
import org.apache.tools.ant.util.StringUtils;

import java.sql.Timestamp;
import java.util.UUID;

public class RefreshLockDatabaseChangeLogGenerator extends AbstractSqlGenerator<RefreshLockDatabaseChangeLogStatement> {

    @Override
    public ValidationErrors validate(RefreshLockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }


    protected String getCharTypeName(Database database) {
        if (database instanceof MSSQLDatabase && ((MSSQLDatabase) database).sendsStringParametersAsUnicode()) {
            return "nvarchar";
        }
        return "varchar";
    }

    @Override
    public Sql[] generateSql(RefreshLockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	String liquibaseSchema = database.getLiquibaseSchemaName();
        String liquibaseCatalog = database.getLiquibaseCatalogName();
        String charTypeName = getCharTypeName(database);

        UpdateStatement updateStatement = new UpdateStatement(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogLockTableName());
        updateStatement.addNewColumnValue("LOCKED", true);
        updateStatement.addNewColumnValue("LOCKGRANTED", new Timestamp(new java.util.Date().getTime()));
        updateStatement.addNewColumnValue("LOCKEDBY", LockDatabaseChangeLogGenerator.getLockedBy());
        updateStatement.setWhereClause(database.escapeColumnName(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogTableName(), "ID")
                + " = 1 AND " + database.escapeColumnName(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogTableName(), "LOCKED")
                + " = " + DataTypeFactory.getInstance().fromDescription("boolean", database).objectToSql(true, database)
                + " AND " + database.escapeColumnName(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogTableName(), "LOCKEDBY")
                + " = " + DataTypeFactory.getInstance().fromDescription(charTypeName + "(255)", database).objectToSql(LockDatabaseChangeLogGenerator.getLockedBy(), database)
        );

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);

    }
}