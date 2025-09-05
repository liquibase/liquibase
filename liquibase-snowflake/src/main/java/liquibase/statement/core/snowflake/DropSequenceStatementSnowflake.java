package liquibase.statement.core.snowflake;

import liquibase.statement.AbstractSqlStatement;

/**
 * SQL statement for dropping a sequence in Snowflake.
 * Supports IF EXISTS, CASCADE, and RESTRICT options.
 */
public class DropSequenceStatementSnowflake extends AbstractSqlStatement {
    
    private String catalogName;
    private String schemaName;
    private String sequenceName;
    private Boolean ifExists;
    private Boolean cascade;
    private Boolean restrict;

    public DropSequenceStatementSnowflake(String catalogName, String schemaName, String sequenceName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.sequenceName = sequenceName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    public Boolean getCascade() {
        return cascade;
    }

    public void setCascade(Boolean cascade) {
        this.cascade = cascade;
    }

    public Boolean getRestrict() {
        return restrict;
    }

    public void setRestrict(Boolean restrict) {
        this.restrict = restrict;
    }
}