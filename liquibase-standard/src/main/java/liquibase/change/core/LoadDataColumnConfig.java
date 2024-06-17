package liquibase.change.core;

import liquibase.change.ColumnConfig;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import lombok.Getter;
import lombok.Setter;

public class LoadDataColumnConfig extends ColumnConfig {

    @Setter
    @Getter
    private Integer index;
    @Setter
    @Getter
    private String header;
    @Setter
    private Boolean allowUpdate;
    private LoadDataChange.LOAD_DATA_TYPE loadType;

    /**
     * Returns true if this Column should be updated. Returns null if update hasn't been explicitly assigned.
     */  
	public Boolean getAllowUpdate() {
		return allowUpdate;
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
     * Returns the type of this {@link LoadDataChange} as a standard enum, or null if the type is {@code null},
     * OR {@link liquibase.change.core.LoadDataChange.LOAD_DATA_TYPE#UNKNOWN} if it doesn't match a standard type.
     *
     * @return LoadDataChange.LOAD_DATA_TYPE enum or null
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
