package liquibase.preconditions;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.log.LogFactory;
import liquibase.util.StringUtils;

public class ForeignKeyExistsPrecondition implements Precondition {
    static final protected Logger log = LogFactory.getLogger();
    private String schemaName;
    private String foreignKeyName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public void setForeignKeyName(String foreignKeyName) {
        this.foreignKeyName = foreignKeyName;
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        // Use DatabaseMetaData to query db's data dictionary
        DatabaseConnection conn = database.getConnection();
        ResultSet foreignKeys = null;
        try {
            String schemaName = getSchemaName();
            DatabaseMetaData dbm = conn.getMetaData();
            foreignKeys = dbm.getCrossReference(
                    schemaName,
                    schemaName,
                    null,
                    schemaName,
                    schemaName,
                    null
            );
            while (foreignKeys.next()) {
                String fk = foreignKeys.getString("FK_NAME");
                if (getForeignKeyName().equalsIgnoreCase(fk)) {
                    return;
                }
            }
        } catch (SQLException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        } finally {
            if (foreignKeys != null) {
                try {
                    foreignKeys.close();
                } catch (SQLException e) {
                    log.warning("Error closing result set: " + e.getMessage());
                }
            }
        }
        // If we got here, the foreign key was not found.
        throw new PreconditionFailedException("Foreign Key "+database.escapeStringForDatabase(getForeignKeyName())+" does not exist", changeLog, this);
    }

    public String getTagName() {
        return "foreignKeyConstraintExists";
    }
}