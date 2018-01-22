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

/**
 * Analyses the properties of a database index and creates an object representation ("snapshot").
 */
public class IndexSnapshotGenerator extends JdbcSnapshotGenerator {
    public IndexSnapshotGenerator() {
        super(Index.class, new Class[]{Table.class, View.class, ForeignKey.class, UniqueConstraint.class});
    }

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
                databaseMetaData = ((JdbcDatabaseSnapshot) snapshot).getMetaDataFromCache();

                rs = databaseMetaData.getIndexInfo(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), relation.getName(), null);
                Map<String, Index> foundIndexes = new HashMap<>();
                for (CachedRow row : rs) {
                    String indexName = row.getString("INDEX_NAME");
                    if (indexName == null) {
                        continue;
                    }

                    if ((database instanceof AbstractDb2Database) && "SYSIBM".equals(row.getString("INDEX_QUALIFIER"))) {
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
                    Boolean descending = "D".equals(ascOrDesc) ? Boolean.TRUE : ("A".equals(ascOrDesc) ? Boolean
                        .FALSE : null);
                    index.addColumn(new Column(row.getString("COLUMN_NAME")).setComputed(false).setDescending(descending).setRelation(index.getTable()));
                }

                //add clustered indexes first, than all others in case there is a clustered and non-clustered version of the same index. Prefer the clustered version
                List<Index> stillToAdd = new ArrayList<>();
                for (Index exampleIndex : foundIndexes.values()) {
                    if ((exampleIndex.getClustered() != null) && exampleIndex.getClustered()) {
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
        if ((foundObject instanceof UniqueConstraint) && (((UniqueConstraint) foundObject).getBackingIndex() == null)
            && !(snapshot.getDatabase() instanceof DB2Database) && !(snapshot.getDatabase() instanceof DerbyDatabase)) {
            Index exampleIndex = new Index().setTable(((UniqueConstraint) foundObject).getTable());
            exampleIndex.getColumns().addAll(((UniqueConstraint) foundObject).getColumns());
            ((UniqueConstraint) foundObject).setBackingIndex(exampleIndex);
        }
        if ((foundObject instanceof ForeignKey) && (((ForeignKey) foundObject).getBackingIndex() == null)) {
            Index exampleIndex = new Index().setTable(((ForeignKey) foundObject).getForeignKeyTable());
            exampleIndex.getColumns().addAll(((ForeignKey) foundObject).getForeignKeyColumns());
            ((ForeignKey) foundObject).setBackingIndex(exampleIndex);
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

        Map<String, Index> foundIndexes = new HashMap<>();
        JdbcDatabaseSnapshot.CachingDatabaseMetaData databaseMetaData = null;
        List<CachedRow> rs = null;
        try {
            databaseMetaData = ((JdbcDatabaseSnapshot) snapshot).getMetaDataFromCache();

            rs = databaseMetaData.getIndexInfo(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), tableName, exampleName);

            for (CachedRow row : rs) {
                String rawIndexName = row.getString("INDEX_NAME");
                String indexName = cleanNameFromDatabase(rawIndexName, database);
                String correctedIndexName = database.correctObjectName(indexName, Index.class);

                if (indexName == null) {
                    continue;
                }
                if ((exampleName != null) && !exampleName.equals(correctedIndexName)) {
                    continue;
                }
                /*
                * TODO Informix generates indexnames with a leading blank if no name given.
                * An identifier with a leading blank is not allowed.
                * So here is it replaced.
                */
                if ((database instanceof InformixDatabase) && indexName.startsWith(" ")) {
                    continue; // suppress creation of generated_index records
                }
                short type = row.getShort("TYPE");
                Boolean nonUnique = row.getBoolean("NON_UNIQUE");
                if (nonUnique == null) {
                    nonUnique = true;
                }

                String columnName = cleanNameFromDatabase(row.getString("COLUMN_NAME"), database);
                short position = row.getShort("ORDINAL_POSITION");

                String definition = StringUtils.trimToNull(row.getString("FILTER_CONDITION"));
                if (definition != null) {
                    if (!(database instanceof OracleDatabase)) { //TODO: this replaceAll code has been there for a long time but we don't know why. Investigate when it is ever needed and modify it to be smarter
                        definition = definition.replaceAll("\"", "");
                    }
                }

                if ((columnName == null) && (definition == null)) {
                    //nothing to index, not sure why these come through sometimes
                    continue;
                }

                if (type == DatabaseMetaData.tableIndexStatistic) {
                    continue;
                }

                /*
                 * In Oracle database, ALL_IND_COLUMNS/ALL_IND_EXPRESSIONS (the views from which we bulk-fetch the
                 * column definitions for a given index) can show a strange behaviour if an index column consists of
                 * a regular table column, but its sort order is DESC(ending). In this case, we get something like
                 * this (example values):
                 * ALL_IND_COLUMNS.COLUMN_NAME=SYS_NC00006$
                 * ALL_IND_EXPRESSIONS.COLUMN_EXPRESSIONS="COLUMN1FORDESC"
                 * Note that the quote characters (") are part of the actual column value!
                 * Our strategy here is: If the expression would be a valid Oracle identifier, but not a valid Oracle
                 * function name, then we assume it is the name of a regular column.
                 */
                if ((database instanceof OracleDatabase) && (definition != null) && (columnName != null))
                {
                    String potentialColumnExpression = definition.replaceFirst("^\"?(.*?)\"?$", "$1");
                    OracleDatabase oracle = (OracleDatabase)database;
                    if (oracle.isValidOracleIdentifier(potentialColumnExpression, Index.class)
                        && (!oracle.isFunction(potentialColumnExpression))) {
                        columnName = potentialColumnExpression;
                        definition = null;
                    }
                }

                // Have we already seen/found this index? If not, let's read its properties!
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

                    String tablespaceName = row.getString("TABLESPACE_NAME");
                    if ((tablespaceName != null) && database.supportsTablespaces()) {
                        returnIndex.setTablespace(tablespaceName);
                    }

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

                if ((database instanceof MSSQLDatabase) && (Boolean) row.get("IS_INCLUDED_COLUMN")) {
                    List<String> includedColumns = returnIndex.getAttribute("includedColumns", List.class);
                    if (includedColumns == null) {
                        includedColumns = new ArrayList<>();
                        returnIndex.setAttribute("includedColumns", includedColumns);
                    }
                    includedColumns.add(columnName);
                } else {
                    if (position != 0) { //if really a column, position is 1-based.

                        /*
                         * TODO: It seems the original author was not completely sure that the columns/expressions
                         * that make up this index would arrive in order, i.e. they thought it could happen that we
                         * get the (example) 4 columns in the order 3,4,1,2. So, instead of doing a simple add to the
                         * collection, they chose this: First, make sure that an (empty) element of "position-1" exists
                         * in the getColumns() array (add nulls until this is the case). After that, we can safely
                         * replace the "position-1"th element. I am not sure if this really necessary, but it might
                         * improve stability, so I leave it in place for the moment.
                         */
                        for (int i = returnIndex.getColumns().size(); i < position; i++) {
                            returnIndex.getColumns().add(null);
                        }

                        // Is this column a simple column (definition == null)
                        // or is it a computed expression (definition != null)
                        if (definition == null) {
                            String ascOrDesc;
                            if (database instanceof Db2zDatabase) {
                                ascOrDesc =  row.getString("ORDER");
                            } else {
                                ascOrDesc = row.getString("ASC_OR_DESC");
                            }
                            Boolean descending = "D".equals(ascOrDesc) ? Boolean.TRUE : ("A".equals(ascOrDesc) ?
                                Boolean.FALSE : null);
                            returnIndex.getColumns().set(position - 1, new Column(columnName)
                                    .setDescending(descending).setRelation(returnIndex.getTable()));
                        } else {
                            returnIndex.getColumns().set(position - 1, new Column()
                                    .setRelation(returnIndex.getTable()).setName(definition, true));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new DatabaseException(e);
        }

        if (exampleName != null) {
            Index index = foundIndexes.get(exampleName);
            return index;
        } else {
            //prefer clustered version of the index
            List<Index> nonClusteredIndexes = new ArrayList<>();
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
                        if ((index.getClustered() != null) && index.getClustered()) {
                            return finalizeIndex(schema, tableName, index, snapshot);
                        } else {
                            nonClusteredIndexes.add(index);
                        }
                    }
                }
            }
            if (!nonClusteredIndexes.isEmpty()) {
                return finalizeIndex(schema, tableName, nonClusteredIndexes.get(0), snapshot);
            }
            return null;
        }
    }

    protected Index finalizeIndex(Schema schema, String tableName, Index index, DatabaseSnapshot snapshot) {
        if ((index.isUnique() == null) || !index.isUnique()) {
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
}
