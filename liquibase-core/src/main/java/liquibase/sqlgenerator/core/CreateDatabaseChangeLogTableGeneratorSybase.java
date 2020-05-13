package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

public class CreateDatabaseChangeLogTableGeneratorSybase extends AbstractSqlGenerator<CreateDatabaseChangeLogTableStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return database instanceof SybaseDatabase;
    }

    @Override
    public ValidationErrors validate(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{
                new UnparsedSql("CREATE TABLE " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()) + " (ID VARCHAR(150) NOT NULL, " +
                        "AUTHOR VARCHAR(150) NOT NULL, " +
                        "FILENAME VARCHAR(255) NOT NULL, " +
                        "DATEEXECUTED " + DataTypeFactory.getInstance().fromDescription("datetime", database).toDatabaseDataType(database) + " NOT NULL, " +
                        "ORDEREXECUTED INT NOT NULL, " +
                        "EXECTYPE VARCHAR(10) NOT NULL, " +
                        "MD5SUM VARCHAR(35) NULL, " +
                        "DESCRIPTION VARCHAR(255) NULL, " +
                        "COMMENTS VARCHAR(255) NULL, " +
                        "TAG VARCHAR(255) NULL, " +
                        "LIQUIBASE VARCHAR(20) NULL, " +
                        "CONTEXTS VARCHAR(255) NULL, " +
                        "LABELS VARCHAR(255) NULL, " +
                        "DEPLOYMENT_ID VARCHAR(10) NULL, " +
                        "PRIMARY KEY(ID, AUTHOR, FILENAME))",
                        getAffectedTable(database))
        };
    }

    protected Relation getAffectedTable(Database database) {
        return new Table().setName(database.getDatabaseChangeLogTableName()).setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName());
    }
}
