package liquibase.change.core;

import liquibase.change.ColumnConfig;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

public class LoadDataColumnConfig extends ColumnConfig {

    private Integer index;
    private String header;
    private Boolean updateable;

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
	public Boolean isUpdateable() {
		return updateable;
	}

	public void setUpdateable(Boolean updateable) {
		this.updateable = updateable;
	}

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        super.load(parsedNode, resourceAccessor);
        this.index = parsedNode.getChildValue(null, "index", Integer.class);
        this.header = parsedNode.getChildValue(null, "header", String.class);
        this.updateable = parsedNode.getChildValue(null, "updateable", Boolean.class);
    }


}