package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BooleanType;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.util.BooleanUtil;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds a not-null constraint to an existing column.
 */
@DatabaseChange(name="addNotNullConstraint",
                description = "Adds a not-null constraint to an existing table. If a defaultNullValue attribute is " +
                "passed, all null values for the column will be updated to the passed value before the constraint " +
                "is applied.",
                priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class AddNotNullConstraintChange extends AbstractChange {

    class SQLiteAlterTableVisitor implements AlterTableVisitor {
        @Override
        public ColumnConfig[] getColumnsToAdd() {
            return new ColumnConfig[0];
        }

        @Override
        public boolean copyThisColumn(ColumnConfig column) {
            return true;
        }

        @Override
        public boolean createThisColumn(ColumnConfig column) {
            if (column.getName().equals(getColumnName())) {
                column.getConstraints().setNullable(false);
            }
            return true;
        }

        @Override
        public boolean createThisIndex(Index index) {
            return true;
        }

    }


    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String defaultNullValue;
    private String columnDataType;
    private String constraintName;
    private Boolean shouldValidate;

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting ="column.relation.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Adds a not-null constraint to an " +
     "existing table. If a defaultNullValue attribute is passed, all null values for the column will be updated to " +
     "the passed value before the constraint is applied.", mustEqualExisting = "column.relation")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(description = "Name of the column to add the constraint to",
        mustEqualExisting = "column.relation.column")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty(description = "Value to set all currently null values to. If not set, change will fail " +
     "if null values exist")
    public String getDefaultNullValue() {
        return defaultNullValue;
    }

    public void setDefaultNullValue(String defaultNullValue) {
        this.defaultNullValue = defaultNullValue;
    }

    @DatabaseChangeProperty(description = "Current data type of the column")
    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    @DatabaseChangeProperty(description = "Created constraint name (if database supports names for NOT NULL " +
     "constraints)")
    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<>();

        if (defaultNullValue != null && !defaultNullValue.equalsIgnoreCase("null")) {
            final String columnDataType = this.getColumnDataType();

            Object finalDefaultNullValue = defaultNullValue;
            if (columnDataType != null) {
                final LiquibaseDataType datatype = DataTypeFactory.getInstance().fromDescription(columnDataType, database);
                if (datatype instanceof BooleanType) {
                    //need to detect a boolean or bit type and handle it correctly sometimes or it is not converted to the correct datatype
                    finalDefaultNullValue = datatype.objectToSql(finalDefaultNullValue, database);
                    if (finalDefaultNullValue.equals("0")) {
                        finalDefaultNullValue = 0;
                    } else if (finalDefaultNullValue.equals("1")) {
                        finalDefaultNullValue = 1;
                    }

                    if (columnDataType.toLowerCase().contains("bit")) {
                        if (BooleanUtil.parseBoolean(finalDefaultNullValue.toString())) {
                            finalDefaultNullValue = 1;
                        } else {
                            finalDefaultNullValue = 0;
                        }
                    }

                    if (database instanceof PostgresDatabase) {
                        if (finalDefaultNullValue.equals(0)) {
                            finalDefaultNullValue = new DatabaseFunction( "B'0'");
                        } else if (finalDefaultNullValue.equals(1)) {
                            finalDefaultNullValue = new DatabaseFunction( "B'1'");
                        }
                    }
                }
            }

            statements.add(new UpdateStatement(getCatalogName(), getSchemaName(), getTableName())
                                   .addNewColumnValue(getColumnName(), finalDefaultNullValue)
                                   .setWhereClause(database.escapeObjectName(getColumnName(), Column.class) +
                                   " IS NULL"));
        }

        statements.add(new SetNullableStatement(getCatalogName(), getSchemaName(), getTableName(),
                getColumnName(),
            getColumnDataType(), false, getConstraintName(),shouldValidate));
        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName()));
        }
        
        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    protected Change[] createInverses() {
        DropNotNullConstraintChange inverse = new DropNotNullConstraintChange();
        inverse.setColumnName(getColumnName());
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setColumnDataType(getColumnDataType());

        return new Change[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "NOT NULL constraint " +
            (StringUtil.trimToNull(getConstraintName()) != null
                ? String.format("\"%s\" ", getConstraintName()) : "" ) +
            "has been added to " + getTableName() + "." + getColumnName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    /**
     * In certain SQL dialects, the VALIDATE keyword defines whether a NOT NULL constraint on a column in a table
     * should be checked if it refers to a valid row or not.
     * @return true if ENABLE VALIDATE (this is the default), or false if ENABLE NOVALIDATE.
     */
    @DatabaseChangeProperty(description = "This is true if the not null constraint has 'ENABLE VALIDATE' set, or false if the not null constrain has 'ENABLE NOVALIDATE' set.")
    public Boolean getValidate() {
        return shouldValidate;
    }

    /**
     *
     * @param shouldValidate - if shouldValidate is set to FALSE then the constraint will be created
     * with the 'ENABLE NOVALIDATE' mode. This means the constraint would be created, but that no
     * check will be done to ensure old data has valid not null constrain - only new data would be checked
     * to see if it complies with the constraint logic. The default state for not null constrain is to
     * have 'ENABLE VALIDATE' set.
     */
    public void setValidate(Boolean shouldValidate) {
        this.shouldValidate = shouldValidate;
    }
}

