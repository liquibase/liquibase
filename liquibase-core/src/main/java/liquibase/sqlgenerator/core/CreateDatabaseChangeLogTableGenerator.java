package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.informix.sqlgenerator.core.InformixCreateDatabaseChangeLogTableGenerator;
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
    	
    	if (database instanceof InformixDatabase) {
    		return new InformixCreateDatabaseChangeLogTableGenerator().generateSql(statement, database, sqlGeneratorChain);
    	}
    	
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .addPrimaryKeyColumn("ID", database.getDataTypeFactory().fromDescription("VARCHAR(" + getIdColumnSize() + ")"), null, null, null, new NotNullConstraint())
                        .addPrimaryKeyColumn("AUTHOR",database.getDataTypeFactory().fromDescription("VARCHAR(" + getAuthorColumnSize() + ")"), null, null, null, new NotNullConstraint())
                        .addPrimaryKeyColumn("FILENAME", database.getDataTypeFactory().fromDescription("VARCHAR(" + getFilenameColumnSize() + ")"), null, null, null, new NotNullConstraint())
                        .addColumn("DATEEXECUTED", database.getDataTypeFactory().fromDescription("datetime"), null, new NotNullConstraint())
                        .addColumn("ORDEREXECUTED", database.getDataTypeFactory().fromDescription("INT"), new NotNullConstraint())
                        .addColumn("EXECTYPE", database.getDataTypeFactory().fromDescription("VARCHAR(10)"), new NotNullConstraint())
                        .addColumn("MD5SUM", database.getDataTypeFactory().fromDescription("VARCHAR(35)"))
                        .addColumn("DESCRIPTION", database.getDataTypeFactory().fromDescription("VARCHAR(255)"))
                        .addColumn("COMMENTS", database.getDataTypeFactory().fromDescription("VARCHAR(255)"))
                        .addColumn("TAG", database.getDataTypeFactory().fromDescription("VARCHAR(255)"))
                        .addColumn("LIQUIBASE", database.getDataTypeFactory().fromDescription("VARCHAR(20)"));

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
