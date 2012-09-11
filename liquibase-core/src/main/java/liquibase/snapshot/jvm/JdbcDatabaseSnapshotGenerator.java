package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.structure.core.*;
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
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class JdbcDatabaseSnapshotGenerator implements DatabaseSnapshotGenerator {

    private Set<DiffStatusListener> statusListeners;

    public Table getDatabaseChangeLogTable(Database database) throws DatabaseException {
        return getTable(database.correctSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database.getDatabaseChangeLogTableName(), database);
    }

    public Table getDatabaseChangeLogLockTable(Database database) throws DatabaseException {
        return getTable(database.correctSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database.getDatabaseChangeLogLockTableName(), database);
    }

    public boolean hasDatabaseChangeLogTable(Database database) {
        return hasTable(database.correctSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database.getDatabaseChangeLogTableName(), database);
    }

    public boolean hasDatabaseChangeLogLockTable(Database database) {
        return hasTable(database.correctSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database.getDatabaseChangeLogLockTableName(), database);
    }

    public boolean hasTable(Schema schema, String tableName, Database database) {
        if (schema == null) {
            schema = Schema.DEFAULT;
        }
        try {
            if (database != null) {
                tableName = database.correctObjectName(tableName, Table.class);
                schema = database.correctSchema(schema);
            }
            ResultSet rs = getMetaData(database).getTables(getJdbcCatalogName(schema), getJdbcSchemaName(schema), tableName, new String[]{"TABLE"});
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

    public boolean hasView(Schema schema, String viewName, Database database) {
        try {
            ResultSet rs = getMetaData(database).getTables(getJdbcCatalogName(schema), getJdbcSchemaName(schema), database.correctObjectName(viewName, View.class), new String[]{"VIEW"});
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

    public Table getTable(Schema schema, String tableName, Database database) throws DatabaseException {
        ResultSet rs = null;
        try {
            DatabaseMetaData metaData = getMetaData(database);
            rs = metaData.getTables(getJdbcCatalogName(schema), getJdbcSchemaName(schema), database.correctObjectName(tableName, Table.class), new String[]{"TABLE"});

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

    public boolean hasColumn(Schema schema, String tableName, String columnName, Database database) {
        try {
            return getColumn(schema, tableName, columnName, database) != null;
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

    public Column getColumn(Schema schema, String tableName, String columnName, Database database) throws DatabaseException {
        ResultSet rs = null;
        try {
            rs = getMetaData(database).getColumns(getJdbcCatalogName(schema), getJdbcSchemaName(schema), database.correctObjectName(tableName, Table.class), database.correctObjectName(columnName, Column.class));

            if (!rs.next()) {
                return null;
            }

            Table table = new Table(database.correctObjectName(tableName, Table.class));
            table.setSchema(schema);
            return readColumn(convertResultSetToMap(rs), table, database);
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

        table.setSchema(getSchemaFromJdbcInfo(rawSchemaName, rawCatalogName, database));

        if (readColumns) {
            Schema rawSchema = database.correctSchema(new Schema(table.getRawCatalogName(), table.getRawSchemaName()));
            ResultSet columnMetadataResultSet = getMetaData(database).getColumns(getJdbcCatalogName(rawSchema), getJdbcSchemaName(rawSchema), rawTableName, null);
            try {
                while (columnMetadataResultSet.next()) {
                    table.getColumns().add(readColumn(convertResultSetToMap(columnMetadataResultSet), table, database));
                }
            } finally {
                columnMetadataResultSet.close();
            }
        }

        return table;
    }

    protected Schema getSchemaFromJdbcInfo(String rawSchemaName, String rawCatalogName, Database database) {
        return database.correctSchema(new Schema(rawCatalogName, rawSchemaName));
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

        view.setSchema(getSchemaFromJdbcInfo(rawSchemaName, rawCatalogName, database));

        try {
            view.setDefinition(database.getViewDefinition(view.getSchema(), view.getName()));
        } catch (DatabaseException e) {
            throw new DatabaseException("Error getting " + database.getConnection().getURL() + " view with " + new GetViewDefinitionStatement(view.getSchema().getCatalog().getName(), view.getSchema().getName(), rawViewName), e);
        }

        return view;
    }

    protected Column readColumn(Map<String, Object> columnMetadataResultSet, Relation table, Database database) throws SQLException, DatabaseException {
        String rawTableName = (String) columnMetadataResultSet.get("TABLE_NAME");
        String rawColumnName = (String) columnMetadataResultSet.get("COLUMN_NAME");
        String rawSchemaName = StringUtils.trimToNull((String) columnMetadataResultSet.get("TABLE_SCHEM"));
        String rawCatalogName = StringUtils.trimToNull((String) columnMetadataResultSet.get("TABLE_CAT"));
        String remarks = StringUtils.trimToNull((String) columnMetadataResultSet.get("REMARKS"));

        Schema schema = new Schema(rawCatalogName, rawSchemaName);
        if (database.isSystemTable(schema, rawTableName) || database.isSystemView(schema, rawTableName)) {
            return null;
        }

        Column column = new Column();
        column.setName(rawColumnName);
        column.setRelation(table);
        column.setRemarks(remarks);

        int nullable = (Integer) columnMetadataResultSet.get("NULLABLE");
        if (nullable == DatabaseMetaData.columnNoNulls) {
            column.setNullable(false);
        } else if (nullable == DatabaseMetaData.columnNullable) {
            column.setNullable(true);
        } else if (nullable == DatabaseMetaData.columnNullableUnknown) {
            LogFactory.getLogger().info("Unknown nullable state for column " + column.toString() + ". Assuming nullable");
            column.setNullable(true);
        }

        if (table instanceof Table) {
            if (columnMetadataResultSet.containsKey("IS_AUTOINCREMENT")) {
                String isAutoincrement = (String) columnMetadataResultSet.get("IS_AUTOINCREMENT");
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
            } else {
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
        }

        DataType type = readDataType(columnMetadataResultSet, column, database);
        column.setType(type);

        column.setDefaultValue(readDefaultValue(columnMetadataResultSet, column, database));

        return column;
    }

    protected DataType readDataType(Map<String, Object> columnMetadataResultSet, Column column, Database database) throws SQLException {
        String columnTypeName = (String) columnMetadataResultSet.get("TYPE_NAME");

        int dataType = (Integer) columnMetadataResultSet.get("DATA_TYPE");
        Integer columnSize = (Integer) columnMetadataResultSet.get("COLUMN_SIZE");

        Integer decimalDigits = (Integer) columnMetadataResultSet.get("DECIMAL_DIGITS");
        if (decimalDigits != null && decimalDigits.equals(0)) {
            decimalDigits = null;
        }

        Integer radix = (Integer) columnMetadataResultSet.get("NUM_PREC_RADIX");

        Integer characterOctetLength = (Integer) columnMetadataResultSet.get("CHAR_OCTET_LENGTH");

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
            DatabaseSnapshot snapshot = new DatabaseSnapshot(database, diffControl.getSchemas(type));
            DatabaseMetaData databaseMetaData = getMetaData(database);
            this.statusListeners = diffControl.getStatusListeners();


            for (Schema schema : diffControl.getSchemas(type)) {
                schema = snapshot.getDatabase().correctSchema(schema);
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
        schema = snapshot.getDatabase().correctSchema(schema);
        Database database = snapshot.getDatabase();
        updateListeners("Reading tables for " + database.toString() + " ...");

        ResultSet tableMetaDataRs = databaseMetaData.getTables(getJdbcCatalogName(schema), getJdbcSchemaName(schema), null, new String[]{"TABLE", "ALIAS"});
        try {
            while (tableMetaDataRs.next()) {
                Table table = readTable(tableMetaDataRs, database, false);
                if (database.isLiquibaseTable(schema, table.getName())) {
                    if (table.equals(database.getDatabaseChangeLogTableName(), database)) {
                        snapshot.setDatabaseChangeLogTable(table);
                        continue;
                    }

                    if (table.equals(database.getDatabaseChangeLogLockTableName(), database)) {
                        snapshot.setDatabaseChangeLogLockTable(table);
                        continue;
                    }
                }
                if (database.isSystemTable(table.getSchema(), table.getName()) || database.isLiquibaseTable(table.getSchema(), table.getName())) {
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

        ResultSet viewsMetadataRs = databaseMetaData.getTables(getJdbcCatalogName(schema), getJdbcSchemaName(schema), null, new String[]{"VIEW"});
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
            allColumnsMetadataRs = databaseMetaData.getColumns(getJdbcCatalogName(schema), getJdbcSchemaName(schema), null, null);
            while (allColumnsMetadataRs.next()) {
                Map<String, Object> data = convertResultSetToMap(allColumnsMetadataRs);
                String tableOrViewName = cleanObjectNameFromDatabase((String) data.get("TABLE_NAME"));
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

                Column column = readColumn(data, relation, database);

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

    private Map<String, Object> convertResultSetToMap(ResultSet rs) throws SQLException {
        Class[] types = new Class[]{ //matches tableMetadata.getColumns() types. Ugly, but otherwise get wrong types
                null, //no zero index
                String.class,
                String.class,
                String.class,
                String.class,
                int.class,
                String.class,
                int.class,
                String.class,
                int.class,
                int.class,
                int.class,
                String.class,
                String.class,
                int.class,
                int.class,
                int.class,
                int.class,
                String.class,
                String.class,
                String.class,
                String.class,
                short.class,
                String.class,
                String.class
        };

        Map<String, Object> data = new HashMap<String, Object>();
        for (int i=1; i<= rs.getMetaData().getColumnCount(); i++) {
            Class classType = types[i];
            Object value;
            if (classType.equals(String.class)) {
                value = rs.getString(i);
            } else if (classType.equals(int.class)) {
                value = rs.getInt(i);
            }  else if (classType.equals(short.class)) {
                value = rs.getShort(i);
            } else {
                value = rs.getObject(i);
            }
            if (rs.wasNull()) {
                value = null;
            }
            data.put(rs.getMetaData().getColumnName(i), value);
        }
        return data;
    }

    protected Object readDefaultValue(Map<String, Object> columnMetadataResultSet, Column columnInfo, Database database) throws SQLException, DatabaseException {
        Object val = columnMetadataResultSet.get("COLUMN_DEF");

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
                    return new BigInteger(stringVal.trim());
                } else if (type == Types.BINARY) {
                    return new DatabaseFunction(stringVal.trim());
                } else if (type == Types.BIT) {
                    if (stringVal.startsWith("b'")) { //mysql returns boolean values as b'0' and b'1'
                        stringVal = stringVal.replaceFirst("b'", "").replaceFirst("'$", "");
                    }
                    return new Integer(stringVal.trim());
                } else if (type == Types.BLOB) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.BOOLEAN) {
                    return Boolean.valueOf(stringVal.trim());
                } else if (type == Types.CHAR) {
                    return stringVal;
                } else if (type == Types.DATALINK) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.DATE) {
                    return new java.sql.Date(getDateFormat().parse(stringVal.trim()).getTime());
                } else if (type == Types.DECIMAL) {
                    return new BigDecimal(stringVal.trim());
                } else if (type == Types.DISTINCT) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.DOUBLE) {
                    return Double.valueOf(stringVal.trim());
                } else if (type == Types.FLOAT) {
                    return Float.valueOf(stringVal.trim());
                } else if (type == Types.INTEGER) {
                    return Integer.valueOf(stringVal.trim());
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
                    return new BigDecimal(stringVal.trim());
                } else if (type == Types.NVARCHAR) {
                    return stringVal;
                } else if (type == Types.OTHER) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.REAL) {
                    return new BigDecimal(stringVal.trim());
                } else if (type == Types.REF) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.ROWID) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.SMALLINT) {
                    return Integer.valueOf(stringVal.trim());
                } else if (type == Types.SQLXML) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.STRUCT) {
                    return new DatabaseFunction(stringVal);
                } else if (type == Types.TIME) {
                    return new java.sql.Time(getTimeFormat().parse(stringVal).getTime());
                } else if (type == Types.TIMESTAMP) {
                    return new Timestamp(getDateTimeFormat().parse(stringVal).getTime());
                } else if (type == Types.TINYINT) {
                    return Integer.valueOf(stringVal.trim());
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
            Schema rawSchema = getSchemaFromJdbcInfo(table.getRawSchemaName(), table.getRawCatalogName(), database);
            ResultSet importedKeyMetadataResultSet = getMetaData(database).getImportedKeys(getJdbcCatalogName(rawSchema), getJdbcSchemaName(rawSchema), table.getName());

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

    public boolean hasIndex(Schema schema, String tableName, String indexName, String columnNames, Database database) throws DatabaseException {
        try {
            if (tableName == null) {
                ResultSet rs = getMetaData(database).getTables(getJdbcCatalogName(schema), getJdbcSchemaName(schema), null, new String[]{"TABLE"});
                try {
                    while (rs.next()) {
                        String foundTable = rs.getString("TABLE_NAME");
                        if (!database.isSystemTable(schema, foundTable) && !database.isSystemView(schema, foundTable)) {
                            if (hasIndex(schema, foundTable, indexName, columnNames, database)) {
                                return true;
                            }
                        }
                    }
                    return false;
                } finally {
                    rs.close();
                }
            }

            Index index = new Index();
            index.setTable((Table) new Table(tableName).setSchema(schema));
            index.setName(indexName);
            if (columnNames != null) {
                for (String column : columnNames.split("\\s*,\\s*")) {
                    index.getColumns().add(column);
                }
            }

            if (columnNames != null) {
                Map<String, TreeMap<Short, String>> columnsByIndexName = new HashMap<String, TreeMap<Short, String>>();
                ResultSet rs = getMetaData(database).getIndexInfo(getJdbcCatalogName(schema), getJdbcSchemaName(schema), database.correctObjectName(tableName, Table.class), false, true);
                try {
                    while (rs.next()) {
                        String foundIndexName = rs.getString("INDEX_NAME");
                        if (indexName != null && indexName.equalsIgnoreCase(foundIndexName)) { //ok to use equalsIgnoreCase because we will check case later
                            continue;
                        }
                        short ordinalPosition = rs.getShort("ORDINAL_POSITION");

                        if (!columnsByIndexName.containsKey(foundIndexName)) {
                            columnsByIndexName.put(foundIndexName, new TreeMap<Short, String>());
                        }
                        String columnName = rs.getString("COLUMN_NAME");
                        Map<Short, String> columns = columnsByIndexName.get(foundIndexName);
                        columns.put(ordinalPosition, columnName);
                    }

                    for (Map.Entry<String, TreeMap<Short, String>> foundIndexData : columnsByIndexName.entrySet()) {
                        Index foundIndex =  new Index()
                                .setName(foundIndexData.getKey())
                                .setTable(((Table) new Table(tableName).setSchema(schema)));
                        foundIndex.getColumns().addAll(foundIndexData.getValue().values());

                        if (foundIndex.equals(index, database)) {
                            return true;
                        }
                        return false;
                    }
                    return false;
                } finally {
                    rs.close();
                }
            } else if (indexName != null) {
                    ResultSet rs = getMetaData(database).getIndexInfo(getJdbcCatalogName(schema), getJdbcSchemaName(schema), database.correctObjectName(tableName, Table.class), false, true);
                    try {
                        while (rs.next()) {
                            Index foundIndex =  new Index()
                                    .setName(rs.getString("INDEX_NAME"))
                                    .setTable(((Table) new Table(tableName).setSchema(schema)));
                            if (foundIndex.getName() == null) {
                                continue;
                            }
                            if (foundIndex.equals(index, database)) {
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
            } else {
                throw new UnexpectedLiquibaseException("Either indexName or columnNames must be set");
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public boolean hasForeignKey(Schema schema, String foreignKeyTableName, String fkName, Database database) throws DatabaseException {
        ForeignKey fk = new ForeignKey().setName(fkName);
        try {
            ResultSet rs = getMetaData(database).getImportedKeys(getJdbcCatalogName(schema), getJdbcSchemaName(schema), database.correctObjectName(foreignKeyTableName, ForeignKey.class));
            try {
                while (rs.next()) {
                    ForeignKey foundFk = new ForeignKey().setName(rs.getString("FK_NAME"));
                    if (fk.equals(foundFk, database)) {
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

        Table tempPkTable = (Table) new Table(importedKeyMetadataResultSet.getString("PKTABLE_NAME")).setSchema(snapshot.getDatabase().correctSchema(new Schema(importedKeyMetadataResultSet.getString("PKTABLE_CAT"), importedKeyMetadataResultSet.getString("PKTABLE_SCHEM"))));
        foreignKey.setPrimaryKeyTable(tempPkTable);
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
                    String sql = "SELECT INDEX_NAME, 3 AS TYPE, TABLE_NAME, COLUMN_NAME, COLUMN_POSITION AS ORDINAL_POSITION, null AS FILTER_CONDITION FROM ALL_IND_COLUMNS WHERE TABLE_OWNER='" + schema.getName() + "' AND TABLE_NAME='" + table.getName() + "' ORDER BY INDEX_NAME, ORDINAL_POSITION";
                    rs = statement.executeQuery(sql);
                } else {
                    rs = databaseMetaData.getIndexInfo(getJdbcCatalogName(schema), getJdbcSchemaName(schema), table.getName(), false, true);
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
                        if (!includeInSnapshot(indexInformation)) {
                            continue;
                        }
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
                if (index.getTable().equals(pk.getTable().getName(), database) && columnNamesAreEqual(index.getColumnNames(), pk.getColumnNames(), database)) {
                    index.addAssociatedWith(Index.MARK_PRIMARY_KEY);
                }
            }
            for (ForeignKey fk : snapshot.getDatabaseObjects(schema, ForeignKey.class)) {
                if (index.getTable().equals(fk.getForeignKeyTable().getName(), database) && columnNamesAreEqual(index.getColumnNames(), fk.getForeignKeyColumns(), database)) {
                    index.addAssociatedWith(Index.MARK_FOREIGN_KEY);
                }
            }
            for (UniqueConstraint uc : snapshot.getDatabaseObjects(schema, UniqueConstraint.class)) {
                if (index.getTable().equals(uc.getTable()) && columnNamesAreEqual(index.getColumnNames(), uc.getColumnNames(), database)) {
                    index.addAssociatedWith(Index.MARK_UNIQUE_CONSTRAINT);
                }
            }

        }
        snapshot.removeDatabaseObjects(schema, indexesToRemove.toArray(new Index[indexesToRemove.size()]));
    }

    protected boolean columnNamesAreEqual(String columnNames, String otherColumnNames, Database database) {
        if (database.isCaseSensitive()) {
            return columnNames.replace(" ", "").equals(otherColumnNames.replace(" ", ""));
        } else {
            return columnNames.replace(" ", "").equalsIgnoreCase(otherColumnNames.replace(" ", ""));
        }
    }

    protected boolean includeInSnapshot(DatabaseObject obj) {
        return true;
    }

    protected void readPrimaryKeys(DatabaseSnapshot snapshot, Schema schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading primary keys for " + database.toString() + " ...");

        //we can't add directly to the this.primaryKeys hashSet because adding columns to an exising PK changes the hashCode and .contains() fails
        List<PrimaryKey> foundPKs = new ArrayList<PrimaryKey>();

        for (Table table : snapshot.getDatabaseObjects(schema, Table.class)) {
            ResultSet rs = databaseMetaData.getPrimaryKeys(getJdbcCatalogName(schema), getJdbcSchemaName(schema), table.getName());

            try {
                while (rs.next()) {
                    String tableName = cleanObjectNameFromDatabase(rs.getString("TABLE_NAME"));
                    String columnName = cleanObjectNameFromDatabase(rs.getString("COLUMN_NAME"));
                    short position = rs.getShort("KEY_SEQ");

                    if (database.isLiquibaseTable(schema, tableName)) {
                        continue;
                    }

                    boolean foundExistingPK = false;
                    for (PrimaryKey pk : foundPKs) {
                        if (pk.getTable().equals(tableName, database)) {
                            pk.addColumnName(position - 1, columnName);

                            foundExistingPK = true;
                        }
                    }

                    if (!foundExistingPK) {
                        PrimaryKey primaryKey = new PrimaryKey();
                        primaryKey.setTable(table);
                        primaryKey.addColumnName(position - 1, columnName);
                        primaryKey.setName(database.correctObjectName(rs.getString("PK_NAME"), PrimaryKey.class));

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
            List<String> sequenceNames = (List<String>) ExecutorService.getInstance().getExecutor(database).queryForList(new SelectSequencesStatement(schema.getCatalogName(), schema.getName()), String.class);


            if (sequenceNames != null) {
                for (String sequenceName : sequenceNames) {
                    Sequence seq = new Sequence();
                    seq.setName(sequenceName.trim());
                    seq.setSchema(new Schema(schema.getCatalogName(), schema.getName()));

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

    public boolean isColumnAutoIncrement(Database database, Schema schema, String tableName, String columnName) throws SQLException, DatabaseException {
        if (!database.supportsAutoIncrement()) {
            return false;
        }

        boolean autoIncrement = false;

        Statement statement = null;
        ResultSet selectRS = null;
        try {
            statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
            selectRS = statement.executeQuery("SELECT " + database.escapeColumnName(schema.getCatalogName(), schema.getName(), tableName, columnName) + " FROM " + database.escapeTableName(schema.getCatalogName(), schema.getName(), tableName) + " WHERE 1 = 0");
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

    protected String getJdbcCatalogName(Schema schema) {
        return schema.getCatalogName();
    }

    protected String getJdbcSchemaName(Schema schema) {
        return schema.getName();
    }
}
