package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;

import java.util.List;

public class CopyRowsStatement extends AbstractSqlStatement {

    private final String sourceTable;
    private final String targetTable;
    private final List<ColumnConfig> copyColumns;


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
