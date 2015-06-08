package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.change.AddColumnConfig;
import liquibase.structure.ObjectName;

public class CreateIndexAction extends AbstractAction {
    
        public ObjectName indexName;
        public ObjectName tableName;
        public ColumnDefinition[] columnDefinitions;
        public String tablespace;
        public Boolean unique;
        public Boolean clustered;
}
