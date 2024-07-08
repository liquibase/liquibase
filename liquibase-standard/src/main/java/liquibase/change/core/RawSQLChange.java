package liquibase.change.core;

import liquibase.change.AbstractSQLChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;
import lombok.Setter;

/**
 * Allows execution of arbitrary SQL. This change can be used when existing change types don't exist
 * or are not flexible enough.
 */
@DatabaseChange(name = "sql",
        description = "Allows you to specify raw SQL to execute against the database",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class RawSQLChange extends AbstractSQLChange {

    @Setter
    private String comment;

    private Boolean rerunnable;
    
    public RawSQLChange() {
    }

    public RawSQLChange(String sql) {
        setSql(sql);
    }

    @Override
    @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE, exampleValue = "insert into person (name) values ('Bob')", requiredForDatabase = "all")
    public String getSql() {
        return super.getSql();
    }

    @DatabaseChangeProperty(serializationType = SerializationType.NESTED_OBJECT, description = "A brief descriptive inline comment. Not stored in the database",
        exampleValue = "What about Bob?")
    public String getComment() {
        return comment;
    }

    @Override
    public String getConfirmationMessage() {
        return "Custom SQL executed";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        String nestedSql = StringUtil.trimToNull(parsedNode.getValue(String.class));
        if (nestedSql != null) {
            setSql(nestedSql);
        }
    }

    public boolean isRerunnable() {
        if (rerunnable == null) {
            return false;
        }
        return rerunnable;
    }

    public void setRerunnable(Boolean rerunnable) {
        if (rerunnable == null) {
            this.rerunnable = false;
        }
        this.rerunnable = rerunnable;
    }
}
