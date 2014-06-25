package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

import java.util.List;

public class CopyRowsStatement extends AbstractStatement {

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

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
                new Table().setName(getTargetTable()).setSchema(null, null)
        };
    }
}
