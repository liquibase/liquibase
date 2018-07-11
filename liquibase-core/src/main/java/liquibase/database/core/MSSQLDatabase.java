package liquibase.database.core;

import java.math.BigInteger;

import liquibase.CatalogAndSchema;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.util.JdbcUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import liquibase.logging.LogFactory;

/**
 * Encapsulates MS-SQL database support.
 */
public class MSSQLDatabase extends AbstractJdbcDatabase {
    public static final String PRODUCT_NAME = "Microsoft SQL Server";
    protected Set<String> systemTablesAndViews = new HashSet<String>();

    private static Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("(?im)^\\s*(CREATE|ALTER)\\s+VIEW\\s+(\\S+)\\s+?AS\\s*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private Boolean sendsStringParametersAsUnicode = null;

    @Override
    public String getShortName() {
        return "mssql";
    }

    public MSSQLDatabase() {
        super.setCurrentDateTimeFunction("GETDATE()");

        super.sequenceNextValueFunction = "NEXT VALUE FOR %s";

        systemTablesAndViews.add("syscolumns");
        systemTablesAndViews.add("syscomments");
        systemTablesAndViews.add("sysdepends");
        systemTablesAndViews.add("sysfilegroups");
        systemTablesAndViews.add("sysfiles");
        systemTablesAndViews.add("sysfiles1");
        systemTablesAndViews.add("sysforeignkeys");
        systemTablesAndViews.add("sysfulltextcatalogs");
        systemTablesAndViews.add("sysfulltextnotify");
        systemTablesAndViews.add("sysindexes");
        systemTablesAndViews.add("sysindexkeys");
        systemTablesAndViews.add("sysmembers");
        systemTablesAndViews.add("sysobjects");
        systemTablesAndViews.add("syspermissions");
        systemTablesAndViews.add("sysproperties");
        systemTablesAndViews.add("sysprotects");
        systemTablesAndViews.add("sysreferences");
        systemTablesAndViews.add("systypes");
        systemTablesAndViews.add("sysusers");
        systemTablesAndViews.add("sysdiagrams");

        systemTablesAndViews.add("syssegments");
        systemTablesAndViews.add("sysconstraints");

        super.quotingStartCharacter = "[";
        super.quotingEndCharacter = "]";
        super.quotingEndReplacement = "]]";
    }


    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "SQL Server";
    }

    @Override
    public Integer getDefaultPort() {
        return 1433;
    }

    @Override
    public Set<String> getSystemViews() {
        return systemTablesAndViews;
    }

