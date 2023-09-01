package liquibase.change.visitor;

import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

public class AddColumnChangeVisitor extends AbstractChangeVisitor {

    private String change;
    private Set<String> dbms;
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
    public Set<String> getDbms() {
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
        this.remove = node.getChildValue(null, "remove", String.class);
        String dbmsString = StringUtil.trimToNull(node.getChildValue(null, "dbms", String.class));
        this.dbms = new HashSet<>();
        if (dbmsString != null) {
            this.dbms.addAll(StringUtil.splitAndTrim(dbmsString, ","));
        }

    }
}
