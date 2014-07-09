package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.MySQLDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

/**
 * SQLite does not support this ALTER TABLE operation until now.
 * For more information see: http://www.sqlite.org/omitted.html.
 * This is a small work around...
 */
public class AddAutoIncrementGeneratorMySQL extends AddAutoIncrementGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof MySQLDatabase;
    }

    @Override
    public Action[] generateActions(AddAutoIncrementStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {

    	Action[] actions = super.generateActions(statement, env, chain);

    	if(statement.getStartWith() != null){
            MySQLDatabase mysqlDatabase = (MySQLDatabase) env.getTargetDatabase();
	        String alterTableSql = "ALTER TABLE "
	            + mysqlDatabase.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
	            + " "
	            + mysqlDatabase.getTableOptionAutoIncrementStartWithClause(statement.getStartWith());

	        actions = concact(actions, new UnparsedSql(alterTableSql));
    	}

        return actions;
    }

	private Action[] concact(Action[] origSql, UnparsedSql unparsedSql) {
		Action[] changedSql = new Action[origSql.length+1];
		System.arraycopy(origSql, 0, changedSql, 0, origSql.length);
		changedSql[origSql.length] = unparsedSql;

		return changedSql;
	}

	private DatabaseObject getAffectedTable(AddAutoIncrementStatement statement) {
		return new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()));
	}
}