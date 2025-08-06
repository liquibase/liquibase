package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropSchemaStatement extends AbstractSqlStatement {
    
    private String schemaName;
    private String catalogName;
    private Boolean ifExists;
    private Boolean cascade;
    private Boolean restrict;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public void setCatalog(String catalog) {
        this.catalogName = catalog;
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