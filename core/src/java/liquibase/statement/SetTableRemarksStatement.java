package liquibase.statement;

public class SetTableRemarksStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String remarks;

    public SetTableRemarksStatement(String schemaName, String tableName, String remarks) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.remarks = remarks;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getRemarks() {
        return remarks;
    }

}