    @Override
    protected Set<String> getSystemTables() {
        return systemTablesAndViews;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        try {
            if (isAzureDb()) {
                return false;
            }
            if (this.getDatabaseMajorVersion() >= 11) {
                return true;
            }
        } catch (DatabaseException e) {
            return false;
        }
        return false;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        String databaseProductName = conn.getDatabaseProductName();
        return PRODUCT_NAME.equalsIgnoreCase(databaseProductName)
                || "SQLOLEDB".equalsIgnoreCase(databaseProductName);
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sqlserver")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (url.startsWith("jdbc:jtds:sqlserver")) {
            return "net.sourceforge.jtds.jdbc.Driver";
        }
        return null;
    }

    @Override
    protected String getAutoIncrementClause() {
    	return "IDENTITY";
    }
    
    @Override
    protected boolean generateAutoIncrementStartWith(BigInteger startWith) {
        return true;
    }

    @Override
    protected boolean generateAutoIncrementBy(BigInteger incrementBy) {
        return true;
    }

    @Override
    protected String getAutoIncrementStartWithClause() {
    	return "%d";
    }

    @Override
    protected String getAutoIncrementByClause() {
    	return "%d";
    }

    @Override
    public String getDefaultCatalogName() {
        if (getConnection() == null) {
            return null;
        }
        try {
            return getConnection().getCatalog();
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    protected SqlStatement getConnectionSchemaNameCallStatement() {
        return new RawSqlStatement("select schema_name()");
    }

    @Override
    public String getConcatSql(String... values) {
        StringBuffer returnString = new StringBuffer();
        for (String value : values) {
            returnString.append(value).append(" + ");
        }

        return returnString.toString().replaceFirst(" \\+ $", "");
    }

    @Override
    public String escapeTableName(String catalogName, String schemaName, String tableName) {
        return escapeObjectName(catalogName, schemaName, tableName, Table.class);
    }

    //    protected void dropForeignKeys(Connection conn) throws DatabaseException {
//        Statement dropStatement = null;
//        PreparedStatement fkStatement = null;
//        ResultSet rs = null;
//        try {
//            dropStatement = conn.createStatement();
//
//            fkStatement = conn.prepareStatement("select TABLE_NAME, CONSTRAINT_NAME from INFORMATION_SCHEMA.TABLE_CONSTRAINTS where CONSTRAINT_TYPE='FOREIGN KEY' AND TABLE_CATALOG=?");
//            fkStatement.setString(1, getDefaultCatalogName());
//            rs = fkStatement.executeQuery();
//            while (rs.next()) {
//                DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
//                dropFK.setBaseTableName(rs.getString("TABLE_NAME"));
//                dropFK.setConstraintName(rs.getString("CONSTRAINT_NAME"));
//
//                try {
//                    dropStatement.execute(dropFK.generateStatements(this)[0]);
//                } catch (UnsupportedChangeException e) {
//                    throw new DatabaseException(e.getMessage());
//                }
//            }
//        } catch (SQLException e) {
//            throw new DatabaseException(e);
//        } finally {
//            try {
//                if (dropStatement != null) {
//                    dropStatement.close();
//                }
//                if (fkStatement != null) {
//                    fkStatement.close();
//                }
//                if (rs != null) {
//                    rs.close();
//                }
//            } catch (SQLException e) {
//                throw new DatabaseException(e);
//            }
//        }
//
//    }

    @Override
    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        if (example.getSchema() == null || example.getSchema().getName() == null) {
            return super.isSystemObject(example);
        }

        if (example instanceof Table && example.getSchema().getName().equals("sys")) {
            return true;
        }
        if (example instanceof View && example.getSchema().getName().equals("sys")) {
            return true;
        }
        return super.isSystemObject(example);
    }

    public String generateDefaultConstraintName(String tableName, String columnName) {
        return "DF_" + tableName + "_" + columnName;
    }


    @Override
    public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectName == null) {
            return null;
        }

        if (objectName.contains("(")) { //probably a function
            return objectName;
        }

        return quoteObject(objectName, objectType);
    }

    @Override
    public String getDateLiteral(String isoDate) {
        return super.getDateLiteral(isoDate).replace(' ', 'T');
    }

	@Override
    public boolean supportsRestrictForeignKeys() {
        return false;
    }

    @Override
    public boolean supportsDropTableCascadeConstraints() {
        return false;
    }

    @Override
    public boolean supportsCatalogInObjectName(Class<? extends DatabaseObject> type) {
        return Relation.class.isAssignableFrom(type);
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
          schema = schema.customize(this);
        List<String> defLines = (List<String>) ExecutorService.getInstance().getExecutor(this).queryForList(new GetViewDefinitionStatement(schema.getCatalogName(), schema.getSchemaName(), viewName), String.class);
        StringBuffer sb = new StringBuffer();
        for (String defLine : defLines) {
            sb.append(defLine);
        }
        String definition = sb.toString();

        String finalDef =definition.replaceAll("\\r\\n", "\n").trim();

        if (finalDef.startsWith("--") || finalDef.startsWith("/*")) { //keep comment as beginning of statement
            return "FULL_DEFINITION: " + finalDef;
        }

        String selectOnly = CREATE_VIEW_AS_PATTERN.matcher(finalDef).replaceFirst("");
        if (selectOnly.equals(finalDef)) {
            return "FULL_DEFINITION: " + finalDef;
        }

        selectOnly = selectOnly.trim();


        /**handle views that end up as '(select XYZ FROM ABC);' */
        if (selectOnly.startsWith("(") && (selectOnly.endsWith(")") || selectOnly.endsWith(");"))) {
            selectOnly = selectOnly.replaceFirst("^\\(", "");
            selectOnly = selectOnly.replaceFirst("\\);?$", "");
        }

        return selectOnly;
    }

    @Override
    public String escapeObjectName(String catalogName, String schemaName, String objectName, Class<? extends DatabaseObject> objectType) {
        if (View.class.isAssignableFrom(objectType)) { //SQLServer does not support specifying the database name as a prefix to the object name
            String name = this.escapeObjectName(objectName, objectType);
            if (schemaName != null) {
                name = this.escapeObjectName(schemaName, Schema.class)+"."+name;
            }
            return name;
        } else if (Index.class.isAssignableFrom(objectType)) {
            return super.escapeObjectName(objectName, objectType);
        }

        boolean includeCatalog = LiquibaseConfiguration.getInstance().shouldIncludeCatalogInSpecification();
        if (catalogName != null && (includeCatalog || !catalogName.equalsIgnoreCase(this.getDefaultCatalogName()))) {
            return super.escapeObjectName(catalogName, schemaName, objectName, objectType);
        } else {
            String name = this.escapeObjectName(objectName, objectType);
            if (schemaName == null) {
                schemaName = this.getDefaultSchemaName();
            }
            if (schemaName != null) {
                name = this.escapeObjectName(schemaName, Schema.class)+"."+name;
            }
            return name;
        }
    }

    @Override
    public String getJdbcSchemaName(CatalogAndSchema schema) {
        String schemaName = super.getJdbcSchemaName(schema);
        if (schemaName != null && !isCaseSensitive()) {
            schemaName = schemaName.toLowerCase();
        }
        return schemaName;
    }

    @Override
    public boolean isCaseSensitive() {
        if (caseSensitive == null) {
            try {
                if (getConnection() instanceof JdbcConnection) {
                    String catalog = getConnection().getCatalog();
                    String sql = "SELECT CONVERT([sysname], DATABASEPROPERTYEX(N'" + escapeStringForDatabase(catalog) + "', 'Collation'))";
                    String collation = ExecutorService.getInstance().getExecutor(this).queryForObject(new RawSqlStatement(sql), String.class);
                    caseSensitive = collation != null && !collation.contains("_CI_");
                } else if (getConnection() instanceof OfflineConnection) {
                    caseSensitive = ((OfflineConnection) getConnection()).isCaseSensitive();
                }
            } catch (Exception e) {
                LogFactory.getLogger().warning("Cannot determine case sensitivity from MSSQL", e);
            }
        }
        return caseSensitive != null && caseSensitive;
    }

    @Override
    public int getDataTypeMaxParameters(String dataTypeName) {
        if ("bigint".equalsIgnoreCase(dataTypeName)
                || "bit".equalsIgnoreCase(dataTypeName)
                || "date".equalsIgnoreCase(dataTypeName)
                || "datetime".equalsIgnoreCase(dataTypeName)
                || "geography".equalsIgnoreCase(dataTypeName)
                || "geometry".equalsIgnoreCase(dataTypeName)
                || "hierarchyid".equalsIgnoreCase(dataTypeName)
                || "image".equalsIgnoreCase(dataTypeName)
                || "int".equalsIgnoreCase(dataTypeName)
                || "money".equalsIgnoreCase(dataTypeName)
                || "ntext".equalsIgnoreCase(dataTypeName)
                || "real".equalsIgnoreCase(dataTypeName)
                || "smalldatetime".equalsIgnoreCase(dataTypeName)
                || "smallint".equalsIgnoreCase(dataTypeName)
                || "smallmoney".equalsIgnoreCase(dataTypeName)
                || "text".equalsIgnoreCase(dataTypeName)
                || "timestamp".equalsIgnoreCase(dataTypeName)
                || "tinyint".equalsIgnoreCase(dataTypeName)
                || "rowversion".equalsIgnoreCase(dataTypeName)
                || "sql_variant".equalsIgnoreCase(dataTypeName)
                || "sysname".equalsIgnoreCase(dataTypeName)
                || "uniqueidentifier".equalsIgnoreCase(dataTypeName)) {

            return 0;
        }

        if ("binary".equalsIgnoreCase(dataTypeName)
                || "char".equalsIgnoreCase(dataTypeName)
                || "datetime2".equalsIgnoreCase(dataTypeName)
                || "datetimeoffset".equalsIgnoreCase(dataTypeName)
                || "float".equalsIgnoreCase(dataTypeName)
                || "nchar".equalsIgnoreCase(dataTypeName)
                || "nvarchar".equalsIgnoreCase(dataTypeName)
                || "time".equalsIgnoreCase(dataTypeName)
                || "varbinary".equalsIgnoreCase(dataTypeName)
                || "varchar".equalsIgnoreCase(dataTypeName)
                || "xml".equalsIgnoreCase(dataTypeName)) {

            return 1;
        }

        return 2;
    }

    @Override
    public String escapeDataTypeName(String dataTypeName) {
        int indexOfPeriod = dataTypeName.indexOf('.');

        if (indexOfPeriod < 0) {
            if (!dataTypeName.startsWith(quotingStartCharacter)) {
                dataTypeName = escapeObjectName(dataTypeName, DatabaseObject.class);
            }

            return dataTypeName;
        }

        String schemaName = dataTypeName.substring(0, indexOfPeriod);
        if (!schemaName.startsWith(quotingStartCharacter)) {
            schemaName = escapeObjectName(schemaName, Schema.class);
        }

        dataTypeName = dataTypeName.substring(indexOfPeriod + 1, dataTypeName.length());
        if (!dataTypeName.startsWith(quotingStartCharacter)) {
            dataTypeName = escapeObjectName(dataTypeName, DatabaseObject.class);
        }

        return schemaName + "." + dataTypeName;
    }

    @Override
    public String unescapeDataTypeName(String dataTypeName) {
         int indexOfPeriod = dataTypeName.indexOf('.');

         if (indexOfPeriod < 0) {
             if (dataTypeName.matches("\\[[^]\\[]++\\]")) {
                 dataTypeName = dataTypeName.substring(1, dataTypeName.length() - 1);
             }

             return dataTypeName;
         }

         String schemaName = dataTypeName.substring(0, indexOfPeriod);
         if (schemaName.matches("\\[[^]\\[]++\\]")) {
             schemaName = schemaName.substring(1, schemaName.length() - 1);
         }

         dataTypeName = dataTypeName.substring(indexOfPeriod + 1, dataTypeName.length());
         if (dataTypeName.matches("\\[[^]\\[]++\\]")) {
             dataTypeName = dataTypeName.substring(1, dataTypeName.length() - 1);
         }

         return schemaName + "." + dataTypeName;
    }

    @Override
    public String unescapeDataTypeString(String dataTypeString) {
        int indexOfLeftParen = dataTypeString.indexOf('(');
        if (indexOfLeftParen < 0) {
            return unescapeDataTypeName(dataTypeString);
        }

        return unescapeDataTypeName(dataTypeString.substring(0, indexOfLeftParen))
                + dataTypeString.substring(indexOfLeftParen);
    }

    public boolean sendsStringParametersAsUnicode() {
        if (sendsStringParametersAsUnicode == null) {
            try {
                if (getConnection() instanceof JdbcConnection) {
                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    try {
                        String sql = "SELECT CONVERT([sysname], SQL_VARIANT_PROPERTY(?, 'BaseType'))";
                        ps = ((JdbcConnection) getConnection()).prepareStatement(sql);
                        ps.setString(1, "Liquibase");
                        rs = ps.executeQuery();
                        String baseType = null;
                        if (rs.next()) {
                            baseType = rs.getString(1);
                        }
                        sendsStringParametersAsUnicode = baseType == null || baseType.startsWith("n"); // i.e. nvarchar (or nchar)
                    } finally {
                        JdbcUtils.close(rs, ps);
                    }
                } else if (getConnection() instanceof OfflineConnection) {
                    sendsStringParametersAsUnicode = ((OfflineConnection) getConnection()).getSendsStringParametersAsUnicode();
                }
            } catch (Exception e) {
                LogFactory.getLogger().warning("Cannot determine whether String parameters are sent as Unicode for MSSQL", e);
            }
        }

        return sendsStringParametersAsUnicode == null ? true : sendsStringParametersAsUnicode;
    }

    public boolean isAzureDb() {
        return "Azure".equalsIgnoreCase(getEngineEdition());
    }

    public String getEngineEdition() {
        try {
            if (getConnection() instanceof JdbcConnection) {
                String sql = "SELECT CASE ServerProperty('EngineEdition')\n" +
                        "         WHEN 1 THEN 'Personal'\n" +
                        "         WHEN 2 THEN 'Standard'\n" +
                        "         WHEN 3 THEN 'Enterprise'\n" +
                        "         WHEN 4 THEN 'Express'\n" +
                        "         WHEN 5 THEN 'Azure'\n" +
                        "         ELSE 'Unknown'\n" +
                        "       END";
                return ExecutorService.getInstance().getExecutor(this).queryForObject(new RawSqlStatement(sql), String.class);
            }
        } catch (DatabaseException e) {
            LogFactory.getLogger().warning("Could not determine engine edition", e);
        }
        return "Unknown";
    }
}
