package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

import java.util.List;

public class DropColumnsAction extends AbstractAction {
        public ObjectReference tableName;
        public List<String> columnNames;
}
