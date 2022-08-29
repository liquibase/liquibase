package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.View;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class BigQueryJdbcDatabaseSnapshot { /*extends JdbcDatabaseSnapshot{

    private JdbcDatabaseSnapshot.CachingDatabaseMetaData cachingDatabaseMetaData;
    private Set<String> userDefinedTypes;

    public BigQueryJdbcDatabaseSnapshot(DatabaseObject[] examples, Database database) throws DatabaseException, InvalidExampleException {
        super(examples, database);
    }

    public BigQueryJdbcDatabaseSnapshot(DatabaseObject[] examples, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        super(examples, database, snapshotControl);
    }

    public BigQueryCachingDatabaseMetaData getMetaDataFromCache() throws SQLException {
        if (this.cachingDatabaseMetaData == null) {
            DatabaseMetaData databaseMetaData = null;
            if (this.getDatabase().getConnection() != null) {
                databaseMetaData = ((JdbcConnection)this.getDatabase().getConnection()).getUnderlyingConnection().getMetaData();
            }

            this.cachingDatabaseMetaData = new BigQueryCachingDatabaseMetaData(this.getDatabase(), databaseMetaData);
        }

        return this.cachingDatabaseMetaData;
    }



    @Override
    public List<CachedRow> getViews(final String catalogName, final String schemaName, String viewName) throws DatabaseException {
        final String view;
        view = viewName;


        return BigQueryJdbcDatabaseSnapshot.this.getResultSetCache("getViews").get(new ResultSetCache.SingleResultSetExtractor(this.database) {
            boolean shouldBulkSelect(String schemaKey, ResultSetCache resultSetCache) {
                return view == null || BigQueryJdbcDatabaseSnapshot.this.getAllCatalogsStringScratchData() != null || super.shouldBulkSelect(schemaKey, resultSetCache);
            }

            public ResultSetCache.RowData rowKeyParameters(CachedRow row) {
                return new ResultSetCache.RowData(row.getString("TABLE_CAT"), row.getString("TABLE_SCHEM"), CachingDatabaseMetaData.this.database, new String[]{row.getString("TABLE_NAME")});
            }

            public ResultSetCache.RowData wantedKeyParameters() {
                return new ResultSetCache.RowData(catalogName, schemaName, CachingDatabaseMetaData.this.database, new String[]{view});
            }

            public boolean bulkContainsSchema(String schemaKey) {
                return CachingDatabaseMetaData.this.database instanceof OracleDatabase;
            }

            public String getSchemaKey(CachedRow row) {
                return row.getString("TABLE_SCHEM");
            }

            public List<CachedRow> fastFetchQuery() throws SQLException, DatabaseException {
                CatalogAndSchema catalogAndSchema = (new CatalogAndSchema(catalogName, schemaName)).customize(CachingDatabaseMetaData.this.database);
                if (CachingDatabaseMetaData.this.database instanceof OracleDatabase) {
                    return this.queryOracle(catalogAndSchema, view);
                } else {
                    String catalog = ((AbstractJdbcDatabase)CachingDatabaseMetaData.this.database).getJdbcCatalogName(catalogAndSchema);
                    String schema = ((AbstractJdbcDatabase)CachingDatabaseMetaData.this.database).getJdbcSchemaName(catalogAndSchema);
                    return this.extract(CachingDatabaseMetaData.this.databaseMetaData.getTables(catalog, JdbcDatabaseSnapshot.this.escapeForLike(schema, CachingDatabaseMetaData.this.database), view == null ? "%" : JdbcDatabaseSnapshot.this.escapeForLike(view, CachingDatabaseMetaData.this.database), new String[]{"VIEW"}));
                }
            }

            public List<CachedRow> bulkFetchQuery() throws SQLException, DatabaseException {
                CatalogAndSchema catalogAndSchema = (new CatalogAndSchema(catalogName, schemaName)).customize(CachingDatabaseMetaData.this.database);
                if (CachingDatabaseMetaData.this.database instanceof OracleDatabase) {
                    return this.queryBigQueryInformationSchema(catalogAndSchema, (String)null);
                } else {
                    String catalog = ((AbstractJdbcDatabase)CachingDatabaseMetaData.this.database).getJdbcCatalogName(catalogAndSchema);
                    String schema = ((AbstractJdbcDatabase)CachingDatabaseMetaData.this.database).getJdbcSchemaName(catalogAndSchema);
                    return this.extract(CachingDatabaseMetaData.this.databaseMetaData.getTables(catalog, JdbcDatabaseSnapshot.this.escapeForLike(schema, CachingDatabaseMetaData.this.database), "%", new String[]{"VIEW"}));
                }
            }

            private List<CachedRow> queryBigQueryInformationSchema(CatalogAndSchema catalogAndSchema, String viewName) throws DatabaseException, SQLException {
                String ownerName = CachingDatabaseMetaData.this.database.correctObjectName(catalogAndSchema.getCatalogName(), Schema.class);
                String sql = "SELECT null as TABLE_CAT, a.OWNER as TABLE_SCHEM, a.VIEW_NAME as TABLE_NAME, 'TABLE' as TABLE_TYPE, c.COMMENTS as REMARKS, TEXT as OBJECT_BODY";
                if (CachingDatabaseMetaData.this.database.getDatabaseMajorVersion() > 10) {
                    sql = sql + ", EDITIONING_VIEW";
                }

                sql = sql + " from ALL_VIEWS a join ALL_TAB_COMMENTS c on a.VIEW_NAME=c.table_name and a.owner=c.owner ";
                if (viewName == null && JdbcDatabaseSnapshot.this.getAllCatalogsStringScratchData() != null) {
                    sql = sql + "WHERE a.OWNER IN ('" + ownerName + "', " + JdbcDatabaseSnapshot.this.getAllCatalogsStringScratchData() + ")";
                } else {
                    sql = sql + "WHERE a.OWNER='" + ownerName + "'";
                }

                if (viewName != null) {
                    sql = sql + " AND a.VIEW_NAME='" + CachingDatabaseMetaData.this.database.correctObjectName(viewName, View.class) + "'";
                }

                sql = sql + " AND a.VIEW_NAME not in (select mv.name from all_registered_mviews mv where mv.owner=a.owner)";
                return this.executeAndExtract(sql, CachingDatabaseMetaData.this.database);
            }
        });
    }

*/

}
