package liquibase.structure;

import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectCollectionComparator;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseObjectCollection implements LiquibaseSerializable {

    private final Map<Class<? extends DatabaseObject>, Map<String, Set<DatabaseObject>>> cache = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Database database;

    public DatabaseObjectCollection(Database database) {
        this.database = database;
    }

    @Override
    public String getSerializedObjectName() {
        return "objects";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_SNAPSHOT_NAMESPACE;
    }

    @Override
    public String getSerializableFieldNamespace(String field) {
        return getSerializedObjectNamespace();
    }


    @Override
    public Set<String> getSerializableFields() {
        SortedSet<String> types = new TreeSet<>();
        for (Class type : cache.keySet()) {
            types.add(type.getName());
        }
        return types;

    }

    @Override
    public Object getSerializableFieldValue(String field) {
        SortedSet<DatabaseObject> objects = new TreeSet<>(new DatabaseObjectCollectionComparator());
        try {
            Map<String, Set<DatabaseObject>> map = cache.get(Class.forName(field));
            if (map == null) {
                return null;
            }
            for (Set<DatabaseObject> set : map.values()) {
                objects.addAll(set);
            }
            return objects;
        } catch (ClassNotFoundException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        return SerializationType.NAMED_FIELD;
    }

    public void add(DatabaseObject databaseObject) {
        if (databaseObject == null) {
            return;
        }
        Map<String, Set<DatabaseObject>> collectionMap = cache.computeIfAbsent(databaseObject.getClass(), k -> new ConcurrentHashMap<>());

        String[] hashes = DatabaseObjectComparatorFactory.getInstance().hash(databaseObject, null, database);

        for (String hash : hashes) {
            Set<DatabaseObject> collection = collectionMap.computeIfAbsent(hash, k -> new HashSet<>());
            collection.add(databaseObject);
        }
    }

    /**
     * Returns the object described by the passed example if it is already included in this snapshot.
     */
    public <DatabaseObjectType extends DatabaseObject> DatabaseObjectType get(DatabaseObjectType example, CompareControl.SchemaComparison[] schemaComparisons) {
        Map<String, Set<DatabaseObject>> databaseObjectsByHash = cache.get(example.getClass());

        if (databaseObjectsByHash == null) {
            return null;
        }

        String[] hashes = DatabaseObjectComparatorFactory.getInstance().hash(example, null, database);

        SortedSet<Set<DatabaseObject>> objectSets = new TreeSet<>((o1, o2) -> {
            int sizeComparison = Integer.compare(o1.size(), o2.size());
            if (sizeComparison == 0) {
                return o1.toString().compareTo(o2.toString());
            }
            return sizeComparison;
        });

        for (String hash : hashes) {
            Set<DatabaseObject> databaseObjects = databaseObjectsByHash.get(hash);
            if (databaseObjects != null) {
                objectSets.add(databaseObjects);
            }
        }

        for (Set<DatabaseObject> databaseObjects : objectSets) {
            for (DatabaseObject obj : databaseObjects) {
                if (DatabaseObjectComparatorFactory.getInstance().isSameObject(obj, example, schemaComparisons, database)) {
                    //noinspection unchecked
                    return (DatabaseObjectType) obj;
                }
            }
        }

        return null;
    }

    /**
     * Returns all objects of the given type that are already included in this snapshot.
     */
    public <DatabaseObjectType extends DatabaseObject> Set<DatabaseObjectType> get(Class<DatabaseObjectType> type) {

        Set<DatabaseObject> returnSet = new HashSet<>();

        Map<String, Set<DatabaseObject>> allFound = cache.get(type);
        if (allFound != null) {
            for (Set<DatabaseObject> objects : allFound.values()) {
                returnSet.addAll(objects);
            }
        }

        return (Set<DatabaseObjectType>) Collections.unmodifiableSet(returnSet);
    }


    public boolean contains(DatabaseObject wantedObject, CompareControl.SchemaComparison[] schemaComparisons) {
        return get(wantedObject, schemaComparisons) != null;
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        throw new RuntimeException("TODO");
    }

    @Override
    public ParsedNode serialize() {
        throw new RuntimeException("TODO");
    }

    public Map<Class<? extends DatabaseObject>, Set<? extends DatabaseObject>> toMap() {
        Map<Class<? extends DatabaseObject>, Set<? extends DatabaseObject>> returnMap =
            Collections.synchronizedMap(new LinkedHashMap<>());
        for (Class<? extends DatabaseObject> type : this.cache.keySet()) {
            returnMap.put(type, get(type));
        }

        return returnMap;
    }

}
