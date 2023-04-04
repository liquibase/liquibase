package liquibase.statement.core;

import liquibase.change.DatabaseChangeProperty;

import java.util.HashMap;
import java.util.Map;

public class InsertOrUpdateStatement extends InsertStatement {
    private String primaryKey;
    private Boolean onlyUpdate = Boolean.FALSE;
    private Map<String, Boolean> allowUpdates = new HashMap<>();

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
        this.onlyUpdate = ((onlyUpdate == null) ? Boolean.FALSE : onlyUpdate);
	}

    public boolean getAllowColumnUpdate(String columnName) {
        final Boolean allow = this.allowUpdates.get(columnName);
        if (allow == null) {
            return true;
        }
        return allow;
    }

    public void setAllowColumnUpdate(String columnName, boolean allowUpdate) {
        this.allowUpdates.put(columnName, allowUpdate);
    }
}
