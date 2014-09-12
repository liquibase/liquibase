package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DatabaseObjectFactory;

import java.util.*;

public class SnapshotControl implements LiquibaseSerializable {

    private Set<Class<? extends DatabaseObject>> types;
    private SnapshotListener snapshotListener;

    public SnapshotControl(Database database) {
        setTypes(DatabaseObjectFactory.getInstance().getStandardTypes(), database);
    }

    public SnapshotControl(Database database, Class<? extends DatabaseObject>... types) {
        if (types == null || types.length == 0) {
            setTypes(DatabaseObjectFactory.getInstance().getStandardTypes(), database);
        } else {
            setTypes(new HashSet<Class<? extends DatabaseObject>>(Arrays.asList(types)), database);
        }
    }

    public SnapshotControl(Database database, String types) {
        setTypes(DatabaseObjectFactory.getInstance().parseTypes(types), database);
    }

    public SnapshotListener getSnapshotListener() {
        return snapshotListener;
    }

    public void setSnapshotListener(SnapshotListener snapshotListener) {
        this.snapshotListener = snapshotListener;
    }

    @Override
    public String getSerializedObjectName() {
        return "snapshotControl";
    }

    @Override
    public Set<String> getSerializableFields() {
        return new HashSet<String>(Arrays.asList("includedType"));
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        if (field.equals("includedType")) {
            SortedSet<String> types = new TreeSet<String>();
            for (Class type : this.getTypesToInclude()) {
                types.add(type.getName());
            }
            return types;
        } else {
            throw new UnexpectedLiquibaseException("Unknown field "+field);
        }
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        if (field.equals("includedType")) {
            return SerializationType.NESTED_OBJECT;
        } else {
            throw new UnexpectedLiquibaseException("Unknown field "+field);
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_SNAPSHOT_NAMESPACE;
    }

    @Override
    public String getSerializableFieldNamespace(String field) {
        return getSerializedObjectNamespace();
    }

    private void setTypes(Set<Class<? extends DatabaseObject>> types, Database database) {
        this.types = new HashSet<Class<? extends DatabaseObject>>();
        for (Class<? extends DatabaseObject> type : types) {
            addType(type, database);
        }
    }

    public boolean addType(Class<? extends DatabaseObject> type, Database database) {
        boolean added = this.types.add(type);
        if (added) {
            for (Class<? extends DatabaseObject> container : SnapshotGeneratorFactory.getInstance().getContainerTypes(type, database)) {
                addType(container, database);
            }
        }

        return added;

    }

    public Set<Class<? extends DatabaseObject>> getTypesToInclude() {
        return types;
    }

    public boolean shouldInclude(Class<? extends DatabaseObject> type) {
        return types.contains(type);
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        throw new RuntimeException("TODO");
    }

    @Override
    public ParsedNode serialize() {
        throw new RuntimeException("TODO");
    }

}
