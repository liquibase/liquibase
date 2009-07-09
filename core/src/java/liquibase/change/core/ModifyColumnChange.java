package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.ModifyColumnsStatement;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Modifies the data type of an existing column.
 */
public class ModifyColumnChange extends AbstractChange implements ChangeWithColumns {

    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns;

    public ModifyColumnChange() {
        super("modifyColumn", "Modify Column", ChangeMetaData.PRIORITY_DEFAULT);
        columns = new ArrayList<ColumnConfig>();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnConfig> getColumns() {
    		return columns;
    }

    public void addColumn(ColumnConfig column) {
      	columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
      	columns.remove(column);
    }

    public SqlStatement[] generateStatements(Database database) {

        return new SqlStatement[] {
            new ModifyColumnsStatement(getSchemaName(), getTableName(), getColumns().toArray(new ColumnConfig[getColumns().size()]))
        };
    }
    
    public String getConfirmationMessage() {
    		List<String> names = new ArrayList<String>(columns.size());
    		for (ColumnConfig col : columns) {
          	names.add(col.getName() + "(" + col.getType() + ")");
    		}

        return "Columns " + StringUtils.join(names, ",") + " of " + getTableName() + " modified";
    }
}
