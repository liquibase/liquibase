package liquibase.informix.sqlgenerator.core;

import liquibase.database.Database;
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
public class InformixCreateDatabaseChangeLogTableGenerator extends CreateDatabaseChangeLogTableGenerator {

	@Override
    public Sql[] generateSql(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	/* XXX:
    	 * Because Informix has some limitations on the primary key column cumulative size (same is for unique indices),
    	 * the ID's column has been made the sole primary key, and also has a reduced size. 
    	 * 
    	 * The original configuration provided by the base class causes the database changelog table not to be created.
    	 */
    	   	
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .addPrimaryKeyColumn("ID", database.getDataTypeFactory().fromDescription("VARCHAR(" + getIdColumnSize() + ")"), null, null, null, new NotNullConstraint())
                .addColumn("AUTHOR",database.getDataTypeFactory().fromDescription("VARCHAR(" + getAuthorColumnSize() + ")"), null, null, null, new NotNullConstraint())
                .addColumn("FILENAME", database.getDataTypeFactory().fromDescription("VARCHAR(" + getFilenameColumnSize() + ")"), null, null, null, new NotNullConstraint())
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
