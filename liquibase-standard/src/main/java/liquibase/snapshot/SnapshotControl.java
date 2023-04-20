package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DatabaseObjectFactory;

import java.util.*;

/**
 * Allows the class user to influence various aspects of the database object snapshot generation, e.g.
 * what types of database objects they want.
 */
public class SnapshotControl implements LiquibaseSerializable {

    private Set<Class<? extends DatabaseObject>> types;
    private ObjectChangeFilter objectChangeFilter;
    private SnapshotListener snapshotListener;
    private boolean warnIfObjectNotFound = true;
    
    
    /**
     * Create a SnapshotControl for a given database and mark the database's standard types for inclusion.
     * @param database the DBMS for which snapshots should be generated
     */
    public SnapshotControl(Database database) {
        setTypes(DatabaseObjectFactory.getInstance().getStandardTypes(), database);
    }
    
    /**
     * Create a Snapshot control for a given database, but explicitly set the object types to be included in snapshots.
     * @param database the DBMS for which snapshots should be generated
     * @param types the list of object types to be included in the snapshot
     */
    public SnapshotControl(Database database, Class<? extends DatabaseObject>... types) {
        this(database, true, types);
    }

    public SnapshotControl(Database database, boolean expandTypesIfNeeded, Class<? extends DatabaseObject>... types) {
        if ((types == null) || (types.length == 0)) {
            setTypes(DatabaseObjectFactory.getInstance().getStandardTypes(), database);
        } else {
            if (expandTypesIfNeeded) {
                setTypes(new HashSet<>(Arrays.asList(types)), database);
            } else {
                this.types = new HashSet<>(Arrays.asList(types));
            }
        }
    }
    
    /**
     * Create a Snapshot control for a given database, but explicitly set the object types to be included in snapshots.
     * @param database the DBMS for which snapshots should be generated
     * @param types the list of object types to be included in the snapshot, separated by commas
     */
    public SnapshotControl(Database database, String types) {
        setTypes(DatabaseObjectFactory.getInstance().parseTypes(types), database);
    }

    public SnapshotControl(Database database, ObjectChangeFilter objectChangeFilter, Class<? extends
            DatabaseObject>... types) {
        this(database, true, types);

        this.objectChangeFilter = objectChangeFilter;
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
        return new HashSet<>(Arrays.asList("includedType"));
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        if ("includedType".equals(field)) {
            SortedSet<String> types = new TreeSet<>();
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
        if ("includedType".equals(field)) {
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
        this.types = new HashSet<>();
        for (Class<? extends DatabaseObject> type : types) {
            addType(type, database);
        }
    }
    
    /**
     * Adds a new DatabaseObject type to the list of object types to be included in snapshots.
     * @param type The type to be added
     * @param database The database to check for any dependent types that need to be included as well
     * @return true if the type was added to the list, false if it was already present.
     */
    public boolean addType(Class<? extends DatabaseObject> type, Database database) {
        boolean added = this.types.add(type);
        if (added) {
            for (Class<? extends DatabaseObject> container : SnapshotGeneratorFactory.getInstance().getContainerTypes(type, database)) {
                addType(container, database);
            }
        }
        return added;
    }
    
    /**
     * Return the types to be included in snapshots
     * @return the set of currently registered types
     */
    public Set<Class<? extends DatabaseObject>> getTypesToInclude() {
        return types;
    }
    
    /**
     * Queries the currently registered list of types to be included and returns true if the given type is in that list
     * @param type the DatabaseObject type to be checked
     * @return true if that type is registered for inclusion, false if not
     */
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
    
    /**
     * Returns if the code should log a LogLevel.WARNING message if the object to be snapshotted could not be found.
     * @return true if WARNINGs should be emitted (default), false if not.
     */
    public boolean isWarnIfObjectNotFound() {
        return warnIfObjectNotFound;
    }
    
    /**
     * Configures the code to log a LogLevel.WARNING message if the object to be snapshotted could not be found.
     * @param warnIfObjectNotFound true if a warning should emitted (default value), false if not.
     */
    public SnapshotControl setWarnIfObjectNotFound(boolean warnIfObjectNotFound) {
        this.warnIfObjectNotFound = warnIfObjectNotFound;
        return this;
    }

    public <T extends DatabaseObject> boolean shouldInclude(T example) {
        if (objectChangeFilter != null) {
            return objectChangeFilter.include(example);
        }
        return shouldInclude(example.getClass());
    }
}
