package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropDatabaseStatement extends AbstractSqlStatement {
    
    private String databaseName;
    private Boolean ifExists;
    private Boolean cascade;
    private Boolean restrict;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
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
     * based on Snowflake DROP DATABASE constraints.
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Basic validation
        if (databaseName == null || databaseName.trim().isEmpty()) {
            result.addError("Database name is required");
        } else if (databaseName.length() > 255 || !databaseName.matches("^[A-Za-z_][A-Za-z0-9_$]*$")) {
            result.addError("Invalid database name format. Must start with letter/underscore, contain only letters/numbers/underscores/dollar signs, max 255 characters: " + databaseName);
        }
        
        // Validate CASCADE vs RESTRICT mutual exclusivity
        if (Boolean.TRUE.equals(cascade) && Boolean.TRUE.equals(restrict)) {
            result.addError("CASCADE and RESTRICT cannot be used together");
        }
        
        // Warn about destructive operation
        if (!Boolean.TRUE.equals(ifExists)) {
            result.addWarning("DROP DATABASE without IF EXISTS will fail if database does not exist");
        }
        
        if (Boolean.TRUE.equals(cascade)) {
            result.addWarning("CASCADE will drop all objects within the database permanently");
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