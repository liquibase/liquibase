package liquibase.structure.core;

import liquibase.structure.ObjectName;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Table extends Relation {

    public Table() {
    }

    public Table(ObjectName name) {
        super(name);
    }

    public Table(String catalogName, String schemaName, String tableName) {
        this(new ObjectName(catalogName, schemaName, tableName));
    }
}
