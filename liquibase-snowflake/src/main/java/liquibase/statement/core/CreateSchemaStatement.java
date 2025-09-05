package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class CreateSchemaStatement extends AbstractSqlStatement {
    
    private String schemaName;
    private String catalogName;
    private String comment;
    private String dataRetentionTimeInDays;
    private String maxDataExtensionTimeInDays;
    private Boolean transient_;
    private Boolean managed_;
    private String defaultDdlCollation;
    private String pipeExecutionPaused;
    private Boolean orReplace;
    private Boolean ifNotExists;
    private String externalVolume;
    private String cloneFrom;
    private String classificationProfile;
    private String tag;
    private String replaceInvalidCharacters;
    private String storageSerializationPolicy;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDataRetentionTimeInDays() {
        return dataRetentionTimeInDays;
    }

    public void setDataRetentionTimeInDays(String dataRetentionTimeInDays) {
        this.dataRetentionTimeInDays = dataRetentionTimeInDays;
    }

    public String getMaxDataExtensionTimeInDays() {
        return maxDataExtensionTimeInDays;
    }

    public void setMaxDataExtensionTimeInDays(String maxDataExtensionTimeInDays) {
        this.maxDataExtensionTimeInDays = maxDataExtensionTimeInDays;
    }

    public Boolean getTransient() {
        return transient_;
    }

    public void setTransient(Boolean transient_) {
        this.transient_ = transient_;
    }

    public Boolean getManaged() {
        return managed_;
    }

    public void setManaged(Boolean managed_) {
        this.managed_ = managed_;
    }

    public String getDefaultDdlCollation() {
        return defaultDdlCollation;
    }

    public void setDefaultDdlCollation(String defaultDdlCollation) {
        this.defaultDdlCollation = defaultDdlCollation;
    }

    public String getPipeExecutionPaused() {
        return pipeExecutionPaused;
    }

    public void setPipeExecutionPaused(String pipeExecutionPaused) {
        this.pipeExecutionPaused = pipeExecutionPaused;
    }

    public Boolean getOrReplace() {
        return orReplace;
    }

    public void setOrReplace(Boolean orReplace) {
        this.orReplace = orReplace;
    }

    public Boolean getIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(Boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
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

    public String getExternalVolume() {
        return externalVolume;
    }

    public void setExternalVolume(String externalVolume) {
        this.externalVolume = externalVolume;
    }


    public String getCloneFrom() {
        return cloneFrom;
    }

    public void setCloneFrom(String cloneFrom) {
        this.cloneFrom = cloneFrom;
    }

    public String getClassificationProfile() {
        return classificationProfile;
    }

    public void setClassificationProfile(String classificationProfile) {
        this.classificationProfile = classificationProfile;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getReplaceInvalidCharacters() {
        return replaceInvalidCharacters;
    }

    public void setReplaceInvalidCharacters(String replaceInvalidCharacters) {
        this.replaceInvalidCharacters = replaceInvalidCharacters;
    }

    public String getStorageSerializationPolicy() {
        return storageSerializationPolicy;
    }

    public void setStorageSerializationPolicy(String storageSerializationPolicy) {
        this.storageSerializationPolicy = storageSerializationPolicy;
    }

    /**
     * Enhanced validation method that ensures the statement configuration is valid
     * based on Snowflake CREATE SCHEMA constraints.
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Basic validation
        if (schemaName == null || schemaName.trim().isEmpty()) {
            result.addError("Schema name is required");
        } else if (schemaName.length() > 255 || !schemaName.matches("^[A-Za-z_][A-Za-z0-9_$]*$")) {
            result.addError("Invalid schema name format. Must start with letter/underscore, contain only letters/numbers/underscores/dollar signs, max 255 characters: " + schemaName);
        }
        
        // Validate OR REPLACE vs IF NOT EXISTS mutual exclusivity
        if (Boolean.TRUE.equals(orReplace) && Boolean.TRUE.equals(ifNotExists)) {
            result.addError("OR REPLACE and IF NOT EXISTS cannot be used together");
        }
        
        // Validate TRANSIENT vs MANAGED mutual exclusivity
        if (Boolean.TRUE.equals(transient_) && Boolean.TRUE.equals(managed_)) {
            result.addError("TRANSIENT and MANAGED cannot be used together");
        }
        
        // Validate data retention time range (0-90 days for standard, up to 1 year for Enterprise)
        if (dataRetentionTimeInDays != null) {
            try {
                int days = Integer.parseInt(dataRetentionTimeInDays);
                if (days < 0) {
                    result.addError("DATA_RETENTION_TIME_IN_DAYS cannot be negative, got: " + days);
                } else if (days > 90) {
                    result.addWarning("DATA_RETENTION_TIME_IN_DAYS > 90 days requires Snowflake Enterprise Edition, got: " + days);
                    if (days > 365) {
                        result.addError("DATA_RETENTION_TIME_IN_DAYS cannot exceed 365 days, got: " + days);
                    }
                }
            } catch (NumberFormatException e) {
                result.addError("DATA_RETENTION_TIME_IN_DAYS must be a valid integer, got: " + dataRetentionTimeInDays);
            }
        }
        
        // Validate max data extension time range
        if (maxDataExtensionTimeInDays != null) {
            try {
                int days = Integer.parseInt(maxDataExtensionTimeInDays);
                if (days < 0) {
                    result.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS cannot be negative, got: " + days);
                } else if (days > 14) {
                    result.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS cannot exceed 14 days, got: " + days);
                }
            } catch (NumberFormatException e) {
                result.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS must be a valid integer, got: " + maxDataExtensionTimeInDays);
            }
        }
        
        // Validate pipe execution paused enumeration
        if (pipeExecutionPaused != null) {
            String[] validStates = {"TRUE", "FALSE"};
            boolean validState = false;
            for (String validSt : validStates) {
                if (validSt.equalsIgnoreCase(pipeExecutionPaused)) {
                    validState = true;
                    break;
                }
            }
            if (!validState) {
                result.addError("Invalid PIPE_EXECUTION_PAUSED value '" + pipeExecutionPaused + "'. Valid values: TRUE, FALSE");
            }
        }
        
        // Validate storage serialization policy enumeration  
        if (storageSerializationPolicy != null) {
            String[] validPolicies = {"COMPATIBLE", "OPTIMIZED"};
            boolean validPolicy = false;
            for (String validPol : validPolicies) {
                if (validPol.equalsIgnoreCase(storageSerializationPolicy)) {
                    validPolicy = true;
                    break;
                }
            }
            if (!validPolicy) {
                result.addError("Invalid STORAGE_SERIALIZATION_POLICY '" + storageSerializationPolicy + "'. Valid values: COMPATIBLE, OPTIMIZED");
            }
        }
        
        // Validate replaceInvalidCharacters enumeration
        if (replaceInvalidCharacters != null) {
            String[] validOptions = {"TRUE", "FALSE"};
            boolean validOption = false;
            for (String validOpt : validOptions) {
                if (validOpt.equalsIgnoreCase(replaceInvalidCharacters)) {
                    validOption = true;
                    break;
                }
            }
            if (!validOption) {
                result.addError("Invalid REPLACE_INVALID_CHARACTERS value '" + replaceInvalidCharacters + "'. Valid values: TRUE, FALSE");
            }
        }
        
        // Validate clone operation constraints
        if (cloneFrom != null) {
            if (Boolean.TRUE.equals(transient_)) {
                result.addError("Cannot create TRANSIENT schema from clone");
            }
            if (dataRetentionTimeInDays != null) {
                result.addWarning("DATA_RETENTION_TIME_IN_DAYS is inherited from source schema when cloning");
            }
        }
        
        // Validate external volume constraints
        if (externalVolume != null) {
            result.addWarning("External volumes require appropriate cloud storage permissions and configuration");
        }
        
        // Validate classification profile constraints
        if (classificationProfile != null) {
            result.addWarning("Classification profiles require Snowflake Enterprise Edition and proper data classification setup");
        }
        
        // Warn about Enterprise Edition features
        if (Boolean.TRUE.equals(managed_)) {
            result.addWarning("Managed access schemas require Snowflake Enterprise Edition");
        }
        if (Boolean.TRUE.equals(transient_)) {
            result.addWarning("TRANSIENT schemas provide cost optimization through reduced data protection");
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