package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeleteDataAction extends AbstractAction {
    public ObjectReference tableName;
    public StringClauses where;
    public List<Object> whereParameters;
    public List<String> whereColumnNames;

    public DeleteDataAction() {
    }

    public DeleteDataAction(ObjectReference tableName) {
        this.tableName = tableName;
    }

    public DeleteDataAction addWhereParameters(Object... parameters) {
        if (parameters != null) {
            if (!CollectionUtil.hasValue(whereParameters)) {
                whereParameters = new ArrayList<>();
            }

            Collections.addAll(this.whereParameters, parameters);
        }

        return this;
    }

}
