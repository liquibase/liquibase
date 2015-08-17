package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;
import liquibase.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DropTablesAction extends AbstractAction {
    public List<ObjectName> tableNames = new ArrayList<>();
    public Boolean cascadeConstraints;

    public DropTablesAction() {
    }

    public DropTablesAction(ObjectName... tableNames) {
        this.tableNames.addAll(Arrays.asList(CollectionUtil.createIfNull(tableNames)));
    }


}
