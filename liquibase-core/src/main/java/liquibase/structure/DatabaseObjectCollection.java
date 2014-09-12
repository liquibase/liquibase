package liquibase.structure;

import liquibase.database.Database;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.NamespaceDetails;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;

import java.util.*;

public class DatabaseObjectCollection implements LiquibaseSerializable {

    private Map<Class<? extends DatabaseObject>, Map<String, Set<DatabaseObject>>> cache = new HashMap<Class<? extends DatabaseObject>, Map<String, Set<DatabaseObject>>>();
    private Database database;

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
        SortedSet<String> types = new TreeSet<String>();
        for (Class type : cache.keySet()) {
            types.add(type.getName());
        }
        return types;

    }

    @Override
    public Object getSerializableFieldValue(String field) {
        SortedSet<DatabaseObject> objects = new TreeSet<DatabaseObject>(new DatabaseObjectComparator());
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
        Map<String, Set<DatabaseObject>> collectionMap = cache.get(databaseObject.getClass());
        if (collectionMap == null) {
            collectionMap = new HashMap<String, Set<DatabaseObject>>();
            cache.put(databaseObject.getClass(), collectionMap);
        }

        String[] hashes = DatabaseObjectComparatorFactory.getInstance().hash(databaseObject, database);

        for (String hash : hashes) {
            Set<DatabaseObject> collection = collectionMap.get(hash);
            if (collection == null) {
                collection = new HashSet<DatabaseObject>();
                collectionMap.put(hash, collection);
            }
            collection.add(databaseObject);
        }
    }

    /**
     * Returns the object described by the passed example if it is already included in this snapshot.
     */
    public <DatabaseObjectType extends DatabaseObject> DatabaseObjectType get(DatabaseObjectType example) {
        Map<String, Set<DatabaseObject>> databaseObjectsByHash = cache.get(example.getClass());

        if (databaseObjectsByHash == null) {
            return null;
        }

        String[] hashes = DatabaseObjectComparatorFactory.getInstance().hash(example, database);

        SortedSet<Set<DatabaseObject>> objectSets = new TreeSet<Set<DatabaseObject>>(new Comparator<Set<DatabaseObject>>() {
            @Override
            public int compare(Set<DatabaseObject> o1, Set<DatabaseObject> o2) {
                int sizeComparison = Integer.valueOf(o1.size()).compareTo(o2.size());
                if (sizeComparison == 0) {
                    return o1.toString().compareTo(o2.toString());
                }
                return sizeComparison;
            }
        } );

        for (String hash : hashes) {
            Set<DatabaseObject> databaseObjects = databaseObjectsByHash.get(hash);
            if (databaseObjects != null) {
                objectSets.add(databaseObjects);
            }
        }

        for (Set<DatabaseObject> databaseObjects : objectSets) {
            for (DatabaseObject obj : databaseObjects) {
                if (DatabaseObjectComparatorFactory.getInstance().isSameObject(obj, example, database)) {
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
    public <DatabaseObjectType extends  DatabaseObject> Set<DatabaseObjectType> get(Class<DatabaseObjectType> type) {

        Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();

        Map<String, Set<DatabaseObject>> allFound = cache.get(type);
        if (allFound != null) {
            for (Set<DatabaseObject> objects : allFound.values()) {
                returnSet.addAll(objects);
            }
        }

        return (Set<DatabaseObjectType>) Collections.unmodifiableSet(returnSet);
    }


    public boolean contains(DatabaseObject wantedObject) {
        return get(wantedObject) != null;
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
