package liquibase.change.core;

import liquibase.database.core.DB2Database;
import liquibase.database.Database;
import liquibase.statement.*;
import liquibase.util.StringUtils;
import liquibase.change.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adds a column to an existing table.
 */
public class AddColumnChange extends AbstractChange implements ChangeWithColumns {

    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns;

    public AddColumnChange() {
        super("addColumn", "Add Column", ChangeMetaData.PRIORITY_DEFAULT);
        columns = new ArrayList<ColumnConfig>();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public ColumnConfig getLastColumn() {
        return (columns.size() > 0) ? columns.get(columns.size() - 1) : null;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        columns.remove(column);
    }

    public SqlStatement[] generateStatements(Database database) {

        List<SqlStatement> sql = new ArrayList<SqlStatement>();

        for (ColumnConfig aColumn : getColumns()) {
            Set<ColumnConstraint> constraints = new HashSet<ColumnConstraint>();
            if (aColumn.getConstraints() != null) {
                if (aColumn.getConstraints().isNullable() != null && !aColumn.getConstraints().isNullable()) {
                    constraints.add(new NotNullConstraint());
                }
                if (aColumn.getConstraints().isPrimaryKey() != null && aColumn.getConstraints().isPrimaryKey()) {
                    constraints.add(new PrimaryKeyConstraint(aColumn.getConstraints().getPrimaryKeyName()));
                }
            }
            if (aColumn.isAutoIncrement() != null && aColumn.isAutoIncrement()) {
                constraints.add(new AutoIncrementConstraint(aColumn.getName()));
            }

            AddColumnStatement addColumnStatement = new AddColumnStatement(getSchemaName(),
                    getTableName(),
                    aColumn.getName(),
                    aColumn.getType(),
                    aColumn.getDefaultValueObject(),
                    constraints.toArray(new ColumnConstraint[constraints.size()]));

            sql.add(addColumnStatement);

            if (database instanceof DB2Database) {
                sql.add(new ReorganizeTableStatement(getSchemaName(), getTableName()));
            }            

            if (aColumn.getValueObject() != null) {
                UpdateStatement updateStatement = new UpdateStatement(getSchemaName(), getTableName());
                updateStatement.addNewColumnValue(aColumn.getName(), aColumn.getValueObject());
                sql.add(updateStatement);
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
