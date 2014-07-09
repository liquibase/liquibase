package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.AddPrimaryKeyStatement;

public class AddPrimaryKeyGeneratorInformix extends AddPrimaryKeyGenerator {
	
    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddPrimaryKeyStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

        return (database instanceof InformixDatabase);
    }

    @Override
    public Action[] generateActions(AddPrimaryKeyStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

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

        return new Action[] {
                new UnparsedSql(sql.toString())
        };
    }
}
