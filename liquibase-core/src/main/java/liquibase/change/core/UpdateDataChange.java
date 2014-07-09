package liquibase.change.core;

import liquibase.change.*;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.Statement;
import liquibase.statement.core.UpdateDataStatement;

import java.util.ArrayList;
import java.util.List;

@DatabaseChange(name = "update", description = "Updates data in an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class UpdateDataChange extends AbstractModifyDataChange implements ChangeWithColumns<ColumnConfig> {

    private List<ColumnConfig> columns;

    public UpdateDataChange() {
        columns = new ArrayList<ColumnConfig>();
    }

    @Override
    public ValidationErrors validate(ExecutionEnvironment env) {
        ValidationErrors validate = super.validate(env);
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
    public Statement[] generateStatements(ExecutionEnvironment env) {

    	boolean needsPreparedStatement = false;
        for (ColumnConfig column : getColumns()) {
            if (column.getValueBlobFile() != null) {
                needsPreparedStatement = true;
            }
            if (column.getValueClobFile() != null) {
                needsPreparedStatement = true;
            }
        }

        UpdateDataStatement statement = new UpdateDataStatement(getCatalogName(), getSchemaName(), getTableName());
        if (needsPreparedStatement) {
            statement.setNeedsPreparedStatement(true);
        }

        for (ColumnConfig column : getColumns()) {
            statement.addNewColumnValue(column.getName(), column.getValueObject());
        }

        statement.setWhere(where);

        for (ColumnConfig whereParam : whereParams) {
            if (whereParam.getName() != null) {
                statement.addWhereColumnNames(whereParam.getName());
            }
            statement.addWhereParameters(whereParam.getValueObject());
        }

        return new Statement[]{
                statement
        };
    }

    @Override
    public ChangeStatus checkStatus(ExecutionEnvironment env) {
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
