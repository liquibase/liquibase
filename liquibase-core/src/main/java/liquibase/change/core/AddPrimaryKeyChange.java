package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddPrimaryKeyStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;

/**
 * Creates a primary key out of an existing column or set of columns.
 */
@DatabaseChange(name="addPrimaryKey", description = "Adds creates a primary key out of an existing column or set of columns.", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class AddPrimaryKeyChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String tablespace;
    private String columnNames;
    private String constraintName;
    private Boolean clustered;

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table to create the primary key on")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog", since = "3.0")
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

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of the column(s) to create the primary key on. Comma separated if multiple")
    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    @DatabaseChangeProperty(description = "Name of primary key constraint", exampleValue = "pk_person")
    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }


    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public Boolean getClustered() {
        return clustered;
    }

    public void setClustered(Boolean clustered) {
        this.clustered = clustered;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {


        AddPrimaryKeyStatement statement = new AddPrimaryKeyStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnNames(), getConstraintName());
        statement.setTablespace(getTablespace());
        statement.setClustered(getClustered());

        if (database instanceof DB2Database) {
            return new SqlStatement[]{
                    statement,
                    new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName())
            };
//todo        } else if (database instanceof SQLiteDatabase) {
//            // return special statements for SQLite databases
//            return generateStatementsForSQLiteDatabase(database);
        }

        return new SqlStatement[]{
                statement
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            PrimaryKey example = new PrimaryKey(getConstraintName(), getCatalogName(), getSchemaName(), getTableName(), Column.arrayFromNames(getColumnNames()));

            PrimaryKey snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            result.assertComplete(snapshot != null, "Primary key does not exist");

            return result;

        } catch (Exception e) {
            return result.unknown(e);
        }
    }

    //    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
//        // SQLite does not support this ALTER TABLE operation until now.
//        // or more information: http://www.sqlite.org/omitted.html
//        // This is a small work around...
//
//        List<SqlStatement> statements = new ArrayList<SqlStatement>();
//
//        // define alter table logic
//        AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
//            public ColumnConfig[] getColumnsToAdd() {
//                return new ColumnConfig[0];
//            }
//
//            public boolean copyThisColumn(ColumnConfig column) {
//                return true;
//            }
//
//            public boolean createThisColumn(ColumnConfig column) {
//                String[] split_columns = getColumnNames().split("[ ]*,[ ]*");
//                for (String split_column : split_columns) {
//                    if (column.getName().equals(split_column)) {
//                        column.getConstraints().setPrimaryKey(true);
//                    }
//                }
//                return true;
//            }
//
//            public boolean createThisIndex(Index index) {
//                return true;
//            }
//        };
//
//        try {
//            // alter table
//            statements.addAll(SQLiteDatabase.getAlterTableStatements(
//                    rename_alter_visitor,
//                    database, getCatalogName(),  getSchemaName(), getTableName()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return statements.toArray(new SqlStatement[statements.size()]);
//    }

    @Override
    protected Change[] createInverses() {
        DropPrimaryKeyChange inverse = new DropPrimaryKeyChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setConstraintName(getConstraintName());

        return new Change[]{
                inverse,
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Primary key added to " + getTableName() + " (" + getColumnNames() + ")";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
