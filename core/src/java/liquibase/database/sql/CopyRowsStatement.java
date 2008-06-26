package liquibase.database.sql;

import java.util.List;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class CopyRowsStatement implements SqlStatement {

	private String sourceTable;
	private String targetTable;
	private List<ColumnConfig> copyColumns;
	
	
	public CopyRowsStatement(String sourceTable, String targetTable, 
			List<ColumnConfig> copyColumns) {
		this.sourceTable = sourceTable;
		this.targetTable = targetTable;
		this.copyColumns = copyColumns;
	}
	
	public String getSourceTable() {
		return this.sourceTable;
	}
	
	public String getTargetTable() {
		return this.targetTable;
	}
	
	public List<ColumnConfig> getCopyColumns() {
		return this.copyColumns;
	}

	public String getEndDelimiter(Database database) {
		return ";";
	}

	public String getSqlStatement(Database database)
			throws StatementNotSupportedOnDatabaseException {
		if (!supportsDatabase(database)) {
			throw new StatementNotSupportedOnDatabaseException(this, database);
		}
		StringBuffer sql = new StringBuffer();
		if (database instanceof SQLiteDatabase) {
			sql.append("INSERT INTO `"+getTargetTable()+"` SELECT ");
			for (int i=0;i<getCopyColumns().size();i++) {
				ColumnConfig column = getCopyColumns().get(i);
				if (i>0) {
					sql.append(",");
				}
				sql.append("`"+column.getName()+"`");
			}
			sql.append(" FROM `"+getSourceTable()+"`");
		}
		return sql.toString();
	}

	public boolean supportsDatabase(Database database) {
		return (database instanceof SQLiteDatabase);
	}

}
