package liquibase.ext.bigquery.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.BigQueryResultSetCache;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.core.Schema;

import java.sql.SQLException;
import java.util.List;

public class BigQueryResultSetConstraintsExtractor extends BigQueryResultSetCache.SingleResultSetExtractor {
    private Database database;
    private String catalogName;
    private String schemaName;
    private String tableName;

    public BigQueryResultSetConstraintsExtractor(DatabaseSnapshot databaseSnapshot, String catalogName, String schemaName, String tableName) {
        super(databaseSnapshot.getDatabase());
        this.database = databaseSnapshot.getDatabase();
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public boolean bulkContainsSchema(String schemaKey) {
        return false;
    }

    public BigQueryResultSetCache.RowData rowKeyParameters(CachedRow row) {
        return new BigQueryResultSetCache.RowData(this.catalogName, this.schemaName, this.database, new String[]{row.getString("TABLE_NAME")});
    }

    public BigQueryResultSetCache.RowData wantedKeyParameters() {
        return new BigQueryResultSetCache.RowData(this.catalogName, this.schemaName, this.database, new String[]{this.tableName});
    }

    public List<CachedRow> fastFetchQuery() throws SQLException, DatabaseException {
        CatalogAndSchema catalogAndSchema = (new CatalogAndSchema(this.catalogName, this.schemaName)).customize(this.database);
        return this.executeAndExtract(this.createSql(((AbstractJdbcDatabase)this.database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase)this.database).getJdbcSchemaName(catalogAndSchema), this.tableName), this.database, false);
    }

    public List<CachedRow> bulkFetchQuery() throws SQLException, DatabaseException {
        CatalogAndSchema catalogAndSchema = (new CatalogAndSchema(this.catalogName, this.schemaName)).customize(this.database);
        return this.executeAndExtract(this.createSql(((AbstractJdbcDatabase)this.database).getJdbcCatalogName(catalogAndSchema), ((AbstractJdbcDatabase)this.database).getJdbcSchemaName(catalogAndSchema), (String)null), this.database);
    }

    private String createSql(String catalog, String schema, String table) {

        CatalogAndSchema catalogAndSchema = (new CatalogAndSchema(catalog, schema)).customize(this.database);
        String jdbcSchemaName = this.database.correctObjectName(((AbstractJdbcDatabase)this.database).getJdbcSchemaName(catalogAndSchema), Schema.class);
        String sql = "select NULL AS CONSTRAINT_NAME, NULL AS CONSTRAINT_TYPE, NULL AS TABLE_NAME from " + jdbcSchemaName+"."+this.database.getSystemSchema().toUpperCase() + ".COLUMNS where TABLE_SCHEMA='" + jdbcSchemaName + "' AND 1=0"; //and CONSTRAINT_TYPE='UNIQUE'";
        System.out.println("SQL: "+sql);
        if (table != null) {
            sql = sql + " and TABLE_NAME='" + table + "'";
        }

        return sql;
    }

}
