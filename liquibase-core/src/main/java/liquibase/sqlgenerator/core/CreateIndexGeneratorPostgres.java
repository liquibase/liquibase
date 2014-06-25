package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.*;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.structure.core.Index;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CreateIndexGeneratorPostgres extends CreateIndexGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateIndexStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof PostgresDatabase;
    }

    @Override
    public Action[] generateActions(CreateIndexStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {

        Database database = env.getTargetDatabase();

        // Default filter of index creation:
        // creation of all indexes with associations are switched off.
        List<String> associatedWith = StringUtils.splitAndTrim(statement.getAssociatedWith(), ",");
        if (associatedWith != null && (associatedWith.contains(Index.MARK_PRIMARY_KEY) ||
            associatedWith.contains(Index.MARK_UNIQUE_CONSTRAINT) ||
            associatedWith.contains(Index.MARK_FOREIGN_KEY))) {
            return new Action[0];
        }

	    StringBuilder buffer = new StringBuilder();

	    buffer.append("CREATE ");
	    if (statement.isUnique() != null && statement.isUnique()) {
		    buffer.append("UNIQUE ");
	    }
	    buffer.append("INDEX ");

	    if (statement.getIndexName() != null) {
            // for postgres setting the schema name for the index name is invalid
            buffer.append(database.escapeObjectName(statement.getIndexName(), Index.class)).append(" ");
	    }
	    buffer.append("ON ");
	    buffer.append(database.escapeTableName(statement.getTableCatalogName(), statement.getTableSchemaName(), statement.getTableName())).append("(");
	    Iterator<String> iterator = Arrays.asList(statement.getColumns()).iterator();
	    while (iterator.hasNext()) {
		    String column = iterator.next();
		    buffer.append(database.escapeColumnName(statement.getTableCatalogName(), statement.getTableSchemaName(), statement.getTableName(), column));
		    if (iterator.hasNext()) {
			    buffer.append(", ");
		    }
	    }
	    buffer.append(")");

	    if (StringUtils.trimToNull(statement.getTablespace()) != null && database.supportsTablespaces()) {
		    if (database instanceof MSSQLDatabase || database instanceof SybaseASADatabase) {
			    buffer.append(" ON ").append(statement.getTablespace());
		    } else if (database instanceof DB2Database || database instanceof InformixDatabase) {
			    buffer.append(" IN ").append(statement.getTablespace());
		    } else {
			    buffer.append(" TABLESPACE ").append(statement.getTablespace());
		    }
	    }

	    return new Action[]{new UnparsedSql(buffer.toString())};
    }
}
