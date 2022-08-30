package liquibase.change.core;

import liquibase.change.ColumnConfig;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

public class LoadDataColumnConfig extends ColumnConfig {

    private Integer index;
    private String header;
    private Boolean allowUpdate;
    private LoadDataChange.LOAD_DATA_TYPE loadType;


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

    public ColumnConfig setType(LoadDataChange.LOAD_DATA_TYPE value) {
        super.setType(value.toString());
        this.loadType = value;
        return this;
    }

    /**
     * Return {@link #getType()} as a standard enum, or null if the type is null OR {@link liquibase.change.core.LoadDataChange.LOAD_DATA_TYPE#UNKNOWN} if it doesn't match a standard type.
     * @return
     */
    public LoadDataChange.LOAD_DATA_TYPE getTypeEnum() {
        final String type = this.getType();
        if (type == null) {
            return null;
        }
        if (this.loadType == null) {
            try {
                this.loadType = LoadDataChange.LOAD_DATA_TYPE.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return LoadDataChange.LOAD_DATA_TYPE.UNKNOWN;
            }
        }
        return this.loadType;
    }
}
