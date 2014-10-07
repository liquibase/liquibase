package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.change.AddColumnConfig;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.snapshot.InvalidExampleException;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.structure.core.*;
import liquibase.util.ISODateFormat;

import java.math.BigInteger;
import java.util.*;

public class SQLiteDatabase extends AbstractJdbcDatabase {

    private Set<String> systemTables = new HashSet<String>();

    {
        systemTables.add("sqlite_sequence");
    }

    public SQLiteDatabase() {
        super.setCurrentDateTimeFunction("CURRENT_TIMESTAMP");
    }

    public static final String PRODUCT_NAME = "SQLite";

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sqlite:")) {
            return "org.sqlite.JDBC";
        }
        return null;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }


    @Override
    public String getShortName() {
        return "sqlite";
    }


    @Override
    protected String getDefaultDatabaseProductName() {
        return PRODUCT_NAME;
    }

    @Override
    public Integer getDefaultPort() {
        return null;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn)
            throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
        return null;
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

    public String getTrigger(String table, String column) {
        return "CREATE TRIGGER insert_" + table + "_timeEnter AFTER  INSERT ON " + table + " BEGIN" +
                " UPDATE " + table + " SET " + column + " = DATETIME('NOW')" +
                " WHERE rowid = new.rowid END ";
    }

    @Override
    public String getAutoIncrementClause() {
        return "AUTOINCREMENT";
    }

    @Override
    protected boolean generateAutoIncrementStartWith(BigInteger startWith) {
        // not supported
        return false;
    }

    @Override
    protected boolean generateAutoIncrementBy(BigInteger incrementBy) {
        // not supported
        return false;
    }

    public static List<SqlStatement> getAlterTableStatements(
            AlterTableVisitor alterTableVisitor,
            Database database, String catalogName, String schemaName, String tableName)
            throws DatabaseException {

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        Table table = null;
        try {
            table = SnapshotGeneratorFactory.getInstance().createSnapshot((Table) new Table().setName(tableName).setSchema(new Schema(new Catalog(null), null)), database);

            List<ColumnConfig> createColumns = new ArrayList<ColumnConfig>();
            List<ColumnConfig> copyColumns = new ArrayList<ColumnConfig>();
            if (table != null) {
                for (Column column : table.getColumns()) {
                    ColumnConfig new_column = new ColumnConfig(column);
                    if (alterTableVisitor.createThisColumn(new_column)) {
                        createColumns.add(new_column);
                    }
                    ColumnConfig copy_column = new ColumnConfig(column);
                    if (alterTableVisitor.copyThisColumn(copy_column)) {
                        copyColumns.add(copy_column);
                    }

                }
            }
            for (ColumnConfig column : alterTableVisitor.getColumnsToAdd()) {
                if (alterTableVisitor.createThisColumn(column)) {
                    createColumns.add(column);
                }
                if (alterTableVisitor.copyThisColumn(column)) {
                    copyColumns.add(column);
                }
            }

            List<Index> newIndices = new ArrayList<Index>();
            for (Index index : SnapshotGeneratorFactory.getInstance().createSnapshot(new CatalogAndSchema(catalogName, schemaName), database, new SnapshotControl(database, Index.class)).get(Index.class)) {
                if (index.getTable().getName().equalsIgnoreCase(tableName)) {
                    if (alterTableVisitor.createThisIndex(index)) {
                        newIndices.add(index);
                    }
                }
            }

            // rename table
            String temp_table_name = tableName + "_temporary";

            statements.addAll(Arrays.asList(new RenameTableStatement(catalogName, schemaName, tableName, temp_table_name)));
            // create temporary table
            CreateTableChange ct_change_tmp = new CreateTableChange();
            ct_change_tmp.setSchemaName(schemaName);
            ct_change_tmp.setTableName(tableName);
            for (ColumnConfig column : createColumns) {
                ct_change_tmp.addColumn(column);
            }
            statements.addAll(Arrays.asList(ct_change_tmp.generateStatements(database)));
            // copy rows to temporary table
            statements.addAll(Arrays.asList(new CopyRowsStatement(temp_table_name, tableName, copyColumns)));
            // delete original table
            statements.addAll(Arrays.asList(new DropTableStatement(catalogName, schemaName, temp_table_name, false)));
            // validate indices
            statements.addAll(Arrays.asList(new ReindexStatement(catalogName, schemaName, tableName)));
            // add remaining indices
            for (Index index_config : newIndices) {
                AddColumnConfig[] columns = new AddColumnConfig[index_config.getColumns().size()];
                for (int i=0; i<index_config.getColumns().size(); i++) {
                    columns[i] = new AddColumnConfig(index_config.getColumns().get(i));
                }

                statements.addAll(Arrays.asList(new CreateIndexStatement(
                        index_config.getName(),
                        catalogName, schemaName, tableName,
                        index_config.isUnique(),
                        index_config.getAssociatedWithAsString(),
                        columns)));
            }

            return statements;
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    @Override
    protected Set<String> getSystemViews() {
        return systemTables;
    }

    @Override
    protected Set<String> getSystemTables() {
        return systemTables;
    }

    @Override
    public String getDateTimeLiteral(java.sql.Timestamp date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }


    public interface AlterTableVisitor {
        public ColumnConfig[] getColumnsToAdd();

        public boolean copyThisColumn(ColumnConfig column);

        public boolean createThisColumn(ColumnConfig column);

        public boolean createThisIndex(Index index);
    }

}
