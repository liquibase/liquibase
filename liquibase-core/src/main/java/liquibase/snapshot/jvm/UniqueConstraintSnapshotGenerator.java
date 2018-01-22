package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.JdbcDatabaseSnapshot;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.sql.SQLException;
import java.util.*;

public class UniqueConstraintSnapshotGenerator extends JdbcSnapshotGenerator {

    public UniqueConstraintSnapshotGenerator() {
        super(UniqueConstraint.class, new Class[]{Table.class});
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SQLiteDatabase) {
            return PRIORITY_NONE;
        }
        return super.getPriority(objectType, database);
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        UniqueConstraint exampleConstraint = (UniqueConstraint) example;
        Relation table = exampleConstraint.getTable();

        List<Map<String, ?>> metadata = listColumns(exampleConstraint, database, snapshot);

        if (metadata.isEmpty()) {
            return null;
        }
        UniqueConstraint constraint = new UniqueConstraint();
        constraint.setTable(table);
        constraint.setName(example.getName());
        constraint.setBackingIndex(exampleConstraint.getBackingIndex());
        constraint.setInitiallyDeferred(((UniqueConstraint) example).isInitiallyDeferred());
        constraint.setDeferrable(((UniqueConstraint) example).isDeferrable());
        constraint.setClustered(((UniqueConstraint) example).isClustered());

        for (Map<String, ?> col : metadata) {
            String ascOrDesc = (String) col.get("ASC_OR_DESC");
            Boolean descending = "D".equals(ascOrDesc) ? Boolean.TRUE : ("A".equals(ascOrDesc) ? Boolean.FALSE : null);
            if (database instanceof H2Database) {
                for (String columnName : StringUtils.splitAndTrim((String) col.get("COLUMN_NAME"), ",")) {
                    constraint.getColumns().add(new Column(columnName).setDescending(descending).setRelation(table));
                }
            } else {
                constraint.getColumns().add(new Column((String) col.get("COLUMN_NAME")).setDescending(descending).setRelation(table));
            }
            setValidateOptionIfAvailable(database, constraint, col);
        }

