package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.CreateTableStatement;

public class CreateDatabaseChangeLogTableGenerator extends AbstractSqlGenerator<CreateDatabaseChangeLogTableStatement> {

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return !(database instanceof SybaseDatabase);
    }

    @Override
    public ValidationErrors validate(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String charTypeName = getCharTypeName(database);
        String dateTimeTypeString = getDateTimeTypeString(database);
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .setTablespace(database.getLiquibaseTablespaceName())
                .addColumn("ID", DataTypeFactory.getInstance().fromDescription(charTypeName + "(" + getIdColumnSize() + ")", database), null, null, new NotNullConstraint())
                .addColumn("AUTHOR", DataTypeFactory.getInstance().fromDescription(charTypeName + "(" + getAuthorColumnSize() + ")", database), null, null, new NotNullConstraint())
                .addColumn("FILENAME", DataTypeFactory.getInstance().fromDescription(charTypeName + "(" + getFilenameColumnSize() + ")", database), null, null, new NotNullConstraint())
                .addColumn("DATEEXECUTED", DataTypeFactory.getInstance().fromDescription(dateTimeTypeString, database), null, null, new NotNullConstraint())
                .addColumn("ORDEREXECUTED", DataTypeFactory.getInstance().fromDescription("int", database), null, null, new NotNullConstraint())
                .addColumn("EXECTYPE", DataTypeFactory.getInstance().fromDescription(charTypeName + "(10)", database), null, null, new NotNullConstraint())
                .addColumn("MD5SUM", DataTypeFactory.getInstance().fromDescription(charTypeName + "(35)", database))
                .addColumn("DESCRIPTION", DataTypeFactory.getInstance().fromDescription(charTypeName + "(255)", database))
                .addColumn("COMMENTS", DataTypeFactory.getInstance().fromDescription(charTypeName + "(255)", database))
                .addColumn("TAG", DataTypeFactory.getInstance().fromDescription(charTypeName + "(255)", database))
                .addColumn("LIQUIBASE", DataTypeFactory.getInstance().fromDescription(charTypeName + "(20)", database))
                .addColumn("CONTEXTS", DataTypeFactory.getInstance().fromDescription(charTypeName + "("+getContextsSize()+")", database))
                .addColumn("LABELS", DataTypeFactory.getInstance().fromDescription(charTypeName + "("+getLabelsSize()+")", database))
                .addColumn("DEPLOYMENT_ID", DataTypeFactory.getInstance().fromDescription(charTypeName+"(10)", database));

        return SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database);
    }

    protected String getCharTypeName(Database database) {
        if ((database instanceof MSSQLDatabase) && ((MSSQLDatabase) database).sendsStringParametersAsUnicode()) {
            return "nvarchar";
        }
        return "varchar";
    }

    protected String getDateTimeTypeString(Database database) {
        if (database instanceof MSSQLDatabase) {
                    return "datetime2(3)";
        }
        return "datetime";
    }

    protected String getLabelsSize() {
        return "255";
    }

    protected String getContextsSize() {
        return "255";
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
