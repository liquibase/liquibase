package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;

public class CreateDatabaseChangeLogTableGeneratorSybase extends AbstractSqlGenerator<CreateDatabaseChangeLogTableStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof SybaseDatabase;
    }

    @Override
    public ValidationErrors validate(CreateDatabaseChangeLogTableStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(CreateDatabaseChangeLogTableStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        return new Action[] {
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
                "PRIMARY KEY(ID, AUTHOR, FILENAME))")
        };
    }
}
