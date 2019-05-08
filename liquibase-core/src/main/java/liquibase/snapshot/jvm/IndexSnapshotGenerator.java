package liquibase.snapshot.jvm;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.JdbcDatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexSnapshotGenerator extends JdbcSnapshotGenerator {
    public IndexSnapshotGenerator() {
        super(Index.class, new Class[]{Table.class, View.class, ForeignKey.class, UniqueConstraint.class});
    }


//    public Boolean has(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException {
//        Database database = snapshot.getDatabase();
//        if (!(example instanceof Index)) {
//            return chain.has(example, snapshot);
//        }
//        String tableName = ((Index) example).getTable().getName();
//        Schema schema = example.getSchema();
//
//        String indexName = example.getName();
//        String columnNames = ((Index) example).getColumnNames();
//
//        try {
//            if (tableName == null) {
//                Index newExample = new Index();
//                newExample.setName(indexName);
//                if (columnNames != null) {
//                    for (String column : columnNames.split("\\s*,\\s*")) {
//                        newExample.getColumns().add(column);
//                    }
//                }
//
//                ResultSet rs = getMetaData(database).getTables(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), null, new String[]{"TABLE"});
//                try {
//                    while (rs.next()) {
//                        String foundTable = rs.getString("TABLE_NAME");
//                        newExample.setTable((Table) new Table().setName(foundTable).setSchema(schema));
//                        if (has(newExample, snapshot, chain)) {
//                            return true;
//                        }
//                    }
//                    return false;
//                } finally {
//                    rs.close();
//                }
//            }
//
//            Index index = new Index();
//            index.setTable((Table) new Table().setName(tableName).setSchema(schema));
//            index.setName(indexName);
//            if (columnNames != null) {
//                for (String column : columnNames.split("\\s*,\\s*")) {
//                    index.getColumns().add(column);
//                }
//            }
//
//            if (columnNames != null) {
//                Map<String, TreeMap<Short, String>> columnsByIndexName = new HashMap<String, TreeMap<Short, String>>();
//                ResultSet rs = getMetaData(database).getIndexInfo(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), database.correctObjectName(tableName, Table.class), false, true);
//                try {
//                    while (rs.next()) {
//                        String foundIndexName = rs.getString("INDEX_NAME");
//                        if (indexName != null && indexName.equalsIgnoreCase(foundIndexName)) { //ok to use equalsIgnoreCase because we will check case later
//                            continue;
//                        }
//                        short ordinalPosition = rs.getShort("ORDINAL_POSITION");
//
//                        if (!columnsByIndexName.containsKey(foundIndexName)) {
//                            columnsByIndexName.put(foundIndexName, new TreeMap<Short, String>());
//                        }
//                        String columnName = rs.getString("COLUMN_NAME");
//                        Map<Short, String> columns = columnsByIndexName.get(foundIndexName);
//                        columns.put(ordinalPosition, columnName);
//                    }
//
//                    for (Map.Entry<String, TreeMap<Short, String>> foundIndexData : columnsByIndexName.entrySet()) {
//                        Index foundIndex = new Index()
//                                .setName(foundIndexData.getKey())
//                                .setTable(((Table) new Table().setName(tableName).setSchema(schema)));
//                        foundIndex.getColumns().addAll(foundIndexData.getValue().values());
//
//                        if (foundIndex.equals(index, database)) {
//                            return true;
//                        }
//                        return false;
//                    }
//                    return false;
//                } finally {
//                    rs.close();
//                }
//            } else if (indexName != null) {
//                ResultSet rs = getMetaData(database).getIndexInfo(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), database.correctObjectName(tableName, Table.class), false, true);
//                try {
//                    while (rs.next()) {
//                        Index foundIndex = new Index()
//                                .setName(rs.getString("INDEX_NAME"))
//                                .setTable(((Table) new Table().setName(tableName).setSchema(schema)));
//                        if (foundIndex.getName() == null) {
//                            continue;
//                        }
//                        if (foundIndex.equals(index, database)) {
//                            return true;
//                        }
//                    }
//                    return false;
//                } finally {
//                    try {
//                        rs.close();
//                    } catch (SQLException ignore) {
//                    }
//                }
//            } else {
//                throw new UnexpectedLiquibaseException("Either indexName or columnNames must be set");
//            }
//        } catch (SQLException e) {
//            throw new DatabaseException(e);
//        }
//
//    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(Index.class)) {
            return;
        }

        if (foundObject instanceof Table  || foundObject instanceof View) {
            if (foundObject instanceof View && !addToViews(snapshot.getDatabase())) {
                return;
            }

            Relation relation = (Relation) foundObject;
            Database database = snapshot.getDatabase();
            Schema schema;
            schema = relation.getSchema();


            List<CachedRow> rs = null;
            JdbcDatabaseSnapshot.CachingDatabaseMetaData databaseMetaData = null;
            try {
                databaseMetaData = ((JdbcDatabaseSnapshot) snapshot).getMetaData();

                rs = databaseMetaData.getIndexInfo(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), relation.getName(), null);
                Map<String, Index> foundIndexes = new HashMap<String, Index>();
                for (CachedRow row : rs) {
                    String indexName = row.getString("INDEX_NAME");
                    if (indexName == null) {
                        continue;
                    }

                    if (database instanceof AbstractDb2Database && "SYSIBM".equals(row.getString("INDEX_QUALIFIER"))) {
                        continue;
                    }

                    Index index = foundIndexes.get(indexName);
                    if (index == null) {
                        index = new Index();
                        index.setName(indexName);
                        index.setTable(relation);

                        short type = row.getShort("TYPE");
                        if (type == DatabaseMetaData.tableIndexClustered) {
                            index.setClustered(true);
                        } else if (database instanceof MSSQLDatabase) {
                            index.setClustered(false);
                        }

                        foundIndexes.put(indexName, index);
                    }

                    String ascOrDesc;
                    if (database instanceof Db2zDatabase) {
                        ascOrDesc = row.getString("ORDER");
                    } else {
                        ascOrDesc = row.getString("ASC_OR_DESC");
                    }
                    Boolean descending = "D".equals(ascOrDesc) ? Boolean.TRUE : "A".equals(ascOrDesc) ? Boolean.FALSE : null;
                    boolean computed = descending != null && descending;
                    index.addColumn(new Column(row.getString("COLUMN_NAME")).setComputed(computed).setDescending(descending).setRelation(index.getTable()));
                }

                //add clustered indexes first, than all others in case there is a clustered and non-clustered version of the same index. Prefer the clustered version
                List<Index> stillToAdd = new ArrayList<Index>();
                for (Index exampleIndex : foundIndexes.values()) {
                    if (exampleIndex.getClustered() != null && exampleIndex.getClustered()) {
                        relation.getIndexes().add(exampleIndex);
                    } else {
                        stillToAdd.add(exampleIndex);
                    }
                }
                for (Index exampleIndex : stillToAdd) {
                    boolean alreadyAddedSimilar = false;
                    for (Index index : relation.getIndexes()) {
                        if (DatabaseObjectComparatorFactory.getInstance().isSameObject(index, exampleIndex, null, database)) {
                            alreadyAddedSimilar = true;
                        }
                    }
                    if (!alreadyAddedSimilar) {
                        relation.getIndexes().add(exampleIndex);
                    }
                }

            } catch (Exception e) {
                throw new DatabaseException(e);
            }
        }
