package liquibase.sqlgenerator.core;

import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.SybaseDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;

public class CreateDatabaseChangeLogTableGeneratorSybase implements SqlGenerator<CreateDatabaseChangeLogTableStatement> {
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return database instanceof SybaseDatabase;
    }

    public ValidationErrors validate(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("CREATE TABLE " + database.escapeTableName(database.getDefaultSchemaName(), database.getDatabaseChangeLogTableName()) + " (ID VARCHAR(150) NOT NULL, " +
                "AUTHOR VARCHAR(150) NOT NULL, " +
                "FILENAME VARCHAR(255) NOT NULL, " +
                "DATEEXECUTED " + database.getDateTimeType() + " NOT NULL, " +
                "ORDEREXECUTED NOT NULL UNIQUE, " +
                "MD5SUM VARCHAR(32) NULL, " +
                "DESCRIPTION VARCHAR(255) NULL, " +
                "COMMENTS VARCHAR(255) NULL, " +
                "TAG VARCHAR(255) NULL, " +
                "LIQUIBASE VARCHAR(10) NULL, " +
                "PRIMARY KEY(ID, AUTHOR, FILENAME))")
        };  //To change body of implemented methods use File | Settings | File Templates.
    }
}
