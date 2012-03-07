package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.structure.*;
import liquibase.database.structure.DataType;
import liquibase.diff.DiffControl;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.statement.core.SelectSequencesStatement;
import liquibase.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class JdbcDatabaseSnapshotGenerator implements DatabaseSnapshotGenerator {

    private Set<DiffStatusListener> statusListeners;

    public Table getDatabaseChangeLogTable(Database database) throws DatabaseException {
        return getTable(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName(), database);
    }

    public Table getDatabaseChangeLogLockTable(Database database) throws DatabaseException {
        return getTable(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName(), database);
    }

    public boolean hasDatabaseChangeLogTable(Database database) {
        return hasTable(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName(), database);
    }

    public boolean hasDatabaseChangeLogLockTable(Database database) {
        return hasTable(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName(), database);
    }

    public boolean hasTable(String catalogName, String schemaName, String tableName, Database database) {
        try {
            ResultSet rs = getMetaData(database).getTables(database.correctCatalogName(catalogName), database.correctSchemaName(schemaName), database.correctTableName(tableName), new String[]{"TABLE"});
            try {
                return rs.next();
            } finally {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public boolean hasView(String catalogName, String schemaName, String viewName, Database database) {
        try {
            ResultSet rs = getMetaData(database).getTables(database.correctCatalogName(catalogName), database.correctSchemaName(schemaName), database.correctTableName(viewName), new String[]{"VIEW"});
            try {
                return rs.next();
            } finally {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public Table getTable(String catalogName, String schemaName, String tableName, Database database) throws DatabaseException {
        ResultSet rs = null;
        try {
            DatabaseMetaData metaData = getMetaData(database);
            rs = metaData.getTables(database.correctCatalogName(catalogName), database.correctSchemaName(schemaName), database.correctTableName(tableName), new String[]{"TABLE"});

            Table table;
            try {
                if (!rs.next()) {
                    return null;
                }

                table = readTable(rs, database, true);
            } finally {
                rs.close();
            }

            return table;
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }
            }
        }
    }

    public boolean hasColumn(String catalogName, String schemaName, String tableName, String columnName, Database database) {
        try {
            return getColumn(catalogName, schemaName, tableName, columnName, database) != null;
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public boolean hasPrimaryKey(Schema schema, String tableName, String primaryKeyName, Database database) {
        DatabaseSnapshot snapshot;
        try {
            snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, new DiffControl(schema, PrimaryKey.class));
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        if (tableName != null) {
            return snapshot.getPrimaryKeyForTable(schema, tableName) != null;
        } else if (primaryKeyName != null) {
            return snapshot.getPrimaryKeyForTable(schema, primaryKeyName) != null;
        } else {
            throw new UnexpectedLiquibaseException("hasPrimaryKey requires a tableName or primaryKeyName");
        }
    }

    public Column getColumn(String catalogName, String schemaName, String tableName, String columnName, Database database) throws DatabaseException {
        ResultSet rs = null;
        try {
            rs = getMetaData(database).getColumns(database.correctCatalogName(catalogName), database.correctSchemaName(schemaName), database.correctTableName(tableName), database.correctColumnName(columnName));

            if (!rs.next()) {
                return null;
            }

            Table table = new Table(database.correctTableName(tableName));
            table.setSchema(new Schema(new Catalog(database.correctCatalogName(catalogName)), database.correctSchemaName(schemaName)));
            return readColumn(rs, table, database);
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }
            }
        }
    }

    protected Table readTable(ResultSet tableMetadataResultSet, Database database, boolean readColumns) throws SQLException, DatabaseException {
        String rawTableName = tableMetadataResultSet.getString("TABLE_NAME");
        String rawSchemaName = StringUtils.trimToNull(tableMetadataResultSet.getString("TABLE_SCHEM"));
        String rawCatalogName = StringUtils.trimToNull(tableMetadataResultSet.getString("TABLE_CAT"));
        String remarks = StringUtils.trimToNull(tableMetadataResultSet.getString("REMARKS"));

        Table table = new Table(cleanObjectNameFromDatabase(rawTableName));
        table.setRemarks(remarks);
        table.setDatabase(database);
        table.setRawSchemaName(rawSchemaName);
        table.setRawCatalogName(rawCatalogName);

        table.setSchema(new Schema(new Catalog(database.correctCatalogName(rawCatalogName)), database.correctSchemaName(rawSchemaName)));

        if (readColumns) {
            ResultSet columnMetadataResultSet = getMetaData(database).getColumns(table.getRawCatalogName(), table.getRawSchemaName(), rawTableName, null);
            try {
                while (columnMetadataResultSet.next()) {
                    table.getColumns().add(readColumn(columnMetadataResultSet, table, database));
                }
            } finally {
                columnMetadataResultSet.close();
            }
        }

        return table;
    }

    protected View readView(ResultSet viewMetadataResultSet, Database database) throws SQLException, DatabaseException {
        String rawViewName = viewMetadataResultSet.getString("TABLE_NAME");
        String rawSchemaName = StringUtils.trimToNull(viewMetadataResultSet.getString("TABLE_SCHEM"));
        String rawCatalogName = StringUtils.trimToNull(viewMetadataResultSet.getString("TABLE_CAT"));
        String remarks = viewMetadataResultSet.getString("REMARKS");

        View view = new View(cleanObjectNameFromDatabase(rawViewName));
        view.setRemarks(remarks);
        view.setDatabase(database);
        view.setRawSchemaName(rawSchemaName);
        view.setRawCatalogName(rawCatalogName);

        view.setSchema(new Schema(rawCatalogName, rawSchemaName));

        try {
            view.setDefinition(database.getViewDefinition(view.getSchema(), view.getName()));
        } catch (DatabaseException e) {
            throw new DatabaseException("Error getting " + database.getConnection().getURL() + " view with " + new GetViewDefinitionStatement(view.getSchema().getCatalog().getName(), view.getSchema().getName(), rawViewName), e);
        }

        return view;
    }

    protected Column readColumn(ResultSet columnMetadataResultSet, Relation table, Database database) throws SQLException, DatabaseException {
        String rawTableName = columnMetadataResultSet.getString("TABLE_NAME");
        String rawColumnName = columnMetadataResultSet.getString("COLUMN_NAME");
        String rawSchemaName = StringUtils.trimToNull(columnMetadataResultSet.getString("TABLE_SCHEM"));
        String rawCatalogName = StringUtils.trimToNull(columnMetadataResultSet.getString("TABLE_CAT"));
        String remarks = StringUtils.trimToNull(columnMetadataResultSet.getString("REMARKS"));

        Schema schema = new Schema(rawCatalogName, rawSchemaName);
        if (database.isSystemTable(schema, rawTableName) || database.isSystemView(schema, rawTableName)) {
            return null;
        }

        Column column = new Column();
        column.setName(rawColumnName);
        column.setRelation(table);
        column.setRemarks(remarks);

        int nullable = columnMetadataResultSet.getInt("NULLABLE");
        if (nullable == DatabaseMetaData.columnNoNulls) {
            column.setNullable(false);
        } else if (nullable == DatabaseMetaData.columnNullable) {
            column.setNullable(true);
        } else if (nullable == DatabaseMetaData.columnNullableUnknown) {
            LogFactory.getLogger().info("Unknown nullable state for column " + column.toString() + ". Assuming nullable");
            column.setNullable(true);
        }

        try {
            String isAutoincrement = columnMetadataResultSet.getString("IS_AUTOINCREMENT");
            if (isAutoincrement.equals("YES")) {
                column.setAutoIncrement(true);
            } else if (isAutoincrement.equals("NO")) {
                column.setAutoIncrement(false);
            } else if (isAutoincrement.equals("")) {
                LogFactory.getLogger().info("Unknown auto increment state for column " + column.toString() + ". Assuming not auto increment");
                column.setAutoIncrement(false);
            } else {
                throw new UnexpectedLiquibaseException("Unknown is_autoincrement value: " + isAutoincrement);
            }
        } catch (SQLException e) {
            //probably older version of java, need to select from the column to find out if it is auto-increment
            String selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + database.escapeTableName(rawCatalogName, rawSchemaName, rawTableName) + " where 0=1";
            Connection underlyingConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
            Statement statement = underlyingConnection.createStatement();
            ResultSet columnSelectRS = statement.executeQuery(selectStatement);
            try {
                if (columnSelectRS.getMetaData().isAutoIncrement(1)) {
                    column.setAutoIncrement(true);
                } else {
                    column.setAutoIncrement(false);
                }
            } finally {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }
                columnSelectRS.close();
            }
        }

        DataType type = readDataType(columnMetadataResultSet, column, database);
        column.setType(type);

        column.setDefaultValue(readDefaultValue(columnMetadataResultSet, column, database));

        return column;
    }

    protected DataType readDataType(ResultSet columnMetadataResultSet, Column column, Database database) throws SQLException {
        String columnTypeName = columnMetadataResultSet.getString("TYPE_NAME");

        int dataType = columnMetadataResultSet.getInt("DATA_TYPE");
        Integer columnSize = columnMetadataResultSet.getInt("COLUMN_SIZE");
        if (columnMetadataResultSet.wasNull()) {
            columnSize = null;
        }

        Integer decimalDigits = columnMetadataResultSet.getInt("DECIMAL_DIGITS");
        if (columnMetadataResultSet.wasNull() || decimalDigits.equals(0)) {
            decimalDigits = null;
        }

        Integer radix = columnMetadataResultSet.getInt("NUM_PREC_RADIX");
        if (columnMetadataResultSet.wasNull()) {
            radix = null;
        }
        Integer characterOctetLength = columnMetadataResultSet.getInt("CHAR_OCTET_LENGTH");
        if (columnMetadataResultSet.wasNull()) {
            characterOctetLength = null;
        }

        DataType type = new DataType(columnTypeName);
        type.setDataTypeId(dataType);
        type.setColumnSize(columnSize);
        type.setDecimalDigits(decimalDigits);
        type.setRadix(radix);
        type.setCharacterOctetLength(characterOctetLength);
        type.setColumnSizeUnit(DataType.ColumnSizeUnit.BYTE);

        return type;
    }

    public DatabaseSnapshot createSnapshot(Database database, DiffControl diffControl, DiffControl.DatabaseRole type) throws DatabaseException {
        try {
            DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
            DatabaseMetaData databaseMetaData = getMetaData(database);
            this.statusListeners = diffControl.getStatusListeners();


            for (Schema schema : diffControl.getSchemas(type)) {
                readTables(snapshot, schema, databaseMetaData);
                readViews(snapshot, schema, databaseMetaData);
                readForeignKeys(snapshot, schema, databaseMetaData);
                readPrimaryKeys(snapshot, schema, databaseMetaData);
                readColumns(snapshot, schema, databaseMetaData);
                readUniqueConstraints(snapshot, schema, databaseMetaData);
                readIndexes(snapshot, schema, databaseMetaData);
                readSequences(snapshot, schema, databaseMetaData);

            }
            ;

            return snapshot;

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    protected DatabaseMetaData getMetaData(Database database) throws SQLException {
        DatabaseMetaData databaseMetaData = null;
        if (database.getConnection() != null) {
            databaseMetaData = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().getMetaData();
        }
        return databaseMetaData;
    }


    protected void readTables(DatabaseSnapshot snapshot, Schema schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading tables for " + database.toString() + " ...");

        ResultSet tableMetaDataRs = databaseMetaData.getTables(schema.getCatalogName(snapshot.getDatabase()), schema.getName(snapshot.getDatabase()), null, new String[]{"TABLE", "ALIAS"});
        try {
            while (tableMetaDataRs.next()) {
                Table table = readTable(tableMetaDataRs, database, false);
                if (database.isLiquibaseTable(table.getName())) {
                    if (table.getName().equals(database.getDatabaseChangeLogTableName())) {
                        snapshot.setDatabaseChangeLogTable(table);
                        continue;
                    }

                    if (table.getName().equals(database.getDatabaseChangeLogLockTableName())) {
                        snapshot.setDatabaseChangeLogLockTable(table);
                        continue;
                    }
                }
                if (database.isSystemTable(table.getSchema(), table.getName())) {
                    continue;
                }

                snapshot.addDatabaseObjects(table);
            }
        } finally {
            try {
                tableMetaDataRs.close();
            } catch (SQLException ignore) {
            }
        }
    }

    protected void readViews(DatabaseSnapshot snapshot, Schema schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading views for " + database.toString() + " ...");

        ResultSet viewsMetadataRs = databaseMetaData.getTables(schema.getCatalogName(snapshot.getDatabase()), schema.getName(snapshot.getDatabase()), null, new String[]{"VIEW"});
        try {
            while (viewsMetadataRs.next()) {
                View view = readView(viewsMetadataRs, database);
                if (database.isSystemView(schema, view.getName())) {
                    continue;
                }

                snapshot.addDatabaseObjects(view);
            }
        } finally {
            try {
                viewsMetadataRs.close();
            } catch (SQLException ignore) {
            }
        }
    }

    protected String cleanObjectNameFromDatabase(String objectName) {
        if (objectName == null) {
            return null;
        }
        return objectName;
    }

    protected void readColumns(DatabaseSnapshot snapshot, Schema schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading columns for " + database.toString() + " ...");

        ResultSet allColumnsMetadataRs = null;
        try {
            allColumnsMetadataRs = databaseMetaData.getColumns(schema.getCatalogName(snapshot.getDatabase()), schema.getName(snapshot.getDatabase()), null, null);
            while (allColumnsMetadataRs.next()) {
                String tableOrViewName = cleanObjectNameFromDatabase(allColumnsMetadataRs.getString("TABLE_NAME"));
                Relation relation = snapshot.getDatabaseObject(schema, tableOrViewName, Table.class);
                if (relation == null) {
                    relation = snapshot.getDatabaseObject(schema, tableOrViewName, View.class);
                }

                if (relation == null) {
                    if (snapshot.getDatabaseChangeLogTable() != null && snapshot.getDatabaseChangeLogTable().equals(relation)) {
                        relation = snapshot.getDatabaseChangeLogTable();
                    } else if (snapshot.getDatabaseChangeLogLockTable() != null && snapshot.getDatabaseChangeLogLockTable().equals(relation)) {
                        relation = snapshot.getDatabaseChangeLogLockTable();
                    } else {
                        continue;
                    }
                }

                Column column = readColumn(allColumnsMetadataRs, relation, database);

                if (column == null) {
                    continue;
                }

                relation.getColumns().add(column);
            }
        } finally {
            if (allColumnsMetadataRs != null) {
                try {
                    allColumnsMetadataRs.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    protected Object readDefaultValue(ResultSet columnMetadataResultSet, Column columnInfo, Database database) throws SQLException, DatabaseException {
        Object val = columnMetadataResultSet.getObject("COLUMN_DEF");
        if (columnMetadataResultSet.wasNull()) {
            return null;
        }
        if (val instanceof String && val.equals("")) {
            return null;
        }

        if (val instanceof String) {
            String stringVal = (String) val;
            if (stringVal.startsWith("'") && stringVal.endsWith("'")) {
                stringVal = stringVal.substring(1, stringVal.length() - 1);
            } else if (stringVal.startsWith("(") && stringVal.endsWith(")")) {
                return new DatabaseFunction(stringVal.substring(1, stringVal.length() - 1));
            }

            int type = columnInfo.getType().getDataTypeId();
            try {
                if (type == Types.ARRAY) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.BIGINT) {
                    return new BigInteger(stringVal);
                } else if (type == Types.BINARY) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.BIT) {
                    return new Integer(stringVal);
                } else if (type == Types.BLOB) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.BOOLEAN) {
                    return Boolean.valueOf(stringVal);
                } else if (type == Types.CHAR) {
                    return stringVal;
                } else if (type == Types.DATALINK) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.DATE) {
                    return new java.sql.Date(getDateFormat().parse(stringVal).getTime());
                } else if (type == Types.DECIMAL) {
                    return new BigDecimal(stringVal);
                } else if (type == Types.DISTINCT) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.DOUBLE) {
                    return Double.valueOf(stringVal);
                } else if (type == Types.FLOAT) {
                    return Float.valueOf(stringVal);
                } else if (type == Types.INTEGER) {
                    return Integer.valueOf(stringVal);
                } else if (type == Types.JAVA_OBJECT) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.LONGNVARCHAR) {
                    return stringVal;
                } else if (type == Types.LONGVARBINARY) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.LONGVARCHAR) {
                    return stringVal;
                } else if (type == Types.NCHAR) {
                    return stringVal;
                } else if (type == Types.NCLOB) {
                    return stringVal;
                } else if (type == Types.NULL) {
                    return null;
                } else if (type == Types.NUMERIC) {
                    return new BigDecimal(stringVal);
                } else if (type == Types.NVARCHAR) {
                    return stringVal;
                } else if (type == Types.OTHER) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.REAL) {
                    return new BigDecimal(stringVal);
                } else if (type == Types.REF) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.ROWID) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.SMALLINT) {
                    return Integer.valueOf(stringVal);
                } else if (type == Types.SQLXML) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.STRUCT) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.TIME) {
                    return new java.sql.Time(getTimeFormat().parse(stringVal).getTime());
                } else if (type == Types.TIMESTAMP) {
                    return new Timestamp(getDateTimeFormat().parse(stringVal).getTime());
                } else if (type == Types.TINYINT) {
                    return Integer.valueOf(stringVal);
                } else if (type == Types.VARBINARY) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.VARCHAR) {
                    return stringVal;
                } else {
                    LogFactory.getLogger().info("Unknown type: " + type);
                    return new DatabaseFunction(stringVal);
                }
            } catch (ParseException e) {
                return new DatabaseFunction(stringVal);
            }
        }
        return val;
    }

    protected void readForeignKeys(DatabaseSnapshot snapshot, Schema schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading foreign keys for " + database.toString() + " ...");

//        String dbSchema = database.convertRequestedSchemaToSchema(schema);
        // First we try to find all database-specific FKs.
        // TODO: there are some filters bellow in for loop. Are they needed here too?
//        snapshot.getForeignKeys().addAll(getAdditionalForeignKeys(dbSchema, database));

        // Then tries to find all other standard FKs
        for (Table table : snapshot.getDatabaseObjects(schema, Table.class)) {
            ResultSet importedKeyMetadataResultSet = getMetaData(database).getImportedKeys(table.getRawCatalogName(), table.getRawSchemaName(), table.getName());

            try {
                while (importedKeyMetadataResultSet.next()) {
                    ForeignKey newFk = readForeignKey(importedKeyMetadataResultSet, snapshot);

                    if (newFk != null) {
                        snapshot.addDatabaseObjects(newFk);
                    }
                }
            } finally {
                importedKeyMetadataResultSet.close();
            }

            for (ForeignKey fk : snapshot.getDatabaseObjects(schema, ForeignKey.class)) {

                Table tempPKTable = fk.getPrimaryKeyTable();
                Table pkTable = snapshot.getDatabaseObject(schema, tempPKTable.getName(), Table.class);
                if (pkTable == null) {
                    LogFactory.getLogger().warning("Foreign key " + fk.getName() + " references table " + tempPKTable + ", which is in a different schema. Retaining FK in diff, but table will not be diffed.");
                }

                Table tempFkTable = fk.getForeignKeyTable();
                Table fkTable = snapshot.getDatabaseObject(schema, tempFkTable.getName(), Table.class);
                if (fkTable == null) {
                    LogFactory.getLogger().warning("Foreign key " + fk.getName() + " is in table " + tempFkTable + ", which we cannot find. Ignoring.");
                    continue;
                }

                snapshot.addDatabaseObjects(fk);
            }
        }
    }

    public boolean hasIndex(String catalog, String schemaName, String tableName, String indexName, Database database, String columnNames) throws DatabaseException {
        try {
            if (tableName == null) {
                ResultSet rs = getMetaData(database).getTables(database.correctCatalogName(catalog), database.correctSchemaName(schemaName), null, new String[]{"TABLE"});
                try {
                    while (rs.next()) {
                        if (hasIndex(catalog, schemaName, rs.getString("TABLE_NAME"), indexName, database, columnNames)) {
                            return true;
                        }
                    }
                    return false;
                } finally {
                    rs.close();
                }
            }

            if (indexName != null) {
                ResultSet rs = getMetaData(database).getIndexInfo(database.correctCatalogName(catalog), database.correctSchemaName(schemaName), database.correctTableName(tableName), false, true);
                try {
                    while (rs.next()) {
                        if (rs.getString("INDEX_NAME").equals(database.correctIndexName(indexName))) {
                            return true;
                        }
                    }
                    return false;
                } finally {
                    try {
                        rs.close();
                    } catch (SQLException ignore) {
                    }
                }
            } else if (columnNames != null) {
                columnNames = columnNames.replace(" ", "");
                if (columnNames.contains(",")) {
                    List<String> fixedColumnNames = new ArrayList<String>();
                    for (String columnName : columnNames.split(",")) {
                        fixedColumnNames.add(database.correctColumnName(columnName));
                    }
                    columnNames = StringUtils.join(fixedColumnNames, ",");
                }
                Map<String, TreeMap<Short, String>> columnsByIndexName = new HashMap<String, TreeMap<Short, String>>();
                ResultSet rs = getMetaData(database).getIndexInfo(database.correctCatalogName(catalog), database.correctSchemaName(schemaName), database.correctTableName(tableName), false, true);
                try {
                    while (rs.next()) {
                        String foundIndexName = rs.getString("INDEX_NAME");
                        short ordinalPosition = rs.getShort("ORDINAL_POSITION");

                        if (!columnsByIndexName.containsKey(foundIndexName)) {
                            columnsByIndexName.put(foundIndexName, new TreeMap<Short, String>());
                        }
                        String columnName = database.correctColumnName(rs.getString("COLUMN_NAME"));
                        Map<Short, String> columns = columnsByIndexName.get(foundIndexName);
                        columns.put(ordinalPosition, columnName);
                    }

                    for (TreeMap<Short, String> columnList : columnsByIndexName.values()) {

                        if (StringUtils.join(columnList.values(), ",").equals(columnNames)) {
                            return true;
                        }
                    }
                    return false;
                } finally {
                    rs.close();
                }


            } else {
                throw new UnexpectedLiquibaseException("Either indexName or columnNames must be set");
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public boolean hasForeignKey(String catalogName, String schemaName, String foreignKeyTableName, String fkName, Database database) throws DatabaseException {
        try {
            ResultSet rs = getMetaData(database).getImportedKeys(database.correctCatalogName(catalogName), database.correctSchemaName(schemaName), database.correctTableName(foreignKeyTableName));
            try {
                while (rs.next()) {
                    if (rs.getString("FK_NAME").equals(database.correctForeignKeyName(fkName))) {
                        return true;
                    }
                }
                return false;
            } finally {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

//    /**
//     * It finds <u>only</u> all database-specific Foreign Keys.
//     * By default it returns an empty ArrayList.
//     *
//     * @param schemaName current shemaName
//     * @param database   current database
//     * @return list of database-specific Foreing Keys
//     * @throws liquibase.exception.DatabaseException
//     *          any kinds of SQLException errors
//     */
//    protected List<ForeignKey> getAdditionalForeignKeys(String schemaName, Database database) throws DatabaseException {
//        return new ArrayList<ForeignKey>();
//    }

    protected ForeignKey readForeignKey(ResultSet importedKeyMetadataResultSet, DatabaseSnapshot snapshot) throws DatabaseException, SQLException {
        String fk_name = cleanObjectNameFromDatabase(importedKeyMetadataResultSet.getString("FK_NAME"));
        ForeignKey foreignKey = new ForeignKey();
        foreignKey.setName(fk_name);

        String fkTableCatalog = cleanObjectNameFromDatabase(importedKeyMetadataResultSet.getString("FKTABLE_CAT"));
        String fkTableSchema = cleanObjectNameFromDatabase(importedKeyMetadataResultSet.getString("FKTABLE_SCHEM"));
        String fkTableName = cleanObjectNameFromDatabase(importedKeyMetadataResultSet.getString("FKTABLE_NAME"));
        Table foreignKeyTable = snapshot.getDatabaseObject(new Schema(fkTableCatalog, fkTableSchema), fkTableName, Table.class);
        if (foreignKeyTable == null) { //not in snapshot, probably a different schema
            foreignKeyTable = new Table(fkTableName);
            foreignKeyTable.setSchema(new Schema(new Catalog(fkTableCatalog), fkTableSchema));
        }

        foreignKey.setForeignKeyTable(foreignKeyTable);
        foreignKey.setForeignKeyColumns(cleanObjectNameFromDatabase(importedKeyMetadataResultSet.getString("FKCOLUMN_NAME")));
        foreignKey.setPrimaryKeyTable(snapshot.getDatabaseObject(new Schema(importedKeyMetadataResultSet.getString("PKTABLE_CAT"), importedKeyMetadataResultSet.getString("PKTABLE_SCHEM")), importedKeyMetadataResultSet.getString("PKTABLE_NAME"), Table.class));
        foreignKey.setPrimaryKeyColumns(cleanObjectNameFromDatabase(importedKeyMetadataResultSet.getString("PKCOLUMN_NAME")));
        //todo foreignKey.setKeySeq(importedKeyMetadataResultSet.getInt("KEY_SEQ"));

        ForeignKeyConstraintType updateRule = convertToForeignKeyConstraintType(importedKeyMetadataResultSet.getInt("UPDATE_RULE"));
        if (importedKeyMetadataResultSet.wasNull()) {
            updateRule = null;
        }
        foreignKey.setUpdateRule(updateRule);
        ForeignKeyConstraintType deleteRule = convertToForeignKeyConstraintType(importedKeyMetadataResultSet.getInt("DELETE_RULE"));
        if (importedKeyMetadataResultSet.wasNull()) {
            deleteRule = null;
        }
        foreignKey.setDeleteRule(deleteRule);
        short deferrability = importedKeyMetadataResultSet.getShort("DEFERRABILITY");
        if (deferrability == DatabaseMetaData.importedKeyInitiallyDeferred) {
            foreignKey.setDeferrable(true);
            foreignKey.setInitiallyDeferred(true);
        } else if (deferrability == DatabaseMetaData.importedKeyInitiallyImmediate) {
            foreignKey.setDeferrable(true);
            foreignKey.setInitiallyDeferred(false);
        } else if (deferrability == DatabaseMetaData.importedKeyNotDeferrable) {
            foreignKey.setDeferrable(false);
            foreignKey.setInitiallyDeferred(false);
        } else {
            throw new RuntimeException("Unknown deferrablility result: " + deferrability);
        }

        return foreignKey;
    }


    protected ForeignKeyConstraintType convertToForeignKeyConstraintType(int jdbcType) throws DatabaseException {
        if (jdbcType == DatabaseMetaData.importedKeyCascade) {
            return ForeignKeyConstraintType.importedKeyCascade;
        } else if (jdbcType == DatabaseMetaData.importedKeyNoAction) {
            return ForeignKeyConstraintType.importedKeyNoAction;
        } else if (jdbcType == DatabaseMetaData.importedKeyRestrict) {
            return ForeignKeyConstraintType.importedKeyRestrict;
        } else if (jdbcType == DatabaseMetaData.importedKeySetDefault) {
            return ForeignKeyConstraintType.importedKeySetDefault;
        } else if (jdbcType == DatabaseMetaData.importedKeySetNull) {
            return ForeignKeyConstraintType.importedKeySetNull;
        } else {
            throw new DatabaseException("Unknown constraint type: " + jdbcType);
        }
    }

    protected void readIndexes(DatabaseSnapshot snapshot, Schema schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading indexes for " + database.toString() + " ...");

        for (Table table : snapshot.getDatabaseObjects(schema, Table.class)) {
            ResultSet rs = null;
            Statement statement = null;
            try {
                if (database instanceof OracleDatabase) {
                    //oracle getIndexInfo is buggy and slow.  See Issue 1824548 and http://forums.oracle.com/forums/thread.jspa?messageID=578383&#578383
                    statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
                    String sql = "SELECT INDEX_NAME, 3 AS TYPE, TABLE_NAME, COLUMN_NAME, COLUMN_POSITION AS ORDINAL_POSITION, null AS FILTER_CONDITION FROM ALL_IND_COLUMNS WHERE TABLE_OWNER='" + schema.getName(snapshot.getDatabase()) + "' AND TABLE_NAME='" + table.getName() + "' ORDER BY INDEX_NAME, ORDINAL_POSITION";
                    rs = statement.executeQuery(sql);
                } else {
                    rs = databaseMetaData.getIndexInfo(schema.getCatalogName(snapshot.getDatabase()), schema.getName(snapshot.getDatabase()), table.getName(), false, true);
                }
                Map<String, Index> indexMap = new HashMap<String, Index>();
                while (rs.next()) {
                    String indexName = cleanObjectNameFromDatabase(rs.getString("INDEX_NAME"));
                    /*
                     * TODO Informix generates indexnames with a leading blank if no name given.
                     * An identifier with a leading blank is not allowed.
                     * So here is it replaced.
                     */
                    if (database instanceof InformixDatabase && indexName.startsWith(" ")) {
                        indexName = "_generated_index_" + indexName.substring(1);
                    }
                    short type = rs.getShort("TYPE");
                    //                String tableName = rs.getString("TABLE_NAME");
                    boolean nonUnique = true;
                    try {
                        nonUnique = rs.getBoolean("NON_UNIQUE");
                    } catch (SQLException e) {
                        //doesn't exist in all databases
                    }
                    String columnName = cleanObjectNameFromDatabase(rs.getString("COLUMN_NAME"));
                    short position = rs.getShort("ORDINAL_POSITION");
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
                    String filterCondition = rs.getString("FILTER_CONDITION");

                    if (type == DatabaseMetaData.tableIndexStatistic) {
                        continue;
                    }
                    //                if (type == DatabaseMetaData.tableIndexOther) {
                    //                    continue;
                    //                }

                    if (columnName == null) {
                        //nothing to index, not sure why these come through sometimes
                        continue;
                    }
                    Index indexInformation;
                    if (indexMap.containsKey(indexName)) {
                        indexInformation = indexMap.get(indexName);
                    } else {
                        indexInformation = new Index();
                        indexInformation.setTable(table);
                        indexInformation.setName(indexName);
                        indexInformation.setUnique(!nonUnique);
                        indexInformation.setFilterCondition(filterCondition);
                        indexMap.put(indexName, indexInformation);
                    }

                    for (int i = indexInformation.getColumns().size(); i < position; i++) {
                        indexInformation.getColumns().add(null);
                    }
                    indexInformation.getColumns().set(position - 1, columnName);
                }
                for (Map.Entry<String, Index> entry : indexMap.entrySet()) {
                    snapshot.addDatabaseObjects(entry.getValue());
                }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ignored) {
                    }
                }
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException ignored) {
                    }
                }
            }
        }

        Set<Index> indexesToRemove = new HashSet<Index>();

        /*
          * marks indexes as "associated with" instead of "remove it"
          * Index should have associations with:
          * foreignKey, primaryKey or uniqueConstraint
          * */
        for (Index index : snapshot.getDatabaseObjects(schema, Index.class)) {
            for (PrimaryKey pk : snapshot.getDatabaseObjects(schema, PrimaryKey.class)) {
                if (index.getTable().getName().equalsIgnoreCase(pk.getTable().getName()) && index.getColumnNames().equals(pk.getColumnNames())) {
                    index.addAssociatedWith(Index.MARK_PRIMARY_KEY);
                }
            }
            for (ForeignKey fk : snapshot.getDatabaseObjects(schema, ForeignKey.class)) {
                if (index.getTable().getName().equalsIgnoreCase(fk.getForeignKeyTable().getName()) && index.getColumnNames().equals(fk.getForeignKeyColumns())) {
                    index.addAssociatedWith(Index.MARK_FOREIGN_KEY);
                }
            }
            for (UniqueConstraint uc : snapshot.getDatabaseObjects(schema, UniqueConstraint.class)) {
                if (index.getTable().getName().equalsIgnoreCase(uc.getTable().getName()) && index.getColumnNames().equals(uc.getColumnNames())) {
                    index.addAssociatedWith(Index.MARK_UNIQUE_CONSTRAINT);
                }
            }

        }
        snapshot.removeDatabaseObjects(schema, indexesToRemove.toArray(new Index[indexesToRemove.size()]));
    }

    protected void readPrimaryKeys(DatabaseSnapshot snapshot, Schema schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading primary keys for " + database.toString() + " ...");

        //we can't add directly to the this.primaryKeys hashSet because adding columns to an exising PK changes the hashCode and .contains() fails
        List<PrimaryKey> foundPKs = new ArrayList<PrimaryKey>();

        for (Table table : snapshot.getDatabaseObjects(schema, Table.class)) {
            ResultSet rs = databaseMetaData.getPrimaryKeys(schema.getCatalogName(snapshot.getDatabase()), schema.getName(snapshot.getDatabase()), table.getName());

            try {
                while (rs.next()) {
                    String tableName = cleanObjectNameFromDatabase(rs.getString("TABLE_NAME"));
                    String columnName = cleanObjectNameFromDatabase(rs.getString("COLUMN_NAME"));
                    short position = rs.getShort("KEY_SEQ");

                    boolean foundExistingPK = false;
                    for (PrimaryKey pk : foundPKs) {
                        if (pk.getTable().getName().equals(tableName)) {
                            pk.addColumnName(position - 1, columnName);

                            foundExistingPK = true;
                        }
                    }

                    if (!foundExistingPK) {
                        PrimaryKey primaryKey = new PrimaryKey();
                        primaryKey.setTable(table);
                        primaryKey.addColumnName(position - 1, columnName);
                        primaryKey.setName(database.correctPrimaryKeyName(rs.getString("PK_NAME")));

                        foundPKs.add(primaryKey);
                    }
                }

                //todo set on column object and table object
            } finally {
                rs.close();
            }

        }

        snapshot.addDatabaseObjects(foundPKs.toArray(new PrimaryKey[foundPKs.size()]));
    }

    protected void readUniqueConstraints(DatabaseSnapshot snapshot, Schema schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading unique constraints for " + database.toString() + " ...");
    }

//    private void readUniqueConstraints(String catalog, String schema) throws DatabaseException, SQLException {
//        updateListeners("Reading unique constraints for " + database.toString() + " ...");
//
//        //noinspection unchecked
//        List<String> sequenceNamess = (List<String>) new Executor(database).queryForList(database.findUniqueConstraints(schema), String.class);
//
//        for (String sequenceName : sequenceNamess) {
//            Sequence seq = new Sequence();
//            seq.setName(sequenceName);
//
//            sequences.add(seq);
//        }
//    }

    protected void readSequences(DatabaseSnapshot snapshot, Schema schema, DatabaseMetaData databaseMetaData) throws DatabaseException {
        Database database = snapshot.getDatabase();
        if (database.supportsSequences()) {
            updateListeners("Reading sequences for " + database.toString() + " ...");

            //noinspection unchecked
            List<String> sequenceNames = (List<String>) ExecutorService.getInstance().getExecutor(database).queryForList(new SelectSequencesStatement(schema.getCatalogName(snapshot.getDatabase()), schema.getName(snapshot.getDatabase())), String.class);


            if (sequenceNames != null) {
                for (String sequenceName : sequenceNames) {
                    Sequence seq = new Sequence();
                    seq.setName(sequenceName.trim());
                    seq.setSchema(new Schema(schema.getCatalogName(snapshot.getDatabase()), schema.getName(snapshot.getDatabase())));

                    snapshot.addDatabaseObjects(seq);
                }
            }
        } else {
            updateListeners("Sequences not supported for " + database.toString() + " ...");
        }
    }

    protected void updateListeners(String message) {
        if (this.statusListeners == null) {
            return;
        }
        LogFactory.getLogger().debug(message);
        for (DiffStatusListener listener : this.statusListeners) {
            listener.statusUpdate(message);
        }
    }

    public boolean isColumnAutoIncrement(Database database, String catalogName, String schemaName, String tableName, String columnName) throws SQLException, DatabaseException {
        if (!database.supportsAutoIncrement()) {
            return false;
        }

        boolean autoIncrement = false;

        Statement statement = null;
        ResultSet selectRS = null;
        try {
            statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
            selectRS = statement.executeQuery("SELECT " + database.escapeColumnName(catalogName, schemaName, tableName, columnName) + " FROM " + database.escapeTableName(catalogName, schemaName, tableName) + " WHERE 1 = 0");
            ResultSetMetaData meta = selectRS.getMetaData();
            autoIncrement = meta.isAutoIncrement(1);
        } finally {
            if (selectRS != null) {
                try {
                    selectRS.close();
                } catch (SQLException ignored) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {
                }
            }
        }

        return autoIncrement;
    }

    public DateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    public DateFormat getTimeFormat() {
        return new SimpleDateFormat("HH:mm:SS");
    }

    public DateFormat getDateTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:SS.S");
    }
}
