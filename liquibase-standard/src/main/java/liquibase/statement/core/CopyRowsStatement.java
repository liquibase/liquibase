package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

import java.util.List;

@Getter
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

}
