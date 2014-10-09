package liquibase.statement.core;

import liquibase.change.DatabaseChangeProperty;

public class InsertOrUpdateStatement extends InsertStatement {
    private String primaryKey;
    private Boolean updateOnly = Boolean.FALSE;

    public InsertOrUpdateStatement(String catalogName, String schemaName, String tableName, String primaryKey) {
        super(catalogName, schemaName, tableName);
        this.primaryKey = primaryKey ;
    }

    public InsertOrUpdateStatement(String catalogName, String schemaName, String tableName, String primaryKey, boolean updateOnly ) {
        this(catalogName, schemaName, tableName,primaryKey);
        this.updateOnly = updateOnly;
    }
    
    public String getPrimaryKey() {
        return primaryKey;
    }

    @DatabaseChangeProperty(description = "Whether records with no matching database record should be ignored")
    public Boolean getUpdateOnly() {
    	if ( updateOnly == null ) {
    		return false;
    	}
		return updateOnly;
	}

	public void setUpdateOnly(Boolean updateOnly) {
		this.updateOnly = updateOnly;
	}
}
