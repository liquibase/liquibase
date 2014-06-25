package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.database.Database;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.CreateTableStatement;

public class CreateDatabaseChangeLogTableGenerator extends AbstractSqlGenerator<CreateDatabaseChangeLogTableStatement> {

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, ExecutionEnvironment env) {
        return (!(env.getTargetDatabase() instanceof SybaseDatabase));
    }

    @Override
    public ValidationErrors validate(CreateDatabaseChangeLogTableStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(CreateDatabaseChangeLogTableStatement statement, ExecutionEnvironment env, StatementLogicChain statementLogicChain) {

        Database database = env.getTargetDatabase();

        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .setTablespace(database.getLiquibaseTablespaceName())
                .addColumn("ID", DataTypeFactory.getInstance().fromDescription("VARCHAR(" + getIdColumnSize() + ")", database), null, null, null, new NotNullConstraint())
                .addColumn("AUTHOR", DataTypeFactory.getInstance().fromDescription("VARCHAR(" + getAuthorColumnSize() + ")", database), null, null, null, new NotNullConstraint())
                .addColumn("FILENAME", DataTypeFactory.getInstance().fromDescription("VARCHAR(" + getFilenameColumnSize() + ")", database), null, null, null, new NotNullConstraint())
                .addColumn("DATEEXECUTED", DataTypeFactory.getInstance().fromDescription("datetime", database), null, null, new NotNullConstraint())
                .addColumn("ORDEREXECUTED", DataTypeFactory.getInstance().fromDescription("INT", database), new NotNullConstraint())
                .addColumn("EXECTYPE", DataTypeFactory.getInstance().fromDescription("VARCHAR(10)", database), new NotNullConstraint())
                .addColumn("MD5SUM", DataTypeFactory.getInstance().fromDescription("VARCHAR(35)", database))
                .addColumn("DESCRIPTION", DataTypeFactory.getInstance().fromDescription("VARCHAR(255)", database))
                .addColumn("COMMENTS", DataTypeFactory.getInstance().fromDescription("VARCHAR(255)", database))
                .addColumn("TAG", DataTypeFactory.getInstance().fromDescription("VARCHAR(255)", database))
                .addColumn("LIQUIBASE", DataTypeFactory.getInstance().fromDescription("VARCHAR(20)", database));

        return StatementLogicFactory.getInstance().generateActions(createTableStatement, env);
    }

    protected String getIdColumnSize() {
        return "255";
    }

    protected String getAuthorColumnSize() {
        return "255";
    }

    protected String getFilenameColumnSize() {
        return "255";
    }
}
