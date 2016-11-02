package liquibase.sqlgenerator.core;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import liquibase.change.AddColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.structure.core.Index;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.util.StringUtils;

public class CreateIndexGeneratorPostgres extends CreateIndexGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateIndexStatement statement, Database database) {
        return database instanceof PostgresDatabase;
    }

    @Override
    public Sql[] generateSql(CreateIndexStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        // Default filter of index creation:
        // creation of all indexes with associations are switched off.
        List<String> associatedWith = StringUtils.splitAndTrim(statement.getAssociatedWith(), ",");
        if (associatedWith != null && (associatedWith.contains(Index.MARK_PRIMARY_KEY) ||
            associatedWith.contains(Index.MARK_UNIQUE_CONSTRAINT) ||
            associatedWith.contains(Index.MARK_FOREIGN_KEY))) {
            return new Sql[0];
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
	    Iterator<AddColumnConfig> iterator = Arrays.asList(statement.getColumns()).iterator();
	    while (iterator.hasNext()) {
		    AddColumnConfig column = iterator.next();
            if (column.getComputed() == null) {
                buffer.append(database.escapeColumnName(statement.getTableCatalogName(), statement.getTableSchemaName(), statement.getTableName(), column.getName(), false));
            } else {
                if (column.getComputed()) {
                    buffer.append(column.getName());
                } else {
                    buffer.append(database.escapeColumnName(statement.getTableCatalogName(), statement.getTableSchemaName(), statement.getTableName(), column.getName()));
                }
            }
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

        if (statement.isClustered() != null && statement.isClustered()) {
            return new Sql[]{
                    new UnparsedSql(buffer.toString(), getAffectedIndex(statement)),
                    new UnparsedSql("CLUSTER " + database.escapeTableName(statement.getTableCatalogName(), statement.getTableSchemaName(), statement.getTableName()) + " USING " + database.escapeObjectName(statement.getIndexName(), Index.class))
            };
        } else {
            return new Sql[]{new UnparsedSql(buffer.toString(), getAffectedIndex(statement))};
        }
    }
}
