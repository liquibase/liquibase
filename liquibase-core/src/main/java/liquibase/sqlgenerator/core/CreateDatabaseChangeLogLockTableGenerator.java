package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.change.ColumnConfig;
import liquibase.exception.UnsupportedException;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.CreateTableStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateDatabaseChangeLogLockTableGenerator extends AbstractSqlGenerator<CreateDatabaseChangeLogLockTableStatement> {

    @Override
    public ValidationErrors validate(CreateDatabaseChangeLogLockTableStatement createDatabaseChangeLogLockTableStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(CreateDatabaseChangeLogLockTableStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .setTablespace(database.getLiquibaseTablespaceName())
                .addPrimaryKeyColumn("ID", DataTypeFactory.getInstance().fromDescription("INT", database), null, null, null, new NotNullConstraint())
                .addColumn("LOCKED", DataTypeFactory.getInstance().fromDescription("BOOLEAN", database), null, new ColumnConstraint[]{new NotNullConstraint()})
                .addColumn("LOCKGRANTED", DataTypeFactory.getInstance().fromDescription("DATETIME", database))
                .addColumn("LOCKEDBY", DataTypeFactory.getInstance().fromDescription("VARCHAR(255)", database));
        List<Action> actions = new ArrayList<Action>();

        actions.addAll(Arrays.asList(StatementLogicFactory.getInstance().generateActions(createTableStatement, env)));

        return actions.toArray(new Action[actions.size()]);
    }
}
