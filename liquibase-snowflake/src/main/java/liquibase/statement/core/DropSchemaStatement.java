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
    
    /**
     * Enhanced validation method that ensures the statement configuration is valid
     * based on Snowflake DROP SCHEMA constraints.
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Basic validation
        if (schemaName == null || schemaName.trim().isEmpty()) {
            result.addError("Schema name is required");
        } else if (schemaName.length() > 255 || !schemaName.matches("^[A-Za-z_][A-Za-z0-9_$]*$")) {
            result.addError("Invalid schema name format. Must start with letter/underscore, contain only letters/numbers/underscores/dollar signs, max 255 characters: " + schemaName);
        }
        
        // Validate CASCADE vs RESTRICT mutual exclusivity
        if (Boolean.TRUE.equals(cascade) && Boolean.TRUE.equals(restrict)) {
            result.addError("CASCADE and RESTRICT cannot be used together");
        }
        
        // Warn about destructive operation
        if (!Boolean.TRUE.equals(ifExists)) {
            result.addWarning("DROP SCHEMA without IF EXISTS will fail if schema does not exist");
        }
        
        if (Boolean.TRUE.equals(cascade)) {
            result.addWarning("CASCADE will drop all objects within the schema permanently");
        }
        
        return result;
    }
    
    /**
     * Simple validation result container
     */
    public static class ValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        
        public boolean hasErrors() { return !errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public java.util.List<String> getErrors() { return errors; }
        public java.util.List<String> getWarnings() { return warnings; }
        
        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
    }
}