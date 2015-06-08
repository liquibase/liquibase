package liquibase.structure.core;

import liquibase.structure.ObjectName;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Table extends Relation {

    public PrimaryKey primaryKey;
    public Set<ForeignKey> outgoingForeignKeys;
    public Set<Index> indexes;
    public Set<UniqueConstraint> uniqueConstraints;

    public Table() {
    }

    public Table(String name) {
        super(name);
    }

    public Table(ObjectName name) {
        super(name);
    }

    public Table(String catalogName, String schemaName, String tableName) {
        this.setSchema(new Schema(catalogName, schemaName));
        setName(new ObjectName(catalogName, schemaName, tableName));
    }
}
