package liquibase.change.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.exception.ValidationErrors;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.SetColumnRemarksStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.StringUtils;

/**
 * Adds a column to an existing table.
 */
@ChangeClass(name="addColumn", description = "Add Column", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class AddColumnChange extends AbstractChange implements ChangeWithColumns<ColumnConfig> {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns;

    public AddColumnChange() {
        columns = new ArrayList<ColumnConfig>();
    }

    @ChangeProperty(mustApplyTo ="relation.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @ChangeProperty(mustApplyTo ="relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo ="table")
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

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
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

        for (ColumnConfig aColumn : getColumns()) {
            Set<ColumnConstraint> constraints = new HashSet<ColumnConstraint>();
            if (aColumn.getConstraints() != null) {
                if (aColumn.getConstraints().isNullable() != null && !aColumn.getConstraints().isNullable()) {
                    constraints.add(new NotNullConstraint());
                }
                if (aColumn.getConstraints().isUnique() != null && aColumn.getConstraints().isUnique()) {
                	constraints.add(new UniqueConstraint());
                }
                if (aColumn.getConstraints().isPrimaryKey() != null && aColumn.getConstraints().isPrimaryKey()) {
                    constraints.add(new PrimaryKeyConstraint(aColumn.getConstraints().getPrimaryKeyName()));
                }

                if (aColumn.getConstraints().getReferences() != null) {
                    constraints.add(new ForeignKeyConstraint(aColumn.getConstraints().getForeignKeyName(), aColumn.getConstraints().getReferences()));
                }

            }

            if (aColumn.isAutoIncrement() != null && aColumn.isAutoIncrement()) {
                constraints.add(new AutoIncrementConstraint(aColumn.getName(), aColumn.getStartWith(), aColumn.getIncrementBy()));
            }

            AddColumnStatement addColumnStatement = new AddColumnStatement(getCatalogName(), getSchemaName(),
                    getTableName(),
                    aColumn.getName(),
                    aColumn.getType(),
                    aColumn.getDefaultValueObject(),
                    constraints.toArray(new ColumnConstraint[constraints.size()]));

            sql.add(addColumnStatement);

            if (database instanceof DB2Database) {
                sql.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName()));
            }            

            if (aColumn.getValueObject() != null) {
                UpdateStatement updateStatement = new UpdateStatement(getCatalogName(), getSchemaName(), getTableName());
                updateStatement.addNewColumnValue(aColumn.getName(), aColumn.getValueObject());
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

//        for (ColumnConfig aColumn : columns) {
//            if (aColumn.getConstraints() != null) {
//                if (aColumn.getConstraints().isPrimaryKey() != null && aColumn.getConstraints().isPrimaryKey()) {
//                    AddPrimaryKeyChange change = new AddPrimaryKeyChange();
//                    change.setSchemaName(schemaName);
//                    change.setTableName(getTableName());
//                    change.setColumnNames(aColumn.getName());
//
//                    sql.addAll(Arrays.asList(change.generateStatements(database)));
//                }
//            }
//        }

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
