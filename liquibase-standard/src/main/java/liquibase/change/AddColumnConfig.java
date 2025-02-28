package liquibase.change;


import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.core.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddColumnConfig extends ColumnConfig {

    private String afterColumn;
    private String beforeColumn;
    private Integer position;

    public AddColumnConfig(Column columnSnapshot) {
        super(columnSnapshot);
    }

    public AddColumnConfig() {
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        super.load(parsedNode, resourceAccessor);
        this.beforeColumn = parsedNode.getChildValue(null, "beforeColumn", String.class);
        this.afterColumn = parsedNode.getChildValue(null, "afterColumn", String.class);
        this.position = parsedNode.getChildValue(null, "position", Integer.class);
    }
}
