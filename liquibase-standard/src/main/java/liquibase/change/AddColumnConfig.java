package liquibase.change;


import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.core.Column;

public class AddColumnConfig extends ColumnConfig {

    private String afterColumn;
    private String beforeColumn;
    private Integer position;

    public AddColumnConfig(Column columnSnapshot) {
        super(columnSnapshot);
    }

    public AddColumnConfig() {
    }

    public String getAfterColumn() {
        return afterColumn;
    }

    public void setAfterColumn(String afterColumn) {
        this.afterColumn = afterColumn;
    }

    public String getBeforeColumn() {
        return beforeColumn;
    }

    public void setBeforeColumn(String beforeColumn) {
        this.beforeColumn = beforeColumn;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        super.load(parsedNode, resourceAccessor);
        this.beforeColumn = parsedNode.getChildValue(null, "beforeColumn", String.class);
        this.afterColumn = parsedNode.getChildValue(null, "afterColumn", String.class);
        this.position = parsedNode.getChildValue(null, "position", Integer.class);
    }
}