        return constraint;
    }

    /**
     * Method to map 'validate' option for UC. This thing works only for ORACLE
     *
     * @param database - DB where UC will be created
     * @param uniqueConstraint - UC object to persist validate option
     * @param columnsMetadata - it's a cache-map to get metadata about UC
     */
    private void setValidateOptionIfAvailable(Database database, UniqueConstraint uniqueConstraint, Map<String, ?> columnsMetadata) {
        if (!(database instanceof OracleDatabase)) {
            return;
        }
        final Object constraintValidate = columnsMetadata.get("CONSTRAINT_VALIDATE");
        final String VALIDATE = "VALIDATED";
        if (constraintValidate!=null && !constraintValidate.toString().trim().isEmpty()) {
            uniqueConstraint.setShouldValidate(VALIDATE.equals(cleanNameFromDatabase(constraintValidate.toString().trim(), database)));
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {

        if (!snapshot.getSnapshotControl().shouldInclude(UniqueConstraint.class)) {
            return;
        }

        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            Database database = snapshot.getDatabase();
            Schema schema;
            schema = table.getSchema();

            List<CachedRow> metadata = null;
            try {
                metadata = listConstraints(table, snapshot, schema);
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }

            Set<String> seenConstraints = new HashSet<>();

            for (CachedRow constraint : metadata) {
                UniqueConstraint uq = new UniqueConstraint()
                        .setName(cleanNameFromDatabase((String) constraint.get("CONSTRAINT_NAME"), database)).setTable(table);
                if (constraint.containsColumn("INDEX_NAME")) {
                    uq.setBackingIndex(new Index((String) constraint.get("INDEX_NAME"), (String) constraint.get("INDEX_CATALOG"), (String) constraint.get("INDEX_SCHEMA"), table.getName()));
                }
                if ("CLUSTERED".equals(constraint.get("TYPE_DESC"))) {
                    uq.setClustered(true);
                }
                if (seenConstraints.add(uq.getName())) {
                    table.getUniqueConstraints().add(uq);
                }
            }
        }
    }

    protected List<CachedRow> listConstraints(Table table, DatabaseSnapshot snapshot, Schema schema) throws DatabaseException, SQLException {
        return ((JdbcDatabaseSnapshot) snapshot).getMetaDataFromCache().getUniqueConstraints(schema.getCatalogName(), schema.getName(), table.getName());
    }

    protected List<Map<String, ?>> listColumns(UniqueConstraint example, Database database, DatabaseSnapshot snapshot) throws DatabaseException {
        Relation table = example.getTable();
        Schema schema = table.getSchema();
        String name = example.getName();

        boolean bulkQuery;
        String sql;

        String cacheKey = "uniqueConstraints-"+example.getClass().getSimpleName()+"-"+example.getSchema().toCatalogAndSchema().customize(database).toString();
        String queryCountKey = "uniqueConstraints-"+example.getClass().getSimpleName()+"-queryCount";

        Map<String, List<Map<String, ?>>> columnCache = (Map<String, List<Map<String, ?>>>) snapshot.getScratchData(cacheKey);
        Integer columnQueryCount = (Integer) snapshot.getScratchData(queryCountKey);
        if (columnQueryCount == null) {
            columnQueryCount = 0;
        }

        if (columnCache == null) {
            bulkQuery = ((database instanceof OracleDatabase) || (database instanceof MSSQLDatabase)) &&
                (columnQueryCount > 3);
            snapshot.setScratchData(queryCountKey, columnQueryCount + 1);

            if ((database instanceof MySQLDatabase) || (database instanceof HsqlDatabase)) {
                sql = "select const.CONSTRAINT_NAME, COLUMN_NAME "
                        + "from " + database.getSystemSchema() + ".table_constraints const "
                        + "join " + database.getSystemSchema() + ".key_column_usage col "
                        + "on const.constraint_schema=col.constraint_schema "
                        + "and const.table_name=col.table_name "
                        + "and const.constraint_name=col.constraint_name "
                        + "where const.constraint_schema='" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' "
                        + "and const.table_name='" + database.correctObjectName(example.getTable().getName(), Table.class) + "' "
                        + "and const.constraint_name='" + database.correctObjectName(name, UniqueConstraint.class) + "'"
                        + "order by ordinal_position";
            } else if (database instanceof PostgresDatabase) {
                sql = "select const.CONSTRAINT_NAME, COLUMN_NAME "
                        + "from " + database.getSystemSchema() + ".table_constraints const "
                        + "join " + database.getSystemSchema() + ".key_column_usage col "
                        + "on const.constraint_schema=col.constraint_schema "
                        + "and const.table_name=col.table_name "
                        + "and const.constraint_name=col.constraint_name "
                        + "where const.constraint_catalog='" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' "
                        + "and const.constraint_schema='" + database.correctObjectName(schema.getSchema().getName(), Schema.class) + "' "
                        + "and const.table_name='" + database.correctObjectName(example.getTable().getName(), Table.class) + "' "
                        + "and const.constraint_name='" + database.correctObjectName(name, UniqueConstraint.class) + "'"
                        + "order by ordinal_position";
            } else if (database instanceof MSSQLDatabase) {
                sql =
                        "SELECT " +
                                "[kc].[name] AS [CONSTRAINT_NAME], " +
                                    "s.name AS constraint_container, "+
                                "[c].[name] AS [COLUMN_NAME], " +
                                "CASE [ic].[is_descending_key] WHEN 0 THEN N'A' WHEN 1 THEN N'D' END AS [ASC_OR_DESC] " +
                                "FROM [sys].[schemas] AS [s] " +
                                "INNER JOIN [sys].[tables] AS [t] " +
                                "ON [t].[schema_id] = [s].[schema_id] " +
                                "INNER JOIN [sys].[key_constraints] AS [kc] " +
                                "ON [kc].[parent_object_id] = [t].[object_id] " +
                                "INNER JOIN [sys].[indexes] AS [i] " +
                                "ON [i].[object_id] = [kc].[parent_object_id] " +
                                "AND [i].[index_id] = [kc].[unique_index_id] " +
                                "INNER JOIN [sys].[index_columns] AS [ic] " +
                                "ON [ic].[object_id] = [i].[object_id] " +
                                "AND [ic].[index_id] = [i].[index_id] " +
                                "INNER JOIN [sys].[columns] AS [c] " +
                                "ON [c].[object_id] = [ic].[object_id] " +
                                "AND [c].[column_id] = [ic].[column_id] " +
                                    "WHERE [s].[name] = N'" + database.escapeStringForDatabase(database.correctObjectName(schema.getName(), Schema.class)) + "' ";
                    if (!bulkQuery) {
                        sql += "AND [t].[name] = N'" + database.escapeStringForDatabase(database.correctObjectName(example.getTable().getName(), Table.class)) + "' " +
                                "AND [kc].[name] = N'" + database.escapeStringForDatabase(database.correctObjectName(name, UniqueConstraint.class)) + "' ";
                    }
                    sql += "ORDER BY " +
                            "[ic].[key_ordinal]";
            } else if (database instanceof OracleDatabase) {
                sql = "select ucc.owner as constraint_container, ucc.constraint_name as constraint_name, ucc.column_name, f.validated as constraint_validate " +
                        "from all_cons_columns ucc " +
                        "INNER JOIN all_constraints f " +
                        "ON ucc.owner = f.owner " +
                        "AND ucc.constraint_name = f.constraint_name " +
                        "where " +
                        (bulkQuery ? "" : "ucc.constraint_name='" + database.correctObjectName(name, UniqueConstraint.class) + "' and ") +
                        "ucc.owner='" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' " +
                        "and ucc.table_name not like 'BIN$%' "+
                        "order by ucc.position";
            } else if (database instanceof DB2Database) {
                if (database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")) {
                    sql = "select T1.constraint_name as CONSTRAINT_NAME, T2.COLUMN_NAME as COLUMN_NAME from QSYS2.TABLE_CONSTRAINTS T1, QSYS2.SYSCSTCOL T2\n"
                            + "where T1.CONSTRAINT_TYPE='UNIQUE' and T1.CONSTRAINT_NAME=T2.CONSTRAINT_NAME\n"
                            + "and T1.CONSTRAINT_SCHEMA='" + database.correctObjectName(schema.getName(), Schema.class) + "'\n"
                            + "and T2.CONSTRAINT_SCHEMA='" + database.correctObjectName(schema.getName(), Schema.class) + "'\n"
                            //+ "T2.TABLE_NAME='"+ database.correctObjectName(example.getTable().getName(), Table.class) + "'\n"
                            //+ "\n"
                            + "order by T2.COLUMN_NAME\n";

                } else {
                    sql = "select k.colname as column_name from syscat.keycoluse k, syscat.tabconst t "
                            + "where k.constname = t.constname "
                            + "and k.tabschema = t.tabschema "
                            + "and t.type='U' "
                            + "and k.constname='" + database.correctObjectName(name, UniqueConstraint.class) + "' "
                            + "and t.tabschema = '" + database.correctObjectName(schema.getName(), Schema.class) + "' "
                            + "order by colseq";
                }
            } else if (database instanceof DerbyDatabase) {
                sql = "SELECT cg.descriptor as descriptor, t.tablename "
                        + "FROM sys.sysconglomerates cg "
                        + "JOIN sys.syskeys k ON cg.conglomerateid = k.conglomerateid "
                        + "JOIN sys.sysconstraints c ON c.constraintid = k.constraintid "
                        + "JOIN sys.systables t ON c.tableid = t.tableid "
                        + "WHERE c.constraintname='" + database.correctObjectName(name, UniqueConstraint.class) + "'";
                List<Map<String, ?>> rows = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(sql));

                List<Map<String, ?>> returnList = new ArrayList<>();
                if (rows.isEmpty()) {
                    return returnList;
                } else if (rows.size() > 1) {
                    throw new UnexpectedLiquibaseException("Got multiple rows back querying unique constraints");
                } else {
                    Map rowData = rows.get(0);
                    String descriptor = rowData.get("DESCRIPTOR").toString();
                    descriptor = descriptor.replaceFirst(".*\\(", "").replaceFirst("\\).*", "");
                    for (String columnNumber : StringUtils.splitAndTrim(descriptor, ",")) {
                        String columnName = (String) ExecutorService.getInstance().getExecutor(database).queryForObject(new RawSqlStatement(
                                "select c.columnname from sys.syscolumns c "
                                        + "join sys.systables t on t.tableid=c.referenceid "
                                        + "where t.tablename='" + rowData.get("TABLENAME") + "' and c.columnnumber=" + columnNumber), String.class);

                        Map<String, String> row = new HashMap<>();
                        row.put("COLUMN_NAME", columnName);
                        returnList.add(row);
                    }
                    return returnList;
                }

            } else if (database instanceof FirebirdDatabase) {
                // Careful! FIELD_NAME and INDEX_NAME in RDB$INDEX_SEGMENTS are CHAR, not VARCHAR columns.
                sql = "SELECT TRIM(RDB$INDEX_SEGMENTS.RDB$FIELD_NAME) AS column_name " +
                        "FROM RDB$INDEX_SEGMENTS " +
                        "LEFT JOIN RDB$INDICES ON RDB$INDICES.RDB$INDEX_NAME = RDB$INDEX_SEGMENTS.RDB$INDEX_NAME " +
                        "WHERE UPPER(TRIM(RDB$INDICES.RDB$INDEX_NAME))='" + database.correctObjectName(name, UniqueConstraint.class) + "' " +
                        "ORDER BY RDB$INDEX_SEGMENTS.RDB$FIELD_POSITION";
            } else if (database instanceof SybaseASADatabase) {
                sql = "select sysconstraint.constraint_name, syscolumn.column_name " +
                        "from sysconstraint, syscolumn, systable " +
                        "where sysconstraint.ref_object_id = syscolumn.object_id " +
                        "and sysconstraint.table_object_id = systable.object_id " +
                        "and sysconstraint.constraint_name = '" + database.correctObjectName(name, UniqueConstraint.class) + "' " +
                        "and systable.table_name = '" + database.correctObjectName(example.getTable().getName(), Table.class) + "'";
            } else if (database instanceof InformixDatabase) {
                sql = getUniqueConstraintsSqlInformix((InformixDatabase)database, schema, name);
            } else {
                // If we do not have a specific handler for the RDBMS, we assume that the database has an
                // INFORMATION_SCHEMA we can use. This is a last-resort measure and might fail.
                String catalogName = database.correctObjectName(schema.getCatalogName(), Catalog.class);
                String schemaName = database.correctObjectName(schema.getName(), Schema.class);
                String constraintName = database.correctObjectName(name, UniqueConstraint.class);
                String tableName = database.correctObjectName(table.getName(), Table.class);
                sql = "select CONSTRAINT_NAME, COLUMN_LIST as COLUMN_NAME "
                        + "from " + database.getSystemSchema() + ".constraints "
                        + "where constraint_type='UNIQUE' ";
                if (catalogName != null) {
                    sql += "and constraint_catalog='" + catalogName + "' ";
                }
                if (schemaName != null) {
                    sql += "and constraint_schema='" + schemaName + "' ";
                }
                if (tableName != null) {
                    sql += "and table_name='" + tableName + "' ";
                }
                if (constraintName != null) {
                    sql += "and constraint_name='" + constraintName + "'";
                }
            }
            List<Map<String, ?>> rows = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(sql));

            if (bulkQuery) {
                columnCache = new HashMap<>();
                snapshot.setScratchData(cacheKey, columnCache);
                for (Map<String, ?> row : rows) {
                    String key = row.get("CONSTRAINT_CONTAINER") + "_" + row.get("CONSTRAINT_NAME");
                    List<Map<String, ?>> constraintRows = columnCache.get(key);
                    if (constraintRows == null) {
                        constraintRows = new ArrayList<>();
                        columnCache.put(key, constraintRows);
                    }
                    constraintRows.add(row);
                }

                return listColumns(example, database, snapshot);
            } else {
                return rows;
            }
        } else {
            String lookupKey = schema.getName() + "_" + example.getName();
            List<Map<String, ?>> rows = columnCache.get(lookupKey);
            if (rows == null) {
                rows = new ArrayList<>();
            }
            return rows;
        }


    }

    /**
     * Gets an SQL query that returns the constraint names and columns for all UNIQUE constraints.
     * @param database A database object of the InformixDatabase type
     * @param schema Name of the schema to examine (or null for all)
     * @param name Name of the constraint to examine (or null for all)
     * @return A lengthy SQL statement that fetches the constraint names and columns
     */
    private String getUniqueConstraintsSqlInformix(InformixDatabase database, Schema schema, String name) {
        StringBuffer sqlBuf = new StringBuffer();

        sqlBuf.append("SELECT * FROM (\n");

        // Yes, I am serious about this. It appears there are neither CTE/WITH clauses nor PIVOT/UNPIVOT operators
        // in Informix SQL.
        for (int i=1; i<=16; i++) {
            if (i>1)
                sqlBuf.append("UNION ALL\n");
            sqlBuf.append(
                String.format("  SELECT\n" +
                        "    CONS.owner,\n" +
                        "    CONS.constrname AS constraint_name,\n" +
                        "    COL.colname AS column_name,\n" +
                        "    CONS.constrtype,\n" +
                        "    %d               AS column_index\n" +
                        "  FROM informix.sysconstraints CONS\n" +
                        "    JOIN informix.sysindexes IDX ON CONS.idxname = IDX.idxname\n" +
                        "    JOIN informix.syscolumns COL ON COL.tabid = CONS.tabid AND COL.colno = IDX.part%d\n",
                        i, i
                )
            );
        }

        // Finish the subquery and filter on the U(NIQUE) constraint type
        sqlBuf.append(
                "                ) SUBQ\n" +
                "WHERE constrtype='U' \n");

        String catalogName = database.correctObjectName(schema.getCatalogName(), Catalog.class);
        String constraintName = database.correctObjectName(name, UniqueConstraint.class);

        // If possible, filter for catalog name and/or constraint name
        if (catalogName != null) {
            sqlBuf.append("AND owner='" + catalogName + "'\n");
        }
        if (constraintName != null) {
            sqlBuf.append("AND constraint_name='" + constraintName + "'");
        }

        // For correct processing, it is important that we get all columns in order.
        sqlBuf.append("ORDER BY owner, constraint_name, column_index");

        // Return the query
        return sqlBuf.toString();
    }

}
