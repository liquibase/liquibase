package liquibase.sqlgenerator.core;

import liquibase.database.Database;
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
        return (!(database instanceof SybaseDatabase));
    }

    public ValidationErrors validate(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .setTablespace(database.getLiquibaseTablespaceName())
                .addPrimaryKeyColumn("ID", DataTypeFactory.getInstance().fromDescription("VARCHAR(" + getIdColumnSize() + ")"), null, null, null, new NotNullConstraint())
                .addPrimaryKeyColumn("AUTHOR", DataTypeFactory.getInstance().fromDescription("VARCHAR(" + getAuthorColumnSize() + ")"), null, null, null, new NotNullConstraint())
                .addPrimaryKeyColumn("FILENAME", DataTypeFactory.getInstance().fromDescription("VARCHAR(" + getFilenameColumnSize() + ")"), null, null, null, new NotNullConstraint())
                .addColumn("DATEEXECUTED", DataTypeFactory.getInstance().fromDescription("datetime"), null, new NotNullConstraint())
                .addColumn("ORDEREXECUTED", DataTypeFactory.getInstance().fromDescription("INT"), new NotNullConstraint())
                .addColumn("EXECTYPE", DataTypeFactory.getInstance().fromDescription("VARCHAR(10)"), new NotNullConstraint())
                .addColumn("MD5SUM", DataTypeFactory.getInstance().fromDescription("VARCHAR(35)"))
                .addColumn("DESCRIPTION", DataTypeFactory.getInstance().fromDescription("VARCHAR(255)"))
                .addColumn("COMMENTS", DataTypeFactory.getInstance().fromDescription("VARCHAR(255)"))
                .addColumn("TAG", DataTypeFactory.getInstance().fromDescription("VARCHAR(255)"))
                .addColumn("LIQUIBASE", DataTypeFactory.getInstance().fromDescription("VARCHAR(20)"));

        return SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database);
    }

    protected String getIdColumnSize() {
        return "63";
    }

    protected String getAuthorColumnSize() {
        return "63";
    }

    protected String getFilenameColumnSize() {
        return "200";
    }
}
