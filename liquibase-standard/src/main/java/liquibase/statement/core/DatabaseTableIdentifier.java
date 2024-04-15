package liquibase.statement.core;
public class DatabaseTableIdentifier{
    private String catalogName;

    public String getCatalogName(){
        return catalogName;
    }

    public void setCatalogName(String catalogName){
        this.catalogName=catalogName;
    }

    private String schemaName;

    public String getSchemaName(){
        return schemaName;
    }

    public void setSchemaName(String schemaName){
        this.schemaName=schemaName;
    }

    private String tableName;

    public String getTableName(){
        return tableName;
    }

    public void setTableName(String tableName){
        this.tableName=tableName;
    }

    public DatabaseTableIdentifier(String catalogName,String schemaName,String tableName){
        this.catalogName=catalogName;
        this.schemaName=schemaName;
        this.tableName=tableName;
    }
}

