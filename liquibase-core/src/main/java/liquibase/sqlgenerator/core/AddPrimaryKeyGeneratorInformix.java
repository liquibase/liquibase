package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddPrimaryKeyStatement;

public class AddPrimaryKeyGeneratorInformix extends AddPrimaryKeyGenerator {
	
    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddPrimaryKeyStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        return (database instanceof InformixDatabase);
    }
    
    @Override
    public Sql[] generateSql(AddPrimaryKeyStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ");
        sql.append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
        sql.append(" ADD CONSTRAINT PRIMARY KEY (");
        sql.append(database.escapeColumnNameList(statement.getColumnNames()));
        sql.append(")");
   	    if (statement.getConstraintName() != null) {
   	        sql.append(" CONSTRAINT ");
   	        sql.append(database.escapeConstraintName(statement.getConstraintName()));
   	    }

        return new Sql[] {
                new UnparsedSql(sql.toString())
        };
    }
}
