package liquibase.change.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.*;
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
@DatabaseChange(name="addColumn", description = "Adds a new column to an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class AddColumnChange extends AbstractChange implements ChangeWithColumns<AddColumnConfig> {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private List<AddColumnConfig> columns;

    public AddColumnChange() {
        columns = new ArrayList<AddColumnConfig>();
    }
    
    @DatabaseChangeProperty(mustEqualExisting ="relation.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table", description = "Name of the table to add the column to")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    @DatabaseChangeProperty(description = "Column constraint and foreign key information. Setting the \"defaultValue\" attribute will specify a default value for the column. Setting the \"value\" attribute will set all rows existing to the specified value without modifying the column default.", requiredForDatabase = "all")
    public List<AddColumnConfig> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<AddColumnConfig> columns) {
        this.columns = columns;
    }

    @Override
    public void addColumn(AddColumnConfig column) {
        this.columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        this.columns.remove(column);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        List<SqlStatement> sql = new ArrayList<SqlStatement>();

        if (getColumns().size() == 0) {
            return new SqlStatement[] {
                    new AddColumnStatement(catalogName, schemaName, tableName, null, null, null)
            };
        }

        for (AddColumnConfig column : getColumns()) {
            Set<ColumnConstraint> constraints = new HashSet<ColumnConstraint>();
            ConstraintsConfig constraintsConfig =column.getConstraints();
            if (constraintsConfig != null) {
                if (constraintsConfig.isNullable() != null && !constraintsConfig.isNullable()) {
                    constraints.add(new NotNullConstraint());
                }
                if (constraintsConfig.isUnique() != null && constraintsConfig.isUnique()) {
                    constraints.add(new UniqueConstraint());
                }
                if (constraintsConfig.isPrimaryKey() != null && constraintsConfig.isPrimaryKey()) {
                    constraints.add(new PrimaryKeyConstraint(constraintsConfig.getPrimaryKeyName()));
                }

                if (constraintsConfig.getReferences() != null ||
                        (constraintsConfig.getReferencedColumnNames() != null && constraintsConfig.getReferencedTableName() != null)) {
                    constraints.add(new ForeignKeyConstraint(constraintsConfig.getForeignKeyName(), constraintsConfig.getReferences()
                            , constraintsConfig.getReferencedTableName(), constraintsConfig.getReferencedColumnNames()));
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
                    column.getRemarks(),
                    constraints.toArray(new ColumnConstraint[constraints.size()]));

            if ((database instanceof MySQLDatabase) && (column.getAfterColumn() != null)) {
                addColumnStatement.setAddAfterColumn(column.getAfterColumn());
            } else if (((database instanceof HsqlDatabase) || (database instanceof H2Database))
                       && (column.getBeforeColumn() != null)) {
                addColumnStatement.setAddBeforeColumn(column.getBeforeColumn());
            } else if ((database instanceof FirebirdDatabase) && (column.getPosition() != null)) {
                addColumnStatement.setAddAtPosition(column.getPosition());
            }

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
                dropChange.setSchemaName(getSchemaName());

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

    @Override
    public String getConfirmationMessage() {
        List<String> names = new ArrayList<String>(columns.size());
        for (ColumnConfig col : columns) {
            names.add(col.getName() + "(" + col.getType() + ")");
        }

        return "Columns " + StringUtils.join(names, ",") + " added to " + tableName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
