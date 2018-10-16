package liquibase.change.core;

import liquibase.change.ColumnConfig;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

public class LoadDataColumnConfig extends ColumnConfig {

    private Integer index;
    private String header;
    private String select;
    private String selectNoResults;

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

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public String getSelectNoResults() {
        return selectNoResults;
    }

    public void setSelectNoResults(String selectNoResults) {
        this.selectNoResults = selectNoResults;
    }


    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        super.load(parsedNode, resourceAccessor);
        this.index = parsedNode.getChildValue(null, "index", Integer.class);
        this.header = parsedNode.getChildValue(null, "header", String.class);
        this.select = parsedNode.getChildValue(null, "select", String.class);
        this.selectNoResults = parsedNode.getChildValue(null, "selectNoResults", String.class);
    }
}
