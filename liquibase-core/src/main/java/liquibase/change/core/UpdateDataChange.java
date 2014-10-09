package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
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

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validate = super.validate(database);
        validate.checkRequiredField("columns", getColumns());
        return validate;
    }

    @Override
    @DatabaseChangeProperty(description = "Data to update", requiredForDatabase = "all")
    public List<ColumnConfig> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    @Override
    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        columns.remove(column);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

    	boolean needsPreparedStatement = false;
        for (ColumnConfig column : getColumns()) {
            if (column.getValueBlobFile() != null) {
                needsPreparedStatement = true;
            }
            if (column.getValueClobFile() != null
                || (column.getType() != null && column.getType().equalsIgnoreCase("CLOB"))) {
                needsPreparedStatement = true;
            }
        }

        if (needsPreparedStatement) {
            UpdateExecutablePreparedStatement statement = new UpdateExecutablePreparedStatement(database, catalogName, schemaName, tableName, columns, getChangeSet(), this.getResourceAccessor());
            
            statement.setWhereClause(where);
            
            for (ColumnConfig whereParam : whereParams) {
                if (whereParam.getName() != null) {
                    statement.addWhereColumnName(whereParam.getName());
                }
                statement.addWhereParameter(whereParam.getValueObject());
            }
            
            return new SqlStatement[] {
                    statement
            };
        }
    	
        UpdateStatement statement = new UpdateStatement(getCatalogName(), getSchemaName(), getTableName());

        for (ColumnConfig column : getColumns()) {
            statement.addNewColumnValue(column.getName(), column.getValueObject());
        }

        statement.setWhereClause(where);

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

    @Override
    public ChangeStatus checkStatus(Database database) {
        return new ChangeStatus().unknown("Cannot check updateData status");
    }

    @Override
    public String getConfirmationMessage() {
        return "Data updated in " + getTableName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        ParsedNode whereParams = parsedNode.getChild(null, "whereParams");
        if (whereParams != null) {
            for (ParsedNode param : whereParams.getChildren(null, "param")) {
                ColumnConfig columnConfig = new ColumnConfig();
                try {
                    columnConfig.load(param, resourceAccessor);
                } catch (ParsedNodeException e) {
                    e.printStackTrace();
                }
                addWhereParam(columnConfig);
            }
        }
    }
}
