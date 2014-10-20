package liquibase.statement.core;

import liquibase.change.DatabaseChangeProperty;

public class InsertOrUpdateStatement extends InsertStatement {
    private String primaryKey;
    private Boolean onlyUpdate = Boolean.FALSE;

    public InsertOrUpdateStatement(String catalogName, String schemaName, String tableName, String primaryKey) {
        super(catalogName, schemaName, tableName);
        this.primaryKey = primaryKey ;
    }

    public InsertOrUpdateStatement(String catalogName, String schemaName, String tableName, String primaryKey, boolean onlyUpdate) {
        this(catalogName, schemaName, tableName,primaryKey);
        this.onlyUpdate = onlyUpdate;
    }
    
    public String getPrimaryKey() {
        return primaryKey;
    }

    @DatabaseChangeProperty(description = "Whether records with no matching database record should be ignored")
    public Boolean getOnlyUpdate() {
    	if ( onlyUpdate == null ) {
    		return false;
    	}
		return onlyUpdate;
	}

	public void setOnlyUpdate(Boolean onlyUpdate) {
		this.onlyUpdate = (onlyUpdate == null ? Boolean.FALSE : onlyUpdate);
	}
}
