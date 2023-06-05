package liquibase.change.visitor;

import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

public class AddColumnChangeVisitor extends AbstractChangeVisitor {

    private String change;
    private String dbms;
    private String remove;
    @Override
    public String getName() {
        return "addColumn";
    }
    @Override
    public String getChange() {
        return change;
    }
    @Override
    public String getDbms() {
        return dbms;
    }
    public String getRemove() {
        return remove;
    }
    @Override
    public String getSerializedObjectName() {
        return getName();
    }
    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
    @Override
    public void load(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        this.change = node.getChildValue(null, "change", String.class);
        this.dbms = node.getChildValue(null, "dbms", String.class);
        this.remove = node.getChildValue(null, "remove", String.class);

    }
}