//        if (foundObject instanceof PrimaryKey) {
//            ((PrimaryKey) foundObject).setBackingIndex(new Index().setTable(((PrimaryKey) foundObject).getTable()).setName(foundObject.getName()));
//        }
        if (foundObject instanceof UniqueConstraint && ((UniqueConstraint) foundObject).getBackingIndex() == null && !(snapshot.getDatabase() instanceof DB2Database) && !(snapshot.getDatabase() instanceof DerbyDatabase)) {
            Index exampleIndex = new Index().setTable(((UniqueConstraint) foundObject).getTable());
            exampleIndex.getColumns().addAll(((UniqueConstraint) foundObject).getColumns());
            ((UniqueConstraint) foundObject).setBackingIndex(exampleIndex);
        }
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        Relation exampleIndex = ((Index) example).getTable();

        String tableName = null;
        Schema schema = null;
        if (exampleIndex != null) {
            tableName = exampleIndex.getName();
            schema = exampleIndex.getSchema();
        }

        if (schema == null) {
            schema = new Schema(database.getDefaultCatalogName(), database.getDefaultSchemaName());
        }


        for (int i = 0; i < ((Index) example).getColumns().size(); i++) {
            ((Index) example).getColumns().set(i, ((Index) example).getColumns().get(i));
        }

        String exampleName = example.getName();
        if (exampleName != null) {
            exampleName = database.correctObjectName(exampleName, Index.class);
        }

        Map<String, Index> foundIndexes = new HashMap<String, Index>();
        JdbcDatabaseSnapshot.CachingDatabaseMetaData databaseMetaData = null;
        List<CachedRow> rs = null;
        try {
            databaseMetaData = ((JdbcDatabaseSnapshot) snapshot).getMetaData();

            rs = databaseMetaData.getIndexInfo(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), tableName, exampleName);

            for (CachedRow row : rs) {
                String rawIndexName = row.getString("INDEX_NAME");
                String indexName = cleanNameFromDatabase(rawIndexName, database);
                String correctedIndexName = database.correctObjectName(indexName, Index.class);

                if (indexName == null) {
                    continue;
                }
                if (exampleName != null && !exampleName.equals(correctedIndexName)) {
                    continue;
                }
                /*
                * TODO Informix generates indexnames with a leading blank if no name given.
                * An identifier with a leading blank is not allowed.
                * So here is it replaced.
                */
                if (database instanceof InformixDatabase && indexName.startsWith(" ")) {
                    //indexName = "_generated_index_" + indexName.substring(1);
                    continue; // suppress creation of generated_index records
                }
                short type = row.getShort("TYPE");
                //                String tableName = rs.getString("TABLE_NAME");
                Boolean nonUnique = row.getBoolean("NON_UNIQUE");
                if (nonUnique == null) {
                    nonUnique = true;
                }

                String columnName = cleanNameFromDatabase(row.getString("COLUMN_NAME"), database);
                short position = row.getShort("ORDINAL_POSITION");
                /*
                * TODO maybe bug in jdbc driver? Need to investigate.
                * If this "if" is commented out ArrayOutOfBoundsException is thrown
                * because it tries to access an element -1 of a List (position-1)
                */
                if (database instanceof InformixDatabase
                        && type != DatabaseMetaData.tableIndexStatistic
                        && position == 0) {
                    System.out.println(this.getClass().getName() + ": corrected position to " + ++position);
                }
                String definition = StringUtils.trimToNull(row.getString("FILTER_CONDITION"));
                if (definition != null) {
                    if (!(database instanceof OracleDatabase)) { //TODO: this replaceAll code has been there for a long time but we don't know why. Investigate when it is ever needed and modify it to be smarter
                        definition = definition.replaceAll("\"", "");
                    }
                }

                if (type == DatabaseMetaData.tableIndexStatistic) {
                    continue;
                }
                //                if (type == DatabaseMetaData.tableIndexOther) {
                //                    continue;
                //                }

                if (columnName == null && definition == null) {
                    //nothing to index, not sure why these come through sometimes
                    continue;
                }
                Index returnIndex = foundIndexes.get(correctedIndexName);
                if (returnIndex == null) {
                    returnIndex = new Index();
                    Relation relation = new Table();
                    if ("V".equals(row.getString("INTERNAL_OBJECT_TYPE"))) {
                        relation = new View();
                    }
                    returnIndex.setTable(relation.setName(row.getString("TABLE_NAME")).setSchema(schema));
                    returnIndex.setName(indexName);
                    returnIndex.setUnique(!nonUnique);

                    if (type == DatabaseMetaData.tableIndexClustered) {
                        returnIndex.setClustered(true);
                    } else if (database instanceof MSSQLDatabase) {
                        returnIndex.setClustered(false);
                    }

                    if (database instanceof MSSQLDatabase) {
                        Boolean recompute = (Boolean) row.get("NO_RECOMPUTE");
                        if (recompute != null) {
                            recompute = !recompute;
                        }

                        returnIndex.setAttribute("padIndex", row.get("IS_PADDED"));
                        returnIndex.setAttribute("fillFactor", row.get("FILL_FACTOR"));
                        returnIndex.setAttribute("ignoreDuplicateKeys", row.get("IGNORE_DUP_KEY"));
                        returnIndex.setAttribute("recomputeStatistics", recompute);
                        returnIndex.setAttribute("incrementalStatistics", row.get("IS_INCREMENTAL"));
                        returnIndex.setAttribute("allowRowLocks", row.get("ALLOW_ROW_LOCKS"));
                        returnIndex.setAttribute("allowPageLocks", row.get("ALLOW_PAGE_LOCKS"));
                    }

                    foundIndexes.put(correctedIndexName, returnIndex);
                }

                if (database instanceof MSSQLDatabase && (Boolean) row.get("IS_INCLUDED_COLUMN")) {
                    List<String> includedColumns = returnIndex.getAttribute("includedColumns", List.class);
                    if (includedColumns == null) {
                        includedColumns = new ArrayList<String>();
                        returnIndex.setAttribute("includedColumns", includedColumns);
                    }
                    includedColumns.add(columnName);
                } else {
                    if (position != 0) { //if really a column, position is 1-based.
                        for (int i = returnIndex.getColumns().size(); i < position; i++) {
                            returnIndex.getColumns().add(null);
                        }
                        String ascOrDesc;
                        if (database instanceof Db2zDatabase) {
                            ascOrDesc =  row.getString("ORDER");
                        } else {
                            ascOrDesc = row.getString("ASC_OR_DESC");
                        }
                        Boolean descending = "D".equals(ascOrDesc) ? Boolean.TRUE : "A".equals(ascOrDesc) ? Boolean.FALSE : null;

                        boolean computed = false;
                        if (definition != null) {
                            computed = true;
                        } else if (descending != null && descending) {
                            definition = columnName;
                            computed = true;
                        }
                        returnIndex.getColumns().set(position - 1,
                                new Column().setDescending(descending)
                                        .setRelation(returnIndex.getTable())
                                        .setName(computed ? definition : columnName, computed));                        }
                    }
                }

        } catch (Exception e) {
            throw new DatabaseException(e);
        }

        if (exampleName != null) {
            Index index = null;

            // If we are informix then must alter the lookup if we get here
            // Wont get here now though due to the continue for generated indexes above
            if (database instanceof InformixDatabase) {
                index = foundIndexes.get("_generated_index_" + exampleName.substring(1));
            } else {
                index = foundIndexes.get(exampleName);
            }

            return index;
        } else {
            //prefer clustered version of the index
            List<Index> nonClusteredIndexes = new ArrayList<Index>();
            for (Index index : foundIndexes.values()) {
                if (DatabaseObjectComparatorFactory.getInstance().isSameObject(index.getTable(), exampleIndex, snapshot.getSchemaComparisons(), database)) {
                    boolean actuallyMatches = false;
                    if (database.isCaseSensitive()) {
                        if (index.getColumnNames().equals(((Index) example).getColumnNames())) {
                            actuallyMatches = true;
                        }
                    } else {
                        if (index.getColumnNames().equalsIgnoreCase(((Index) example).getColumnNames())) {
                            actuallyMatches = true;
                        }
                    }
                    if (actuallyMatches) {
                        if (index.getClustered() != null && index.getClustered()) {
                            return finalizeIndex(schema, tableName, index, snapshot);
                        } else {
                            nonClusteredIndexes.add(index);
                        }
                    }
                }
            }
            if (nonClusteredIndexes.size() > 0) {
                return finalizeIndex(schema, tableName, nonClusteredIndexes.get(0), snapshot);
            }
            return null;
        }

        //todo?
