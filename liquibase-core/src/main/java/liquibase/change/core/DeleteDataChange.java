package liquibase.change.core;

import liquibase.change.ChangeMetaData;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import  liquibase.ExecutionEnvironment;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.Statement;
import liquibase.statement.core.DeleteDataStatement;

@DatabaseChange(name="delete", description = "Deletes data from an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class DeleteDataChange extends AbstractModifyDataChange {


    @Override
    public Statement[] generateStatements(ExecutionEnvironment env) {

        DeleteDataStatement statement = new DeleteDataStatement(getCatalogName(), getSchemaName(), getTableName());

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
    public String getConfirmationMessage() {
        return "Data deleted from " + getTableName();
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
