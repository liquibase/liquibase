package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DeleteStatement;

@DatabaseChange(name="delete", description = "Delete Data", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class DeleteDataChange extends AbstractModifyDataChange {

    public SqlStatement[] generateStatements(Database database) {

        DeleteStatement statement = new DeleteStatement(getCatalogName(), getSchemaName(), getTableName());

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
        return "Data deleted from " + getTableName();
    }

}
