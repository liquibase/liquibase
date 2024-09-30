package liquibase.snapshot.jvm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.JdbcDatabaseSnapshot;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtil;

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
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        Database database = snapshot.getDatabase();
        UniqueConstraint exampleConstraint = (UniqueConstraint) example;
        Relation table = exampleConstraint.getRelation();

        List<Map<String, ?>> metadata = listColumns(exampleConstraint, database, snapshot);

        if (metadata.isEmpty()) {
            return null;
        }
        UniqueConstraint constraint = new UniqueConstraint();
        constraint.setRelation(table);
        constraint.setName(example.getName());
        constraint.setBackingIndex(exampleConstraint.getBackingIndex());
        constraint.setInitiallyDeferred(((UniqueConstraint) example).isInitiallyDeferred());
        constraint.setDeferrable(((UniqueConstraint) example).isDeferrable());
        constraint.setClustered(((UniqueConstraint) example).isClustered());

        for (Map<String, ?> col : metadata) {
            String ascOrDesc = (String) col.get("ASC_OR_DESC");
            Boolean descending = "D".equals(ascOrDesc) ? Boolean.TRUE : ("A".equals(ascOrDesc) ? Boolean.FALSE : null);
            if (database instanceof H2Database) {
                for (String columnName : StringUtil.splitAndTrim((String) col.get("COLUMN_NAME"), ",")) {
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
     * @param database         - DB where UC will be created
     * @param uniqueConstraint - UC object to persist validate option
     * @param columnsMetadata  - it's a cache-map to get metadata about UC
     */
    private void setValidateOptionIfAvailable(Database database, UniqueConstraint uniqueConstraint, Map<String, ?> columnsMetadata) {
        if (!(database instanceof OracleDatabase)) {
            return;
        }
        final Object constraintValidate = columnsMetadata.get("CONSTRAINT_VALIDATE");
        final String VALIDATE = "VALIDATED";
        if (constraintValidate != null && !constraintValidate.toString().trim().isEmpty()) {
            uniqueConstraint.setShouldValidate(VALIDATE.equals(cleanNameFromDatabase(constraintValidate.toString().trim(), database)));
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException {

        if (!snapshot.getSnapshotControl().shouldInclude(UniqueConstraint.class) || !snapshot.getDatabase().supports(UniqueConstraint.class)) {
            return;
        }

        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            Database database = snapshot.getDatabase();
            Schema schema;
            schema = table.getSchema();

            List<CachedRow> metadata;
            try {
                metadata = listConstraints(table, snapshot, schema);
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }

            Set<String> seenConstraints = new HashSet<>();

            for (CachedRow constraint : metadata) {
                UniqueConstraint uq = new UniqueConstraint()
                        .setName(cleanNameFromDatabase((String) constraint.get("CONSTRAINT_NAME"), database)).setRelation(table);
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
        Relation table = example.getRelation();
        Schema schema = example.getSchema();
        String name = example.getName();

        boolean bulkQuery;
        String rawSql;
        List<String> parameters = new ArrayList<>();

        String cacheKey = "uniqueConstraints-" + example.getClass().getSimpleName() + "-" + example.getSchema().toCatalogAndSchema().customize(database).toString();
        String queryCountKey = "uniqueConstraints-" + example.getClass().getSimpleName() + "-queryCount";

        Map<String, List<Map<String, ?>>> columnCache = (ConcurrentHashMap<String, List<Map<String, ?>>>) snapshot.getScratchData(cacheKey);
        Integer columnQueryCount = (Integer) snapshot.getScratchData(queryCountKey);
        if (columnQueryCount == null) {
            columnQueryCount = 0;
        }

        if (columnCache == null) {
            bulkQuery = false;
            if (columnQueryCount > 3) {
                bulkQuery = supportsBulkQuery(database);
            }

            snapshot.setScratchData(queryCountKey, columnQueryCount + 1);

            if ((database instanceof MySQLDatabase) || (database instanceof HsqlDatabase)) {
                StringBuilder sql = new StringBuilder("select const.CONSTRAINT_NAME, const.TABLE_NAME, COLUMN_NAME, const.constraint_schema as CONSTRAINT_CONTAINER ")
                        .append(String.format("from %s.table_constraints const ", database.getSystemSchema()))
                        .append(String.format("join %s.key_column_usage col ", database.getSystemSchema()))
                        .append("on const.constraint_schema=col.constraint_schema ")
                        .append("and const.table_name=col.table_name ")
                        .append("and const.constraint_name=col.constraint_name ")
                        .append(String.format("where const.constraint_schema='%s' ", database.correctObjectName(schema.getCatalogName(), Catalog.class)));
                if (!bulkQuery) {
                    sql.append(String.format("and const.table_name='%s' ", database.correctObjectName(example.getRelation().getName(), Table.class)))
                            .append(String.format("and const.constraint_name='%s' ", database.correctObjectName(name, UniqueConstraint.class)));
                }
                sql.append("order by ordinal_position");
                rawSql = sql.toString();
            } else if (database instanceof PostgresDatabase) {
                List<String> conditions = new ArrayList<>();
                StringBuilder sql = new StringBuilder("select const.CONSTRAINT_NAME, COLUMN_NAME, const.constraint_schema as CONSTRAINT_CONTAINER ")
                        .append(String.format("from %s.table_constraints const ", database.getSystemSchema()))
                        .append(String.format("join %s.key_column_usage col ", database.getSystemSchema()))
                        .append("on const.constraint_schema=col.constraint_schema ")
                        .append("and const.table_name=col.table_name ")
                        .append("and const.constraint_name=col.constraint_name ");
                if (schema.getCatalogName() != null) {
                    conditions.add("const.constraint_catalog=?");
                    parameters.add(database.correctObjectName(schema.getCatalogName(), Catalog.class));
                }
                if (database instanceof CockroachDatabase) {
                    conditions.add("(select count(*) from (select indexdef from pg_indexes where schemaname='\" + database.correctObjectName(schema.getSchema().getName(), Schema.class) + \"' AND indexname='\" + database.correctObjectName(name, UniqueConstraint.class) + \"' AND (position('DESC,' in indexdef) > 0 OR position('DESC)' in indexdef) > 0))) = 0");
                    conditions.add("const.constraint_name != 'primary'");
                }
                if (schema.getSchema().getName() != null) {
                    conditions.add("const.constraint_schema=?");
                    parameters.add(database.correctObjectName(schema.getSchema().getName(), Schema.class));
                }
                if (!bulkQuery) {
                    conditions.add("const.table_name=?");
                    parameters.add(database.correctObjectName(example.getRelation().getName(), Table.class));
                    if (name != null) {
                        conditions.add("const.constraint_name=? ");
                        parameters.add(database.correctObjectName(name, UniqueConstraint.class));
                    }
                }

                if (!conditions.isEmpty()) {
                    sql.append(" WHERE ");
                    sql.append(String.join(" AND ", conditions));
                }

                sql.append(" order by ordinal_position");
                rawSql = sql.toString();
            } else if (database.getClass().getName().contains("MaxDB")) { //have to check classname as this is currently an extension
                String sql = "select CONSTRAINTNAME as constraint_name, COLUMNNAME as column_name from CONSTRAINTCOLUMNS WHERE CONSTRAINTTYPE = 'UNIQUE_CONST' AND tablename = ? AND constraintname = ?";
                parameters.add(database.correctObjectName(example.getRelation().getName(), Table.class));
                parameters.add(database.correctObjectName(name, UniqueConstraint.class));
                rawSql = sql;
            } else if (database instanceof MSSQLDatabase) {
                StringBuilder sql = new StringBuilder("SELECT [kc].[name] AS [CONSTRAINT_NAME], s.name AS constraint_container, [c].[name] AS [COLUMN_NAME], ")
                        .append("CASE [ic].[is_descending_key] WHEN 0 THEN N'A' WHEN 1 THEN N'D' END AS [ASC_OR_DESC] FROM [sys].[schemas] AS [s] ")
                        .append("INNER JOIN [sys].[tables] AS [t] ON [t].[schema_id] = [s].[schema_id] INNER JOIN [sys].[key_constraints] AS [kc] ")
                        .append("ON [kc].[parent_object_id] = [t].[object_id] INNER JOIN [sys].[indexes] AS [i] ON [i].[object_id] = [kc].[parent_object_id] ")
                        .append("AND [i].[index_id] = [kc].[unique_index_id] INNER JOIN [sys].[index_columns] AS [ic] ON [ic].[object_id] = [i].[object_id] ")
                        .append("AND [ic].[index_id] = [i].[index_id] INNER JOIN [sys].[columns] AS [c] ON [c].[object_id] = [ic].[object_id] AND [c].[column_id] = [ic].[column_id] ")
                        .append(String.format("WHERE [s].[name] = N'%s' ", database.escapeStringForDatabase(database.correctObjectName(schema.getName(), Schema.class))));
                    if (!bulkQuery) {
                        sql.append(String.format("AND [t].[name] = N'%s' AND [kc].[name] = N'%s' ", database.escapeStringForDatabase(database.correctObjectName(example.getRelation().getName(), Table.class)),
                                database.escapeStringForDatabase(database.correctObjectName(name, UniqueConstraint.class))));
                    }
                    sql.append("ORDER BY [ic].[key_ordinal]");
                rawSql = sql.toString();
            } else if (database instanceof OracleDatabase) {
                StringBuilder sql = new StringBuilder("select ucc.owner as constraint_container, ucc.constraint_name as constraint_name, ucc.column_name, f.validated as constraint_validate, ucc.table_name ")
                        .append("from all_cons_columns ucc INNER JOIN all_constraints f ON ucc.owner = f.owner AND ucc.constraint_name = f.constraint_name where ");
                        if(!bulkQuery) {
                            sql.append("ucc.constraint_name=? and ");
                            parameters.add(database.correctObjectName(name, UniqueConstraint.class));
                        }
                        sql.append("ucc.owner=? and ucc.table_name not like 'BIN$%' order by ucc.position");
                parameters.add(database.correctObjectName(schema.getCatalogName(), Catalog.class));
                rawSql = sql.toString();
            } else if (database instanceof DB2Database) {
                if (database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")) {
                    StringBuilder sql = new StringBuilder("select T1.constraint_name as CONSTRAINT_NAME, T2.COLUMN_NAME as COLUMN_NAME, T1.CONSTRAINT_SCHEMA as CONSTRAINT_CONTAINER from QSYS2.TABLE_CONSTRAINTS T1, QSYS2.SYSCSTCOL T2\n")
                            .append("where T1.CONSTRAINT_TYPE='UNIQUE' and T1.CONSTRAINT_NAME=T2.CONSTRAINT_NAME\n")
                            .append("and T1.CONSTRAINT_SCHEMA=?\n")
                            .append("and T2.CONSTRAINT_SCHEMA=?\n")
                            //+ "T2.TABLE_NAME='"+ database.correctObjectName(example.getTable().getName(), Table.class) + "'\n"
                            //+ "\n"
                            .append("order by T2.COLUMN_NAME\n");
                    parameters.add(database.correctObjectName(schema.getName(), Schema.class));
                    parameters.add(database.correctObjectName(schema.getName(), Schema.class));
                    rawSql = sql.toString();
                } else {
                    StringBuilder sql = new StringBuilder("select k.constname as constraint_name, k.colname as column_name from syscat.keycoluse k, syscat.tabconst t ")
                            .append("where k.constname = t.constname and k.tabschema = t.tabschema and t.type = 'U' ");
                            if(!bulkQuery) {
                                sql.append("and k.constname = ? ");
                                parameters.add(database.correctObjectName(name, UniqueConstraint.class));
                            }
                            sql.append("and t.tabschema = ? order by colseq");
                    parameters.add(database.correctObjectName(schema.getName(), Schema.class));
                    rawSql = sql.toString();
                }
            } else if (database instanceof Db2zDatabase) {
                StringBuilder sql = new StringBuilder("select  KC.colname as column_name ")
                        .append(" from SYSIBM.SYSKEYCOLUSE KC inner join SYSIBM.SYSTABCONST TC on KC.CONSTNAME = TC.CONSTNAME")
                        .append(" and KC.TBCREATOR = TC.TBCREATOR and KC.TBNAME = TC.TBNAME where TC.TYPE = 'U'");
                        if(!bulkQuery) {
                            sql.append(" and TC.CONSTNAME = ?");
                            parameters.add(database.correctObjectName(name, UniqueConstraint.class));
                        }
                        sql.append(" and TC.TBCREATOR = ? order by KC.COLSEQ");
                parameters.add(database.correctObjectName(schema.getName(), Schema.class));
                rawSql = sql.toString();
            } else if (database instanceof DerbyDatabase) {
                //does not support bulkQuery,  supportsBulkQuery should return false()
                StringBuilder sql = new StringBuilder("SELECT cg.descriptor as descriptor, t.tablename ")
                        .append("FROM sys.sysconglomerates cg JOIN sys.syskeys k ON cg.conglomerateid = k.conglomerateid ")
                        .append("JOIN sys.sysconstraints c ON c.constraintid = k.constraintid JOIN sys.systables t ON c.tableid = t.tableid ")
                        .append(String.format("WHERE c.constraintname='%s'", database.correctObjectName(name, UniqueConstraint.class)));
                List<Map<String, ?>> rows = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForList(new RawParameterizedSqlStatement(sql.toString()));

                List<Map<String, ?>> returnList = new ArrayList<>();
                if (rows.isEmpty()) {
                    return returnList;
                } else if (rows.size() > 1) {
                    throw new UnexpectedLiquibaseException("Got multiple rows back querying unique constraints");
                } else {
                    Map rowData = rows.get(0);
                    String descriptor = rowData.get("DESCRIPTOR").toString();
                    descriptor = descriptor.replaceFirst(".*\\(", "").replaceFirst("\\).*", "");
                    for (String columnNumber : StringUtil.splitAndTrim(descriptor, ",")) {
                        parameters = new ArrayList<>();
                        parameters.add((String) rowData.get("TABLENAME"));
                        parameters.add(columnNumber);
                        String columnName = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForObject(new RawParameterizedSqlStatement(
                                "select c.columnname from sys.syscolumns c join sys.systables t on t.tableid=c.referenceid where t.tablename=? and c.columnnumber=?", parameters.toArray()), String.class);

                        Map<String, String> row = new HashMap<>();
                        row.put("COLUMN_NAME", columnName);
                        returnList.add(row);
                    }
                    return returnList;
                }

            } else if (database instanceof FirebirdDatabase) {
                //does not support bulkQuery,  supportsBulkQuery should return false()

                // Careful! FIELD_NAME and INDEX_NAME in RDB$INDEX_SEGMENTS are CHAR, not VARCHAR columns.
                StringBuilder sql = new StringBuilder("SELECT TRIM(RDB$INDEX_SEGMENTS.RDB$FIELD_NAME) AS column_name ")
                        .append("FROM RDB$INDEX_SEGMENTS LEFT JOIN RDB$INDICES ON RDB$INDICES.RDB$INDEX_NAME = RDB$INDEX_SEGMENTS.RDB$INDEX_NAME ")
                        .append("WHERE UPPER(TRIM(RDB$INDICES.RDB$INDEX_NAME))=? ORDER BY RDB$INDEX_SEGMENTS.RDB$FIELD_POSITION");
                parameters.add(database.correctObjectName(name, UniqueConstraint.class));
                rawSql = sql.toString();
            } else if (database instanceof SybaseDatabase) {
                //does not support bulkQuery,  supportsBulkQuery should return false()
                StringBuilder sql = new StringBuilder("select soc.name as constraint_name")
                        .append(", (select scl.name from dbo.syscolumns scl where scl.id = sc.tableid and scl.colid = sc.colid) as column_name ")
                        .append("from dbo.sysconstraints sc inner join dbo.sysobjects soc on soc.id = sc.constrid inner join dbo.sysobjects sot on sot.id = sc.tableid ")
                        .append("where sot.id = OBJECT_ID(?) and soc.id = OBJECT_ID(?)");
                parameters.add(database.correctObjectName(example.getRelation().getName(), Table.class));
                parameters.add(database.correctObjectName(name, UniqueConstraint.class));
                rawSql = sql.toString();
            } else if (database instanceof SybaseASADatabase) {
                //does not support bulkQuery,  supportsBulkQuery should return false()
                StringBuilder sql = new StringBuilder("select sysconstraint.constraint_name, syscolumn.column_name ")
                        .append("from sysconstraint, syscolumn, systable where sysconstraint.ref_object_id = syscolumn.object_id ")
                        .append("and sysconstraint.table_object_id = systable.object_id and sysconstraint.constraint_name = ? and systable.table_name = ?");
                parameters.add(database.correctObjectName(name, UniqueConstraint.class));
                parameters.add(database.correctObjectName(example.getRelation().getName(), Table.class));
                rawSql = sql.toString();
            } else if(database instanceof Ingres9Database) {
                //does not support bulkQuery,  supportsBulkQuery should return false()
                rawSql = "select constraint_name, column_name from iikeys where constraint_name = ? and table_name = ?";
                parameters.add(database.correctObjectName(name, UniqueConstraint.class));
                parameters.add(database.correctObjectName(example.getRelation().getName(), Table.class));
            } else if (database instanceof InformixDatabase) {
                //does not support bulkQuery,  supportsBulkQuery should return false()

                rawSql = getUniqueConstraintsSqlInformix((InformixDatabase) database, schema, name);
            } else if (database instanceof H2Database && database.getDatabaseMajorVersion() >= 2) {
                String catalogName = database.correctObjectName(schema.getCatalogName(), Catalog.class);
                String schemaName = database.correctObjectName(schema.getName(), Schema.class);
                String constraintName = database.correctObjectName(name, UniqueConstraint.class);
                String tableName = database.correctObjectName(table.getName(), Table.class);
                StringBuilder sql = new StringBuilder("select table_constraints.CONSTRAINT_NAME, index_columns.COLUMN_NAME, table_constraints.constraint_schema as CONSTRAINT_CONTAINER ")
                        .append("from information_schema.table_constraints join information_schema.index_columns on index_columns.index_name=table_constraints.index_name where constraint_type='UNIQUE' ");
                if (catalogName != null) {
                    sql.append("and constraint_catalog=? ");
                    parameters.add(catalogName);
                }
                if (schemaName != null) {
                    sql.append("and constraint_schema=? ");
                    parameters.add(schemaName);
                }
                if (!bulkQuery) {
                    if (tableName != null) {
                        sql.append("and table_constraints.table_name=? ");
                        parameters.add(tableName);
                    }
                    if (constraintName != null) {
                        sql.append("and constraint_name=?");
                        parameters.add(constraintName);
                    }
                }
                rawSql = sql.toString();
            } else {
                // If we do not have a specific handler for the RDBMS, we assume that the database has an
                // INFORMATION_SCHEMA we can use. This is a last-resort measure and might fail.
                String catalogName = database.correctObjectName(schema.getCatalogName(), Catalog.class);
                String schemaName = database.correctObjectName(schema.getName(), Schema.class);
                String constraintName = database.correctObjectName(name, UniqueConstraint.class);
                String tableName = database.correctObjectName(table.getName(), Table.class);
                StringBuilder sql = new StringBuilder("select CONSTRAINT_NAME, COLUMN_LIST as COLUMN_NAME, constraint_schema as CONSTRAINT_CONTAINER ")
                        .append("from ?.constraints where constraint_type='UNIQUE' ");
                parameters.add(database.getSystemSchema());
                if (catalogName != null) {
                    sql.append("and constraint_catalog=? ");
                    parameters.add(catalogName);
                }
                if (schemaName != null) {
                    sql.append("and constraint_schema=? ");
                    parameters.add(schemaName);
                }

                if (!bulkQuery) {
                    if (tableName != null) {
                        sql.append("and table_name=? ");
                        parameters.add(tableName);
                    }
                    if (constraintName != null) {
                        sql.append("and constraint_name=?");
                        parameters.add(constraintName);
                    }
                }
                rawSql = sql.toString();
            }
            List<Map<String, ?>> rows = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForList(new RawParameterizedSqlStatement(rawSql, parameters.toArray()));

            if (bulkQuery) {
                columnCache = new ConcurrentHashMap<>();
                snapshot.setScratchData(cacheKey, columnCache);
                for (Map<String, ?> row : rows) {
                    String key = getCacheKey(row, database);
                    List<Map<String, ?>> constraintRows = columnCache.computeIfAbsent(key, k -> new ArrayList<>());
                    constraintRows.add(row);
                }

                return listColumns(example, database, snapshot);
            } else {
                return rows;
            }
        } else {
            String lookupKey = getCacheKey(example, database);
            List<Map<String, ?>> rows = columnCache.get(lookupKey);
            if (rows == null) {
                rows = new ArrayList<>();
            }
            return rows;
        }
    }

    /**
     * Should the given database include the table name in the key?
     * Databases that need to include the table names are ones where unique constraint names do not have to be unique
     * within the schema.
     * <p>
     * Currently only mysql is known to have non-unique constraint names.
     * <p>
     * If this returns true, the database-specific query in {@link #listColumns(UniqueConstraint, Database, DatabaseSnapshot)} must include
     * a TABLE_NAME column in the results for {@link #getCacheKey(Map, Database)} to use.
     */
    protected boolean includeTableNameInCacheKey(Database database) {
        return database instanceof MySQLDatabase;
    }


    /**
     * Return the cache key for the given UniqueConstraint. Must return the same result as {@link #getCacheKey(Map, Database)}.
     * Default implementation uses {@link #includeTableNameInCacheKey(Database)} to determine if the table name should be included in the key or not.
     */
    protected String getCacheKey(UniqueConstraint example, Database database) {
        if (includeTableNameInCacheKey(database)) {
            return example.getSchema().getName() + "_" + example.getRelation() + "_" + example.getName();
        } else {
            return example.getSchema().getName() + "_" + example.getName();
        }
    }

    /**
     * Return the cache key for the given query row. Must return the same result as {@link #getCacheKey(UniqueConstraint, Database)}
     * Default implementation uses {@link #includeTableNameInCacheKey(Database)} to determine if the table name should be included in the key or not.
     */
    protected String getCacheKey(Map<String, ?> row, Database database) {
        if (includeTableNameInCacheKey(database)) {
            return row.get("CONSTRAINT_CONTAINER") + "_" + row.get("TABLE_NAME") + "_" + row.get("CONSTRAINT_NAME");
        } else {
            return row.get("CONSTRAINT_CONTAINER") + "_" + row.get("CONSTRAINT_NAME");
        }
    }

    /**
     * To support bulk query, the resultSet must include a CONSTRAINT_CONTAINER column for caching purposes
     */
    protected boolean supportsBulkQuery(Database database) {
        return !(database instanceof DerbyDatabase)
                && !(database instanceof FirebirdDatabase)
                && !(database instanceof SybaseDatabase)
                && !(database instanceof SybaseASADatabase)
                && !(database instanceof Ingres9Database)
                && !(database instanceof InformixDatabase);
    }

    /**
     * Gets an SQL query that returns the constraint names and columns for all UNIQUE constraints.
     *
     * @param database A database object of the InformixDatabase type
     * @param schema   Name of the schema to examine (or null for all)
     * @param name     Name of the constraint to examine (or null for all)
     * @return A lengthy SQL statement that fetches the constraint names and columns
     */
    private String getUniqueConstraintsSqlInformix(InformixDatabase database, Schema schema, String name) {
        StringBuilder sqlBuf = new StringBuilder();

        sqlBuf.append("SELECT * FROM (\n");

        // Yes, I am serious about this. It appears there are neither CTE/WITH clauses nor PIVOT/UNPIVOT operators
        // in Informix SQL.
        for (int i = 1; i <= 16; i++) {
            if (i > 1)
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
            sqlBuf.append("AND owner='").append(catalogName).append("'\n");
        }
        if (constraintName != null) {
            sqlBuf.append("AND constraint_name='").append(constraintName).append("'");
        }

        // For correct processing, it is important that we get all columns in order.
        sqlBuf.append("ORDER BY owner, constraint_name, column_index");

        // Return the query
        return sqlBuf.toString();
    }
}
