package liquibase.structure.core;

import liquibase.structure.ObjectName;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Table extends Relation {

    public static enum Attr {
        outgoingForeignKeys,
        indexes,
        uniqueConstraints,
    }

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
        setName(tableName);
    }

    public PrimaryKey getPrimaryKey() {
        return get("primaryKey", PrimaryKey.class);
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.set("primaryKey", primaryKey);
    }

    public List<ForeignKey> getOutgoingForeignKeys() {
        return get("outgoingForeignKeys", List.class);
    }

    public List<Index> getIndexes() {
        return get("indexes", List.class);
    }

    public List<UniqueConstraint> getUniqueConstraints() {
        return get("uniqueConstraints", List.class);
    }

}
