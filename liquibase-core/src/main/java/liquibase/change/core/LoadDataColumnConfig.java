package liquibase.change.core;

import liquibase.change.ColumnConfig;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

public class LoadDataColumnConfig extends ColumnConfig {

    private Integer index;
    private String header;
    private Boolean allowUpdate;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
    
    /**
     * Returns true if this Column should be updated. Returns null if update hasn't been explicitly assigned.
     */  
	public Boolean getAllowUpdate() {
		return allowUpdate;
	}

	public void setAllowUpdate(Boolean getAllowUpdate) {
		this.allowUpdate = getAllowUpdate;
	}

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        super.load(parsedNode, resourceAccessor);
        this.index = parsedNode.getChildValue(null, "index", Integer.class);
        this.header = parsedNode.getChildValue(null, "header", String.class);
        this.allowUpdate = parsedNode.getChildValue(null, "allowUpdate", Boolean.class);
    }

    private LoadDataChange.LOAD_DATA_TYPE loadType;

    public ColumnConfig setType(LoadDataChange.LOAD_DATA_TYPE value) {
        super.setType(value.toString());
        this.loadType = value;
        return this;
    }

    public LoadDataChange.LOAD_DATA_TYPE type() {
        if (this.loadType == null) {
            try {
                this.loadType = LoadDataChange.LOAD_DATA_TYPE.valueOf(this.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return this.loadType;
    }
}
