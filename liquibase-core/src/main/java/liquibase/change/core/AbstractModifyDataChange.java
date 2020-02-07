package liquibase.change.core;

import liquibase.change.AbstractTableChange;
import liquibase.change.Param;
import liquibase.change.DatabaseChangeProperty;

import java.util.ArrayList;
import java.util.List;

import static liquibase.change.ChangeParameterMetaData.ALL;

/**
 * Encapsulates common fields for update and delete changes.
 */
public abstract class AbstractModifyDataChange extends AbstractTableChange {

    protected List<Param> whereParams = new ArrayList<>();

    protected String where;

    @DatabaseChangeProperty( supportsDatabase = ALL,
        description="Allows to define the 'where' condition(s) string",
        serializationType = SerializationType.NESTED_OBJECT, exampleValue = "name='Bob' and :name=:value or id=:value")
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

    public void addWhereParam(Param param) { whereParams.add(param); }

    public void removeWhereParam(Param param) {
        whereParams.remove(param);
    }

    @DatabaseChangeProperty( supportsDatabase = ALL, serializationType = SerializationType.NESTED_OBJECT,
        description = "Parameters for the 'where' condition.\n\nThe 'param'(s) are inserted in the order they " +
                "are defined in place of the <code>:name</code> and <code>:value</code> placeholders. See generated " +
                "SQL Sample below")
    public List<Param> getWhereParams() { return whereParams; }
    public void setWhereParams(List<Param> params) { this.whereParams = params; }
}
