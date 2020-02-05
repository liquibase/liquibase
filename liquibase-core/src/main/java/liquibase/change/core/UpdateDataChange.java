package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.UpdateExecutablePreparedStatement;
import liquibase.statement.core.UpdateStatement;

import java.util.ArrayList;
import java.util.List;

import static liquibase.change.ChangeParameterMetaData.ALL;

@DatabaseChange(name = "update", description = "Updates data in an existing table"
        , priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class UpdateDataChange extends AbstractModifyDataChange implements ChangeWithColumns<ColumnConfig> {

    private List<ColumnConfig> columns;

    public UpdateDataChange() {
        columns = new ArrayList<>();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validate = super.validate(database);
        validate.checkRequiredField("columns", getColumns());
        return validate;
    }

    @Override
    @DatabaseChangeProperty(description = "Data to update", requiredForDatabase = ALL)
    public List<ColumnConfig> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

    	boolean needsPreparedStatement = false;
        for (ColumnConfig column : getColumns()) {
            if (column.getValueBlobFile() != null) {
                needsPreparedStatement = true;
            }
            if (column.getValueClobFile() != null) {
                needsPreparedStatement = true;
            }

            if ((database instanceof OracleDatabase) && (column.getType() != null) && "CLOB".equalsIgnoreCase(column
                .getType()) && (column.getValue() != null) && (column.getValue().length() >= 4000)) {
                needsPreparedStatement = true;
            }
        }

        if (needsPreparedStatement) {
            UpdateExecutablePreparedStatement statement = new UpdateExecutablePreparedStatement(database, catalogName, schemaName, tableName, columns, getChangeSet(), this.getResourceAccessor());
            
            statement.setWhereClause(where);
            
            for (Param whereParam : whereParams) {
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

        for (Param whereParam : whereParams) {
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

}
