package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.CreateTableStatement;

public class CreateDatabaseChangeLogTableGenerator extends AbstractSqlGenerator<CreateDatabaseChangeLogTableStatement> {

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return (!(database instanceof SybaseDatabase));
    }

    @Override
    public ValidationErrors validate(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	
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

        return SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database);
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
