package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.change.AddColumnConfig;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.ISODateFormat;
import liquibase.statement.core.RawParameterizedSqlStatement;

import java.math.BigInteger;
import java.util.*;

public class SQLiteDatabase extends AbstractJdbcDatabase {

    public static final String PRODUCT_NAME = "SQLite";
    private final Set<String> systemTables = new HashSet<>();

    {
        systemTables.add("sqlite_sequence");
        systemTables.add("sqlite_master");
    }

    public SQLiteDatabase() {
        super.setCurrentDateTimeFunction("CURRENT_TIMESTAMP");
    }

    public static List<SqlStatement> getAlterTableStatements(
            AlterTableVisitor alterTableVisitor,
            Database database, String catalogName, String schemaName, String tableName)
            throws DatabaseException {

        List<SqlStatement> statements = new ArrayList<>();

        Table table = null;
        try {
            table = SnapshotGeneratorFactory.getInstance().createSnapshot((Table) new Table().setName(tableName).setSchema(new Schema(new Catalog(null), null)), database);

            List<ColumnConfig> createColumns = new ArrayList<>();
            List<ColumnConfig> copyColumns = new ArrayList<>();
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

            List<Index> newIndices = new ArrayList<>();
            for (Index index : SnapshotGeneratorFactory.getInstance().createSnapshot(
                    new CatalogAndSchema(catalogName, schemaName), database,
                    new SnapshotControl(database, Index.class)).get(Index.class)) {
                if (index.getRelation().getName().equalsIgnoreCase(tableName)) {
                    if (alterTableVisitor.createThisIndex(index)) {
                        newIndices.add(index);
                    }
                }
            }

            // rename table
            String temp_table_name = tableName + "_temporary";

            statements.addAll(Collections.singletonList(new RenameTableStatement(catalogName, schemaName, tableName, temp_table_name)));
            // create temporary table
            CreateTableChange ct_change_tmp = new CreateTableChange();
            ct_change_tmp.setSchemaName(schemaName);
            ct_change_tmp.setTableName(tableName);
            for (ColumnConfig column : createColumns) {
                ct_change_tmp.addColumn(column);
            }
            statements.addAll(Arrays.asList(ct_change_tmp.generateStatements(database)));
            // copy rows to temporary table
            statements.addAll(Collections.singletonList(new CopyRowsStatement(temp_table_name, tableName, copyColumns)));
            // delete original table
            statements.addAll(Collections.singletonList(new DropTableStatement(catalogName, schemaName, temp_table_name, false)));
            // validate indices
            statements.addAll(Collections.singletonList(new ReindexStatement(catalogName, schemaName, tableName)));
            // add remaining indices
            for (Index index_config : newIndices) {
                AddColumnConfig[] columns = new AddColumnConfig[index_config.getColumns().size()];
                for (int i=0; i<index_config.getColumns().size(); i++) {
                    columns[i] = new AddColumnConfig(index_config.getColumns().get(i));
                }

                // We must not create indexes that are auto-generated by SQLite
                if (!index_config.getName().toLowerCase().startsWith("sqlite_autoindex_")) {
                    statements.addAll(Collections.singletonList(new CreateIndexStatement(
                            index_config.getName(),
                            catalogName, schemaName, tableName,
                            index_config.isUnique(),
                            index_config.getAssociatedWithAsString(),
                            columns)));
                }
            }

            return statements;
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    /**
     * Uses the {@link SqlGeneratorFactory} to generate SQL statements for altering the specified table in the given database.
     *
     * @param database          the database to generate SQL for
     * @param alterTableVisitor the visitor to use for generating the ALTER TABLE statements
     * @param catalogName       the name of the catalog containing the table (may be {@code null})
     * @param schemaName        the name of the schema containing the table (may be {@code null})
     * @param tableName         the name of the table to alter
     * @return an array of {@link Sql} objects containing the generated SQL statements
     * @throws UnexpectedLiquibaseException if an error occurs during SQL generation
     */
    public static Sql[] getAlterTableSqls(Database database, SQLiteDatabase.AlterTableVisitor alterTableVisitor,
                                          String catalogName, String schemaName, String tableName) {
        Sql[] generatedSqls;
        try {
            List<SqlStatement> statements = SQLiteDatabase.getAlterTableStatements(alterTableVisitor, database,
                    catalogName, schemaName, tableName);
            // convert from SqlStatement to Sql
            SqlStatement[] sqlStatements = new SqlStatement[statements.size()];
            sqlStatements = statements.toArray(sqlStatements);
            generatedSqls = SqlGeneratorFactory.getInstance().generateSql(sqlStatements, database);
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return generatedSqls;
    }

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
        return true;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
        String definition = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForObject(
                new RawParameterizedSqlStatement("SELECT sql FROM sqlite_master WHERE name=?", viewName),
                String.class);
        // SQLite is friendly and already returns the form CREATE VIEW ... AS. However, we cannot use this, so we have
        // to cut off that header.
        definition = definition.replaceFirst("^(?i)CREATE [\\w\\s]*VIEW [^\\s]+ AS\\s*", "");
        return definition;
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }



    @Override
    public boolean supports(Class<? extends DatabaseObject> object) {
        if (Schema.class.isAssignableFrom(object)) {
            return false;
        }
        if (Sequence.class.isAssignableFrom(object)) {
            return false;
        }
        return super.supports(object);
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

    @Override
    public boolean supportsDropTableCascadeConstraints() {
        return false;
    }

    @Override
    public boolean supportsCreateIfNotExists(Class<? extends DatabaseObject> type) {
        return type.isAssignableFrom(Table.class);
    }

    public interface AlterTableVisitor {
        ColumnConfig[] getColumnsToAdd();

        boolean copyThisColumn(ColumnConfig column);

        boolean createThisColumn(ColumnConfig column);

        boolean createThisIndex(Index index);
    }

    @Override
    public boolean supportsDatabaseChangeLogHistory() {
        return true;
    }
}
