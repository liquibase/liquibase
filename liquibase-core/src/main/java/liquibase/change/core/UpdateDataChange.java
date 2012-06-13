package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.TextNode;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.UpdateExecutablePreparedStatement;
import liquibase.statement.core.UpdateStatement;

@ChangeClass(name = "update", description = "Update Data", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class UpdateDataChange extends AbstractChange implements ChangeWithColumns<ColumnConfig> {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns;

    @TextNode(nodeName="where")
    private String whereClause;

    public UpdateDataChange() {
        columns = new ArrayList<ColumnConfig>();
    }

    @ChangeProperty(mustApplyTo ="column.relation.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @ChangeProperty(mustApplyTo ="column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo = "table")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @ChangeProperty(requiredForDatabase = "all")
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

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public SqlStatement[] generateStatements(Database database) {

    	boolean needsPreparedStatement = false;
        for (ColumnConfig column : columns) {
            if (column.getValueBlob() != null) {
                needsPreparedStatement = true;
            }
            if (column.getValueClob() != null) {
                needsPreparedStatement = true;
            }
            if (column.getValueText() != null && database instanceof InformixDatabase) {
                needsPreparedStatement = true;
            }
        }

        if (needsPreparedStatement) {
            return new SqlStatement[] { 
            		new UpdateExecutablePreparedStatement(database, catalogName, schemaName, tableName, columns) 
            };
        }
    	
        UpdateStatement statement = new UpdateStatement(getCatalogName(), getSchemaName(), getTableName());

        for (ColumnConfig column : columns) {
            statement.addNewColumnValue(column.getName(), column.getValueObject());
        }

        statement.setWhereClause(whereClause);

        return new SqlStatement[]{
                statement
        };
    }

    public String getConfirmationMessage() {
        return "Data updated in " + getTableName();
    }

}
