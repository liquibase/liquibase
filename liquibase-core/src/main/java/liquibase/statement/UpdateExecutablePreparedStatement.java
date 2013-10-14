package liquibase.statement;

import java.util.List;

import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;

public class UpdateExecutablePreparedStatement extends ExecutablePreparedStatementBase {

	public UpdateExecutablePreparedStatement(Database database, ChangeSet changeSet, String catalogName, String schemaName, String tableName, List<ColumnConfig> columns) {
		super(database, changeSet, catalogName, schemaName, tableName, columns);
	}

	@Override
	protected String generateSql(List<ColumnConfig> cols) {

		StringBuilder sql = new StringBuilder("UPDATE ").append(database.escapeTableName(getCatalogName(), getSchemaName(), getTableName()));

		StringBuilder params = new StringBuilder(" SET ");
	    for(ColumnConfig column : getColumns()) {
	    	params.append(database.escapeColumnName(getCatalogName(), getSchemaName(), getTableName(), column.getName()));
	    	params.append(" = ");
	        params.append("?, ");
	        cols.add(column);
	    }
	    params.deleteCharAt(params.lastIndexOf(" "));
	    params.deleteCharAt(params.lastIndexOf(","));
	    sql.append(params);
		return sql.toString();		
	}
}
