package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class CreateDatabaseStatement extends AbstractSqlStatement {
    
    private String databaseName;
    private String comment;
    private String dataRetentionTimeInDays;
    private String maxDataExtensionTimeInDays;
    private Boolean transient_;
    private String defaultDdlCollation;
    private Boolean orReplace;
    private Boolean ifNotExists;
    private String cloneFrom;
    private String fromDatabase;
    private String tag;
    private String externalVolume;
    private String catalog;
    private Boolean replaceInvalidCharacters;
    private String storageSerializationPolicy;
    private String catalogSync;
    private String catalogSyncNamespaceMode;
    private String catalogSyncNamespaceFlattenDelimiter;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
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

    public String getDefaultDdlCollation() {
        return defaultDdlCollation;
    }

    public void setDefaultDdlCollation(String defaultDdlCollation) {
        this.defaultDdlCollation = defaultDdlCollation;
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

    public String getCloneFrom() {
        return cloneFrom;
    }

    public void setCloneFrom(String cloneFrom) {
        this.cloneFrom = cloneFrom;
    }

    public String getFromDatabase() {
        return fromDatabase;
    }

    public void setFromDatabase(String fromDatabase) {
        this.fromDatabase = fromDatabase;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getExternalVolume() {
        return externalVolume;
    }

    public void setExternalVolume(String externalVolume) {
        this.externalVolume = externalVolume;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public Boolean getReplaceInvalidCharacters() {
        return replaceInvalidCharacters;
    }

    public void setReplaceInvalidCharacters(Boolean replaceInvalidCharacters) {
        this.replaceInvalidCharacters = replaceInvalidCharacters;
    }

    public String getStorageSerializationPolicy() {
        return storageSerializationPolicy;
    }

    public void setStorageSerializationPolicy(String storageSerializationPolicy) {
        this.storageSerializationPolicy = storageSerializationPolicy;
    }

    public String getCatalogSync() {
        return catalogSync;
    }

    public void setCatalogSync(String catalogSync) {
        this.catalogSync = catalogSync;
    }

    public String getCatalogSyncNamespaceMode() {
        return catalogSyncNamespaceMode;
    }

    public void setCatalogSyncNamespaceMode(String catalogSyncNamespaceMode) {
        this.catalogSyncNamespaceMode = catalogSyncNamespaceMode;
    }

    public String getCatalogSyncNamespaceFlattenDelimiter() {
        return catalogSyncNamespaceFlattenDelimiter;
    }

    public void setCatalogSyncNamespaceFlattenDelimiter(String catalogSyncNamespaceFlattenDelimiter) {
        this.catalogSyncNamespaceFlattenDelimiter = catalogSyncNamespaceFlattenDelimiter;
    }

    /**
     * Enhanced validation method that ensures the statement configuration is valid
     * based on Snowflake CREATE DATABASE constraints.
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Basic validation
        if (databaseName == null || databaseName.trim().isEmpty()) {
            result.addError("Database name is required");
        } else if (databaseName.length() > 255 || !databaseName.matches("^[A-Za-z_][A-Za-z0-9_$]*$")) {
            result.addError("Invalid database name format. Must start with letter/underscore, contain only letters/numbers/underscores/dollar signs, max 255 characters: " + databaseName);
        }
        
        // Validate OR REPLACE vs IF NOT EXISTS mutual exclusivity
        if (Boolean.TRUE.equals(orReplace) && Boolean.TRUE.equals(ifNotExists)) {
            result.addError("OR REPLACE and IF NOT EXISTS cannot be used together");
        }
        
        // Validate data retention time range (0-90 days for standard, up to 1 year for Enterprise)
        if (dataRetentionTimeInDays != null) {
            try {
                int days = Integer.parseInt(dataRetentionTimeInDays);
                if (days < 0) {
                    result.addError("DATA_RETENTION_TIME_IN_DAYS cannot be negative, got: " + days);
                } else if (days > 90) {
                    result.addWarning("DATA_RETENTION_TIME_IN_DAYS > 90 days requires Snowflake Enterprise Edition, got: " + days);
                } else if (days > 365) {
                    result.addError("DATA_RETENTION_TIME_IN_DAYS cannot exceed 365 days, got: " + days);
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
        
        // Validate catalog sync options
        if (catalogSync != null) {
            String[] validSyncOptions = {"ENABLE", "DISABLE"};
            boolean validSync = false;
            for (String validOpt : validSyncOptions) {
                if (validOpt.equalsIgnoreCase(catalogSync)) {
                    validSync = true;
                    break;
                }
            }
            if (!validSync) {
                result.addError("Invalid CATALOG_SYNC value '" + catalogSync + "'. Valid values: ENABLE, DISABLE");
            }
        }
        
        // Validate catalog sync namespace mode
        if (catalogSyncNamespaceMode != null) {
            String[] validModes = {"SINGLE", "MULTIPLE"};
            boolean validMode = false;
            for (String validMod : validModes) {
                if (validMod.equalsIgnoreCase(catalogSyncNamespaceMode)) {
                    validMode = true;
                    break;
                }
            }
            if (!validMode) {
                result.addError("Invalid CATALOG_SYNC_NAMESPACE_MODE '" + catalogSyncNamespaceMode + "'. Valid values: SINGLE, MULTIPLE");
            }
        }
        
        // Validate clone operation constraints
        if (cloneFrom != null || fromDatabase != null) {
            if (Boolean.TRUE.equals(transient_)) {
                result.addError("Cannot create TRANSIENT database from clone");
            }
            if (dataRetentionTimeInDays != null) {
                result.addWarning("DATA_RETENTION_TIME_IN_DAYS is inherited from source database when cloning");
            }
        }
        
        // Validate external volume constraints
        if (externalVolume != null) {
            result.addWarning("External volumes require appropriate cloud storage permissions and configuration");
        }
        
        // Warn about Enterprise Edition features
        if (Boolean.TRUE.equals(transient_)) {
            result.addWarning("TRANSIENT databases provide cost optimization through reduced data protection");
        }
        if (catalog != null) {
            result.addWarning("Catalog integration requires Snowflake Enterprise Edition and appropriate permissions");
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