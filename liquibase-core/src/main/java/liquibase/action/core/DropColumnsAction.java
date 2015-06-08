package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

import java.util.List;

public class DropColumnsAction extends AbstractAction {
        public ObjectName tableName;
        public List<String> columnNames;
}