//        Set<Index> indexesToRemove = new HashSet<Index>();

        /*
   * marks indexes as "associated with" instead of "remove it"
   * Index should have associations with:
   * foreignKey, primaryKey or uniqueConstraint
   * */
//        for (Index index : snapshot.getDatabaseObjects(schema, Index.class)) {
//            for (PrimaryKey pk : snapshot.getDatabaseObjects(schema, PrimaryKey.class)) {
//                if (index.getTable().equals(pk.getTable().getName(), database) && columnNamesAreEqual(index.getColumnNames(), pk.getColumnNames(), database)) {
//                    index.addAssociatedWith(Index.MARK_PRIMARY_KEY);
//                }
//            }
//            for (ForeignKey fk : snapshot.getDatabaseObjects(schema, ForeignKey.class)) {
//                if (index.getTable().equals(fk.getForeignKeyTable().getName(), database) && columnNamesAreEqual(index.getColumnNames(), fk.getForeignKeyColumns(), database)) {
//                    index.addAssociatedWith(Index.MARK_FOREIGN_KEY);
//                }
//            }
//            for (UniqueConstraint uc : snapshot.getDatabaseObjects(schema, UniqueConstraint.class)) {
//                if (index.getTable().equals(uc.getTable()) && columnNamesAreEqual(index.getColumnNames(), uc.getColumnNames(), database)) {
//                    index.addAssociatedWith(Index.MARK_UNIQUE_CONSTRAINT);
//                }
//            }
//
//        }
//        snapshot.removeDatabaseObjects(schema, indexesToRemove.toArray(new Index[indexesToRemove.size()]));
    }

    protected Index finalizeIndex(Schema schema, String tableName, Index index, DatabaseSnapshot snapshot) {
        if (index.isUnique() == null || !index.isUnique()) {
            List<Column> columns = index.getColumns();
            PrimaryKey tablePK = new PrimaryKey(null, schema.getCatalogName(), schema.getName(), tableName, columns.toArray(new Column[index.getColumns().size()]));
            if (snapshot.get(tablePK) != null) { //actually is unique since it's the PK
                index.setUnique(true);
            }
        }

        return index;
    }

    protected boolean addToViews(Database database) {
        return database instanceof MSSQLDatabase;
    }

    //METHOD FROM SQLIteDatabaseSnapshotGenerator
    //    protected void readIndexes(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
