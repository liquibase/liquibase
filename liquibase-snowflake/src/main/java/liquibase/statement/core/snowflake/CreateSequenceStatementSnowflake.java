package liquibase.statement.core.snowflake;

import liquibase.statement.core.CreateSequenceStatement;

/**
 * Snowflake-specific CreateSequenceStatement with ORDER/NOORDER and OR REPLACE/IF NOT EXISTS support.
 */
public class CreateSequenceStatementSnowflake extends CreateSequenceStatement {
    
    private Boolean orReplace;
    private Boolean ifNotExists;
    private String comment;
    private Boolean order;
    
    public CreateSequenceStatementSnowflake(String catalogName, String schemaName, String sequenceName) {
        super(catalogName, schemaName, sequenceName);
    }
    
    /**
     * Whether sequence values should be ordered (ORDER) or not (NOORDER).
     * Default is NOORDER in Snowflake.
     * Inherited from parent class.
     */
    
    /**
     * Whether to use CREATE OR REPLACE SEQUENCE.
     */
    public Boolean getOrReplace() {
        return orReplace;
    }
    
    public void setOrReplace(Boolean orReplace) {
        this.orReplace = orReplace;
    }
    
    /**
     * Whether to use CREATE SEQUENCE IF NOT EXISTS.
     */
    public Boolean getIfNotExists() {
        return ifNotExists;
    }
    
    public void setIfNotExists(Boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }
    
    /**
     * Comment for the sequence.
     */
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    /**
     * Order parameter for the sequence.
     */
    public Boolean getOrder() {
        return order;
    }
    
    public void setOrder(Boolean order) {
        this.order = order;
    }

    /**
     * Enhanced validation method that ensures the statement configuration is valid
     * based on Snowflake CREATE SEQUENCE constraints.
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Basic validation - sequence name
        if (getSequenceName() == null || getSequenceName().trim().isEmpty()) {
            result.addError("Sequence name is required");
        } else if (getSequenceName().length() > 255 || !getSequenceName().matches("^[A-Za-z_][A-Za-z0-9_$]*$")) {
            result.addError("Invalid sequence name format. Must start with letter/underscore, contain only letters/numbers/underscores/dollar signs, max 255 characters: " + getSequenceName());
        }
        
        // Validate OR REPLACE vs IF NOT EXISTS mutual exclusivity
        if (Boolean.TRUE.equals(orReplace) && Boolean.TRUE.equals(ifNotExists)) {
            result.addError("OR REPLACE and IF NOT EXISTS cannot be used together");
        }
        
        // Validate sequence value ranges
        if (getStartValue() != null && getMinValue() != null) {
            if (getStartValue().compareTo(getMinValue()) < 0) {
                result.addError("START WITH value (" + getStartValue() + ") cannot be less than MINVALUE (" + getMinValue() + ")");
            }
        }
        
        if (getStartValue() != null && getMaxValue() != null) {
            if (getStartValue().compareTo(getMaxValue()) > 0) {
                result.addError("START WITH value (" + getStartValue() + ") cannot be greater than MAXVALUE (" + getMaxValue() + ")");
            }
        }
        
        if (getMinValue() != null && getMaxValue() != null) {
            if (getMinValue().compareTo(getMaxValue()) >= 0) {
                result.addError("MINVALUE (" + getMinValue() + ") must be less than MAXVALUE (" + getMaxValue() + ")");
            }
        }
        
        // Validate increment value
        if (getIncrementBy() != null) {
            if (getIncrementBy().equals(java.math.BigInteger.ZERO)) {
                result.addError("INCREMENT BY cannot be zero");
            }
        }
        
        // Validate cache size
        if (getCacheSize() != null) {
            if (getCacheSize().compareTo(java.math.BigInteger.ONE) < 0) {
                result.addError("CACHE size must be >= 1, got: " + getCacheSize());
            }
            // Snowflake maximum cache size is implementation-dependent, but warn about very large values
            if (getCacheSize().compareTo(java.math.BigInteger.valueOf(1000000)) > 0) {
                result.addWarning("Very large CACHE size may impact performance: " + getCacheSize());
            }
        }
        
        // Validate cycle constraints
        if (Boolean.TRUE.equals(getCycle())) {
            if (getMaxValue() == null && getMinValue() == null) {
                result.addWarning("CYCLE sequences should have explicit MAXVALUE and MINVALUE for predictable behavior");
            }
        }
        
        // Warn about ORDER/NOORDER implications
        if (Boolean.TRUE.equals(order)) {
            result.addWarning("ORDER sequences guarantee ordering but may impact concurrency performance");
        } else if (Boolean.FALSE.equals(order)) {
            result.addWarning("NOORDER sequences provide better concurrency but no ordering guarantees. This setting is irreversible.");
        } else {
            // Default is NOORDER in Snowflake
            result.addInfo("Sequence will use default NOORDER behavior for optimal concurrency");
        }
        
        return result;
    }
    
    /**
     * Simple validation result container
     */
    public static class ValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        private final java.util.List<String> info = new java.util.ArrayList<>();
        
        public boolean hasErrors() { return !errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public boolean hasInfo() { return !info.isEmpty(); }
        public java.util.List<String> getErrors() { return errors; }
        public java.util.List<String> getWarnings() { return warnings; }
        public java.util.List<String> getInfo() { return info; }
        
        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
        public void addInfo(String info) { this.info.add(info); }
    }
}