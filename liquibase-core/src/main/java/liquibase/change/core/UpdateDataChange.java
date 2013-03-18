package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.UpdateExecutablePreparedStatement;
import liquibase.statement.core.UpdateStatement;

import java.util.ArrayList;
import java.util.List;

@DatabaseChange(name = "update", description = "Updates data in an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class UpdateDataChange extends AbstractModifyDataChange implements ChangeWithColumns<ColumnConfig> {

    private List<ColumnConfig> columns;

    public UpdateDataChange() {
        columns = new ArrayList<ColumnConfig>();
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", description = "Data to update")
    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        columns.remove(column);
    }

    public SqlStatement[] generateStatements(Database database) {

    	boolean needsPreparedStatement = false;
        for (ColumnConfig column : getColumns()) {
            if (column.getValueBlobFile() != null) {
                needsPreparedStatement = true;
            }
            if (column.getValueClobFile() != null) {
                needsPreparedStatement = true;
            }
        }

        if (needsPreparedStatement) {
            return new SqlStatement[] { 
            		new UpdateExecutablePreparedStatement(database, catalogName, schemaName, tableName, columns)
            };
        }
    	
        UpdateStatement statement = new UpdateStatement(getCatalogName(), getSchemaName(), getTableName());

        for (ColumnConfig column : getColumns()) {
            statement.addNewColumnValue(column.getName(), column.getValueObject());
        }

        statement.setWhereClause(whereClause);

        for (ColumnConfig whereParam : whereParams) {
            if (whereParam.getName() != null) {
                statement.addWhereColumnName(whereParam.getName());
            }
            statement.addWhereParameter(whereParam.getValueObject());
        }

        return new SqlStatement[]{
                statement
        };
    }

    public String getConfirmationMessage() {
        return "Data updated in " + getTableName();
    }

}
