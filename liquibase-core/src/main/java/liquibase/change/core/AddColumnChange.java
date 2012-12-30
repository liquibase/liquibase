package liquibase.change.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.exception.ValidationErrors;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.*;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.SetColumnRemarksStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.StringUtils;

/**
 * Adds a column to an existing table.
 */
@DatabaseChange(name="addColumn", description = "Add Column", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class AddColumnChange extends AbstractChange implements ChangeWithColumns<AddColumnConfig> {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private final List<AddColumnConfig> columns = new ArrayList<AddColumnConfig>();

    @DatabaseChangeProperty(mustApplyTo ="relation.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustApplyTo ="relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustApplyTo ="table")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all")
    public List<AddColumnConfig> getColumns() {
        return columns;
    }

    public void addColumn(AddColumnConfig column) {
        columns.add(column);
    }

    public void removeColumn(AddColumnConfig column) {
        columns.remove(column);
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = super.validate(database);
        if (columns.size() == 0) {
            validationErrors.addError("'columns' is required");
        }
        return validationErrors;
    }

    public SqlStatement[] generateStatements(Database database) {

        List<SqlStatement> sql = new ArrayList<SqlStatement>();

        for (ColumnConfig column : getColumns()) {
            Set<ColumnConstraint> constraints = new HashSet<ColumnConstraint>();
            if (column.getConstraints() != null) {
                if (column.getConstraints().isNullable() != null && !column.getConstraints().isNullable()) {
                    constraints.add(new NotNullConstraint());
                }
                if (column.getConstraints().isUnique() != null && column.getConstraints().isUnique()) {
                	constraints.add(new UniqueConstraint());
                }
                if (column.getConstraints().isPrimaryKey() != null && column.getConstraints().isPrimaryKey()) {
                    constraints.add(new PrimaryKeyConstraint(column.getConstraints().getPrimaryKeyName()));
                }

                if (column.getConstraints().getReferences() != null) {
                    constraints.add(new ForeignKeyConstraint(column.getConstraints().getForeignKeyName(), column.getConstraints().getReferences()));
                }

            }

            if (column.isAutoIncrement() != null && column.isAutoIncrement()) {
                constraints.add(new AutoIncrementConstraint(column.getName(), column.getStartWith(), column.getIncrementBy()));
            }

            AddColumnStatement addColumnStatement = new AddColumnStatement(getCatalogName(), getSchemaName(),
                    getTableName(),
                    column.getName(),
                    column.getType(),
                    column.getDefaultValueObject(),
                    constraints.toArray(new ColumnConstraint[constraints.size()]));

            sql.add(addColumnStatement);

            if (database instanceof DB2Database) {
                sql.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName()));
            }            

            if (column.getValueObject() != null) {
                UpdateStatement updateStatement = new UpdateStatement(getCatalogName(), getSchemaName(), getTableName());
                updateStatement.addNewColumnValue(column.getName(), column.getValueObject());
                sql.add(updateStatement);
            }
        }

      for (ColumnConfig column : getColumns()) {
          String columnRemarks = StringUtils.trimToNull(column.getRemarks());
          if (columnRemarks != null) {
              SetColumnRemarksStatement remarksStatement = new SetColumnRemarksStatement(catalogName, schemaName, tableName, column.getName(), columnRemarks);
              if (SqlGeneratorFactory.getInstance().supports(remarksStatement, database)) {
                  sql.add(remarksStatement);
              }
          }
      }

      return sql.toArray(new SqlStatement[sql.size()]);
    }

    @Override
    protected Change[] createInverses() {
        List<Change> inverses = new ArrayList<Change>();

        for (ColumnConfig aColumn : columns) {
            if (aColumn.hasDefaultValue()) {
                DropDefaultValueChange dropChange = new DropDefaultValueChange();
                dropChange.setTableName(getTableName());
                dropChange.setColumnName(aColumn.getName());

                inverses.add(dropChange);
            }


            DropColumnChange inverse = new DropColumnChange();
            inverse.setSchemaName(getSchemaName());
            inverse.setColumnName(aColumn.getName());
            inverse.setTableName(getTableName());
            inverses.add(inverse);
        }

        return inverses.toArray(new Change[inverses.size()]);
    }

    public String getConfirmationMessage() {
        List<String> names = new ArrayList<String>(columns.size());
        for (ColumnConfig col : columns) {
            names.add(col.getName() + "(" + col.getType() + ")");
        }

        return "Columns " + StringUtils.join(names, ",") + " added to " + tableName;
    }
}
