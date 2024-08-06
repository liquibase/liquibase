package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;
import lombok.Setter;

@Getter
public class FindForeignKeyConstraintsStatement extends AbstractSqlStatement {

    public static final String RESULT_COLUMN_BASE_TABLE_NAME        = "TABLE_NAME";
    public static final String RESULT_COLUMN_BASE_TABLE_COLUMN_NAME = "COLUMN_NAME";
    public static final String RESULT_COLUMN_FOREIGN_TABLE_NAME     = "REFERENCED_TABLE_NAME";
    public static final String RESULT_COLUMN_FOREIGN_COLUMN_NAME    = "REFERENCED_COLUMN_NAME";
    public static final String RESULT_COLUMN_CONSTRAINT_NAME        = "CONSTRAINT_NAME";

    private final String baseTableCatalogName;
    @Setter
    private String baseTableSchemaName;
    @Setter
    private String baseTableName;

    public FindForeignKeyConstraintsStatement(String baseTableCatalogName, String baseTableSchemaName,
                                              String baseTableName) {
        this.baseTableCatalogName = baseTableCatalogName;
        this.baseTableSchemaName = baseTableSchemaName;
        this.baseTableName = baseTableName;
    }

}
