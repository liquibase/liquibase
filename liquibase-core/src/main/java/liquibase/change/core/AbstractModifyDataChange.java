package liquibase.change.core;

import liquibase.change.AbstractTableChange;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChangeProperty;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;

import java.util.ArrayList;
import java.util.List;

import static liquibase.change.ChangeParameterMetaData.ALL;

/**
 * Encapsulates common fields for update and delete changes.
 */
public abstract class AbstractModifyDataChange extends AbstractTableChange {

    protected List<ColumnConfig> whereParams = new ArrayList<>();

    protected String where;

    @DatabaseChangeProperty( supportsDatabase = ALL,
        description="Allows to define the 'where' condition as string",
        serializationType = SerializationType.NESTED_OBJECT, exampleValue = "name='Bob'")
    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    /** @deprecated use getWhere().  */
    @DatabaseChangeProperty(isChangeProperty = false)
    public String getWhereClause() {
        return where;
    }

    /** @deprecated use setWhere()  */
    public void setWhereClause(String where) {
        this.where = where;
    }

    public void addWhereParam(ColumnConfig param) { whereParams.add(param); }

    public void removeWhereParam(ColumnConfig param) {
        whereParams.remove(param);
    }

    @DatabaseChangeProperty( supportsDatabase = ALL, serializationType = SerializationType.NESTED_OBJECT,
        description = "Parameters for the 'where' condition. The 'param'(s) are inserted in the order they are " +
                    "defined in place of ':name' and ':value' placeholders.")
    public List<ColumnConfig> getWhereParams() { return whereParams; }
    public void setWhereParams(List<ColumnConfig> params) { this.whereParams = params; }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        ParsedNode whereParams = parsedNode.getChild(null, "whereParams");
        if (whereParams != null) {
            for (ParsedNode param : whereParams.getChildren(null, "param")) {
                ColumnConfig columnConfig = new ColumnConfig();
                try {
                    columnConfig.load(param, resourceAccessor);
                } catch (ParsedNodeException e) {
                    e.printStackTrace();
                }
                addWhereParam(columnConfig);
            }
        }
    }
}
