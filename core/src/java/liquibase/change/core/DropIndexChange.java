package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropIndexStatement;
import liquibase.util.StringUtils;

/**
 * Drops an existing index.
 */
public class DropIndexChange extends AbstractChange {

    private String schemaName;
    private String indexName;
    private String tableName;

    public DropIndexChange() {
        super("dropIndex", "Drop Index", ChangeMetaData.PRIORITY_DEFAULT);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
            new DropIndexStatement(getIndexName(), getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getTableName())
        };
    }

    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " dropped from table " + getTableName();
    }
}