//        Database database = snapshot.getDatabase();
//        updateListeners("Reading indexes for " + database.toString() + " ...");
//
//        for (Table table : snapshot.getTables()) {
//            ResultSet rs = null;
//            Statement statement = null;
//            Map<String, Index> indexMap;
//            try {
//                indexMap = new HashMap<String, Index>();
//
//                // for the odbc driver at http://www.ch-werner.de/sqliteodbc/
//                // databaseMetaData.getIndexInfo is not implemented
//                statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//                String sql = "PRAGMA index_list(" + table.getName() + ");";
//                try {
//                    rs = statement.executeQuery(sql);
//                } catch (SQLException e) {
//                    if (!e.getMessage().equals("query does not return ResultSet")) {
//                        System.err.println(e);
////            			throw e;
//                    }
//                }
//                while ((rs != null) && rs.next()) {
//                    String index_name = rs.getString("name");
//                    boolean index_unique = rs.getBoolean("unique");
//                    sql = "PRAGMA index_info(" + index_name + ");";
//                    Statement statement_2 = null;
//                    ResultSet rs_2 = null;
//                    try {
//                        statement_2 = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//                        rs_2 = statement_2.executeQuery(sql);
//                        while ((rs_2 != null) && rs_2.next()) {
//                            int index_column_seqno = rs_2.getInt("seqno");
////                		int index_column_cid = rs.getInt("cid");
//                            String index_column_name = rs_2.getString("name");
//                            if (index_unique) {
//                                Column column = snapshot.getColumn(table.getName(), index_column_name);
//                                column.setUnique(true);
//                            } else {
//                                Index indexInformation;
//                                if (indexMap.containsKey(index_name)) {
//                                    indexInformation = indexMap.get(index_name);
//                                } else {
//                                    indexInformation = new Index();
//                                    indexInformation.setTable(table);
//                                    indexInformation.setName(index_name);
//                                    indexInformation.setFilterCondition("");
//                                    indexMap.put(index_name, indexInformation);
//                                }
//                                indexInformation.getColumns().add(index_column_seqno, index_column_name);
//                            }
//                        }
//                    } finally {
//                        if (rs_2 != null) {
//                            try {
//                                rs_2.close();
//                            } catch (SQLException ignored) {
//                            }
//                        }
//                        if (statement_2 != null) {
//                            try {
//                                statement_2.close();
//                            } catch (SQLException ignored) {
//                            }
//                        }
//                    }
//
//                }
//            } finally {
//                if (rs != null) {
//                    try {
//                        rs.close();
//                    } catch (SQLException ignored) { }
//                }
//                if (statement != null) {
//                    try {
//                        statement.close();
//                    } catch (SQLException ignored) { }
//                }
//            }
//
//            for (Map.Entry<String, Index> entry : indexMap.entrySet()) {
//                snapshot.getIndexes().add(entry.getValue());
//            }
//        }
//
//        //remove PK indexes
//        Set<Index> indexesToRemove = new HashSet<Index>();
//        for (Index index : snapshot.getIndexes()) {
//            for (PrimaryKey pk : snapshot.getPrimaryKeys()) {
//                if (index.getTable().getName().equalsIgnoreCase(pk.getTable().getName())
//                        && index.getColumnNames().equals(pk.getColumnNames())) {
//                    indexesToRemove.add(index);
//                }
//            }
//        }
//        snapshot.getIndexes().removeAll(indexesToRemove);
//    }

