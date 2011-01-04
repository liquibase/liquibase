package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.database.core.SybaseDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;

public class CreateDatabaseChangeLogTableGeneratorSybase extends AbstractSqlGenerator<CreateDatabaseChangeLogTableStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
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
                "DATEEXECUTED " + TypeConverterFactory.getInstance().findTypeConverter(database).getDateTimeType() + " NOT NULL, " +
                "ORDEREXECUTED INT NOT NULL, " +
                "EXECTYPE VARCHAR(10) NOT NULL, " +
                "MD5SUM VARCHAR(35) NULL, " +
                "DESCRIPTION VARCHAR(255) NULL, " +
                "COMMENTS VARCHAR(255) NULL, " +
                "TAG VARCHAR(255) NULL, " +
                "LIQUIBASE VARCHAR(20) NULL, " +
                "PRIMARY KEY(ID, AUTHOR, FILENAME))")
        };  //To change body of implemented methods use File | Settings | File Templates.
    }
}
