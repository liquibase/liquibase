package liquibase.database.statement;

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
}
