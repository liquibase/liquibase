package liquibase.statement.core.snowflake;

import liquibase.statement.AbstractSqlStatement;

import java.math.BigInteger;

/**
 * SQL statement for altering a sequence in Snowflake.
 * Supports all ALTER SEQUENCE operations including rename, increment changes, ordering, and comments.
 */
public class AlterSequenceStatementSnowflake extends AbstractSqlStatement {
    
    private String catalogName;
    private String schemaName;
    private String sequenceName;
    private String newSequenceName; // For RENAME TO
    private BigInteger incrementBy;
    private Boolean ordered;
    private String comment;
    private Boolean unsetComment;
    private Boolean ifExists;

    public AlterSequenceStatementSnowflake(String catalogName, String schemaName, String sequenceName) {
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

    public String getNewSequenceName() {
        return newSequenceName;
    }

    public void setNewSequenceName(String newSequenceName) {
        this.newSequenceName = newSequenceName;
    }

    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    public void setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
    }

    public Boolean getOrdered() {
        return ordered;
    }

    public void setOrdered(Boolean ordered) {
        this.ordered = ordered;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getUnsetComment() {
        return unsetComment;
    }

    public void setUnsetComment(Boolean unsetComment) {
        this.unsetComment = unsetComment;
    }

    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }
}