package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

/**
 * SQL statement for altering table clustering configuration in Snowflake.
 */
public class AlterTableClusterStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String clusterBy;
    private Boolean dropClusteringKey;
    private Boolean suspendRecluster;
    private Boolean resumeRecluster;

    public AlterTableClusterStatement(String catalogName, String schemaName, String tableName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getClusterBy() {
        return clusterBy;
    }

    public void setClusterBy(String clusterBy) {
        this.clusterBy = clusterBy;
    }

    public Boolean getDropClusteringKey() {
        return dropClusteringKey;
    }

    public void setDropClusteringKey(Boolean dropClusteringKey) {
        this.dropClusteringKey = dropClusteringKey;
    }

    public Boolean getSuspendRecluster() {
        return suspendRecluster;
    }

    public void setSuspendRecluster(Boolean suspendRecluster) {
        this.suspendRecluster = suspendRecluster;
    }

    public Boolean getResumeRecluster() {
        return resumeRecluster;
    }

    public void setResumeRecluster(Boolean resumeRecluster) {
        this.resumeRecluster = resumeRecluster;
    }
}