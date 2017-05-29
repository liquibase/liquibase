package liquibase.snapshot.jvm;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
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
        super(Index.class, new Class[]{Table.class, ForeignKey.class, UniqueConstraint.class});
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(Index.class)) {
            return;
        }

        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            Database database = snapshot.getDatabase();
            Schema schema;
            schema = table.getSchema();


            List<CachedRow> rs = null;
            JdbcDatabaseSnapshot.CachingDatabaseMetaData databaseMetaData = null;
            try {
                databaseMetaData = ((JdbcDatabaseSnapshot) snapshot).getMetaData();

                rs = databaseMetaData.getIndexInfo(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), table.getName(), null);
                Map<String, Index> foundIndexes = new HashMap<String, Index>();
                for (CachedRow row : rs) {
                    String indexName = row.getString("INDEX_NAME");
                    if (indexName == null) {
                        continue;
                    }

                    if (database instanceof DB2Database && "SYSIBM".equals(row.getString("INDEX_QUALIFIER"))) {
                        continue;
                    }

                    Index index = foundIndexes.get(indexName);
                    if (index == null) {
                        index = new Index();
                        index.setName(indexName);
                        index.setTable(table);

                        short type = row.getShort("TYPE");
                        if (type == DatabaseMetaData.tableIndexClustered) {
                            index.setClustered(true);
                        } else if (database instanceof MSSQLDatabase) {
                            index.setClustered(false);
                        }

                        foundIndexes.put(indexName, index);
                    }
                    String ascOrDesc = row.getString("ASC_OR_DESC");
                    Boolean descending = "D".equals(ascOrDesc) ? Boolean.TRUE : "A".equals(ascOrDesc) ? Boolean.FALSE : null;
                    index.addColumn(new Column(row.getString("COLUMN_NAME")).setComputed(false).setDescending(descending).setRelation(index.getTable()));
                }

                //add clustered indexes first, than all others in case there is a clustered and non-clustered version of the same index. Prefer the clustered version
                List<Index> stillToAdd = new ArrayList<Index>();
                for (Index exampleIndex : foundIndexes.values()) {
                    if (exampleIndex.getClustered() != null && exampleIndex.getClustered()) {
                        table.getIndexes().add(exampleIndex);
                    } else {
                        stillToAdd.add(exampleIndex);
                    }
                }
                for (Index exampleIndex : stillToAdd) {
                    boolean alreadyAddedSimilar = false;
                    for (Index index : table.getIndexes()) {
                        if (DatabaseObjectComparatorFactory.getInstance().isSameObject(index, exampleIndex, null, database)) {
                            alreadyAddedSimilar = true;
                        }
                    }
                    if (!alreadyAddedSimilar) {
                        table.getIndexes().add(exampleIndex);
                    }
                }

            } catch (Exception e) {
                throw new DatabaseException(e);
            }
        }
        if (foundObject instanceof UniqueConstraint && ((UniqueConstraint) foundObject).getBackingIndex() == null
                && !(snapshot.getDatabase() instanceof DB2Database) && !(snapshot.getDatabase() instanceof DerbyDatabase)) {
            Index exampleIndex = new Index().setTable(((UniqueConstraint) foundObject).getTable());
            exampleIndex.getColumns().addAll(((UniqueConstraint) foundObject).getColumns());
            ((UniqueConstraint) foundObject).setBackingIndex(exampleIndex);
        }
        if (foundObject instanceof ForeignKey && ((ForeignKey) foundObject).getBackingIndex() == null) {
            Index exampleIndex = new Index().setTable(((ForeignKey) foundObject).getForeignKeyTable());
            exampleIndex.getColumns().addAll(((ForeignKey) foundObject).getForeignKeyColumns());
            ((ForeignKey) foundObject).setBackingIndex(exampleIndex);
        }
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        Table exampleTable = ((Index) example).getTable();

        String tableName = null;
        Schema schema = null;
        if (exampleTable != null) {
            tableName = exampleTable.getName();
            schema = exampleTable.getSchema();
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
                    continue; // suppress creation of generated_index records
                }
                short type = row.getShort("TYPE");
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
                    LogFactory.getInstance().getLog().debug(this.getClass().getName() + ": corrected position to " + ++position);
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

                if (columnName == null && definition == null) {
                    //nothing to index, not sure why these come through sometimes
                    continue;
                }
                Index returnIndex = foundIndexes.get(correctedIndexName);
                if (returnIndex == null) {
                    returnIndex = new Index();
                    returnIndex.setTable((Table) new Table().setName(row.getString("TABLE_NAME")).setSchema(schema));
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
                        if (definition == null) {
                            String ascOrDesc = row.getString("ASC_OR_DESC");
                            Boolean descending = "D".equals(ascOrDesc) ? Boolean.TRUE : "A".equals(ascOrDesc) ? Boolean.FALSE : null;
                            returnIndex.getColumns().set(position - 1, new Column(columnName).setDescending(descending).setRelation(returnIndex.getTable()));
                        } else {
                            returnIndex.getColumns().set(position - 1, new Column().setRelation(returnIndex.getTable()).setName(definition, true));
                        }
                    }
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
                if (DatabaseObjectComparatorFactory.getInstance().isSameObject(index.getTable(), exampleTable, snapshot.getSchemaComparisons(), database)) {
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

}
