package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.CreateDatabaseChangeLogTableGenerator;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.CreateTableStatement;

/**
 * 
 * @author Ivaylo Slavov
 */
public class CreateDatabaseChangeLogTableGeneratorInformix extends CreateDatabaseChangeLogTableGenerator {

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return database instanceof InformixDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public Sql[] generateSql(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	/* Because Informix has some limitations on the primary key column cumulative size (same is for unique indices),
    	 * the ID's column has been made the sole primary key, and also has a reduced size. 
    	 * 
    	 * The original configuration provided by the base class causes the database changelog table not to be created.
    	 */
    	   	
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .addPrimaryKeyColumn("ID", DataTypeFactory.getInstance().fromDescription("VARCHAR(" + getIdColumnSize() + ")"), null, null, null, new NotNullConstraint())
                .addColumn("AUTHOR",DataTypeFactory.getInstance().fromDescription("VARCHAR(" + getAuthorColumnSize() + ")"), null, null, null, new NotNullConstraint())
                .addColumn("FILENAME", DataTypeFactory.getInstance().fromDescription("VARCHAR(" + getFilenameColumnSize() + ")"), null, null, null, new NotNullConstraint())
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

    @Override
    protected String getIdColumnSize() {
        return "63";
    }

    @Override
    protected String getAuthorColumnSize() {
        return "63";
    }

    @Override
    protected String getFilenameColumnSize() {
        return "200";
    }
}
