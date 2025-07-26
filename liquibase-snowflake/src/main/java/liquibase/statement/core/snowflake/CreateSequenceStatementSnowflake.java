package liquibase.statement.core.snowflake;

import liquibase.statement.core.CreateSequenceStatement;

/**
 * Snowflake-specific CreateSequenceStatement with ORDER/NOORDER and OR REPLACE/IF NOT EXISTS support.
 */
public class CreateSequenceStatementSnowflake extends CreateSequenceStatement {
    
    private Boolean orReplace;
    private Boolean ifNotExists;
    private String comment;
    
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
}