//    THIS METHOD WAS FROM DerbyDatabaseSnapshotGenerator
//    public boolean hasIndex(Schema schema, String tableName, String indexName, String columnNames, Database database) throws DatabaseException {
//        try {
//            ResultSet rs = getMetaData(database).getIndexInfo(schema.getCatalogName(), schema.getName(), "%", false, true);
//            while (rs.next()) {
//                if (database.objectNamesEqual(rs.getString("INDEX_NAME"), indexName)) {
//                    return true;
//                }
//                if (tableName != null && columnNames != null) {
//                    if (database.objectNamesEqual(tableName, rs.getString("TABLE_NAME")) && database.objectNamesEqual(columnNames.replaceAll(" ",""), rs.getString("COLUMN_NAME").replaceAll(" ",""))) {
//                        return true;
//                    }
//                }
//            }
//            return false;
//        } catch (SQLException e) {
//            throw new DatabaseException(e);
//        }
//    }

    //below code is from OracleDatabaseSnapshotGenerator
//    @Override
//    protected void readIndexes(DatabaseSnapshot snapshot, Schema schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
//        Database database = snapshot.getDatabase();
//        schema = database.correctSchema(schema);
//        updateListeners("Reading indexes for " + database.toString() + " ...");
//
//        String query = "select aic.index_name, 3 AS TYPE, aic.table_name, aic.column_name, aic.column_position AS ORDINAL_POSITION, null AS FILTER_CONDITION, ai.tablespace_name AS TABLESPACE, ai.uniqueness FROM all_ind_columns aic, all_indexes ai WHERE aic.table_owner='" + schema.getName() + "' and ai.table_owner='" + schema.getName() + "' and aic.index_name = ai.index_name ORDER BY INDEX_NAME, ORDINAL_POSITION";
//        Statement statement = null;
//        ResultSet rs = null;
//        Map<String, Index> indexMap = null;
//        try {
//            statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//            rs = statement.executeQuery(query);
//
//            indexMap = new HashMap<String, Index>();
//            while (rs.next()) {
//                String indexName = cleanObjectNameFromDatabase(rs.getString("INDEX_NAME"));
//                String tableName = rs.getString("TABLE_NAME");
//                String tableSpace = rs.getString("TABLESPACE");
//                String columnName = cleanObjectNameFromDatabase(rs.getString("COLUMN_NAME"));
//                if (columnName == null) {
//                    //nothing to index, not sure why these come through sometimes
//                    continue;
//                }
//                short type = rs.getShort("TYPE");
//
//                boolean nonUnique;
//
//                String uniqueness = rs.getString("UNIQUENESS");
//
//                if ("UNIQUE".equals(uniqueness)) {
//                    nonUnique = false;
//                } else {
//                    nonUnique = true;
//                }
//
//                short position = rs.getShort("ORDINAL_POSITION");
//                String filterCondition = rs.getString("FILTER_CONDITION");
//
//                if (type == DatabaseMetaData.tableIndexStatistic) {
//                    continue;
//                }
//
//                Index index;
//                if (indexMap.containsKey(indexName)) {
//                    index = indexMap.get(indexName);
//                } else {
//                    index = new Index();
//                    Table table = snapshot.getDatabaseObject(schema, tableName, Table.class);
//                    if (table == null) {
//                        continue; //probably different schema
//                    }
//                    index.setTable(table);
//                    index.setTablespace(tableSpace);
//                    index.setName(indexName);
//                    index.setUnique(!nonUnique);
//                    index.setFilterCondition(filterCondition);
//                    indexMap.put(indexName, index);
//                }
//
//                for (int i = index.getColumns().size(); i < position; i++) {
//                    index.getColumns().add(null);
//                }
//                index.getColumns().set(position - 1, columnName);
//            }
//        } finally {
//            JdbcUtils.closeResultSet(rs);
//            JdbcUtils.closeStatement(statement);
//        }
//
//        for (Map.Entry<String, Index> entry : indexMap.entrySet()) {
//            snapshot.addDatabaseObjects(entry.getValue());
//        }
//
//        /*
//          * marks indexes as "associated with" instead of "remove it"
//          * Index should have associations with:
//          * foreignKey, primaryKey or uniqueConstraint
//          * */
//        for (Index index : snapshot.getDatabaseObjects(schema, Index.class)) {
//            for (PrimaryKey pk : snapshot.getDatabaseObjects(schema, PrimaryKey.class)) {
//                if (index.getTable().equals(pk.getTable().getName(), database) && columnNamesAreEqual(index.getColumnNames(), pk.getColumnNames(), database)) {
//                    index.addAssociatedWith(Index.MARK_PRIMARY_KEY);
//                }
//            }
//            for (ForeignKey fk : snapshot.getDatabaseObjects(schema, ForeignKey.class)) {
//                if (index.getTable().equals(fk.getForeignKeyTable().getName(), database) && columnNamesAreEqual(index.getColumnNames(), fk.getForeignKeyColumns(), database)) {
//                    index.addAssociatedWith(Index.MARK_FOREIGN_KEY);
//                }
//            }
//            for (UniqueConstraint uc : snapshot.getDatabaseObjects(schema, UniqueConstraint.class)) {
//                if (index.getTable().equals(uc.getTable()) && columnNamesAreEqual(index.getColumnNames(), uc.getColumnNames(), database)) {
//                    index.addAssociatedWith(Index.MARK_UNIQUE_CONSTRAINT);
//                }
//            }
//
//        }
//
//    }
}
