package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.change.AddColumnConfig;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Index;

public class CreateIndexAction extends AbstractAction {
    
        public ObjectName indexName;
        public ObjectName tableName;
        public Index.IndexedColumn[] columns;
        public String tablespace;
        public Boolean unique;
        public Boolean clustered;
}
