package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Pattern;

/**
 * Created by martoccia.i on 19/10/2016.
 */
public class Ingres9Database extends AbstractJdbcDatabase {

    public static final String PRODUCT_NAME = "INGRES";

    private static Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public Ingres9Database() {
        setCurrentDateTimeFunction("date('now')");
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection databaseConnection) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(databaseConnection.getDatabaseProductName());
    }

    @Override
    protected String getConnectionSchemaName() {
        return getConnection().getConnectionUserName();
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
        final String sql = "select text_segment from iiviews where table_name = '" + viewName + "'";
        Statement stmt = null;
        String definition = "";
        try {
            if (getConnection() instanceof OfflineConnection) {
                throw new DatabaseException("Cannot execute commands against an offline database");
            }
            stmt = ((JdbcConnection) getConnection()).getUnderlyingConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                definition += rs.getString("text_segment");
            }
        }
        catch (Exception ex) {
            JdbcUtils.closeStatement(stmt);
            stmt = null;
            return null;
        }
        finally {
            JdbcUtils.closeStatement(stmt);
        }
        if (definition == null) {
            return null;
        }
        return CREATE_VIEW_AS_PATTERN.matcher(definition).replaceFirst("");
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:ingres:")) {
            return "com.ingres.jdbc.IngresDriver";
        }
        return null;
    }

    @Override
    public String getShortName() {
        return "ingres";
    }

    @Override
    public Integer getDefaultPort() {
        return 21071;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return PRODUCT_NAME;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        if (example instanceof Table) {
            if (example.getSchema() != null) {
                if ("$ingres".equals(example.getSchema().getName())) {
                    return true;
                }
            }
        }
        return super.isSystemObject(example);
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }

}
