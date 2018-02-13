package liquibase.serializer.core.string;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.SnapshotSerializer;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.structure.CatalogLevelObject;
import liquibase.structure.DatabaseLevelObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class StringSnapshotSerializerReadable implements SnapshotSerializer {

    private static final int INDENT_LENGTH = 4;

    @Override
    public String[] getValidFileExtensions() {
        return new String[]{"txt"};
    }

    @Override
    public String serialize(LiquibaseSerializable object, boolean pretty) {
        try {
            StringBuilder buffer = new StringBuilder();
            DatabaseSnapshot snapshot = ((DatabaseSnapshot) object);
            Database database = snapshot.getDatabase();

            buffer.append("Database snapshot for ").append(database.getConnection().getURL()).append("\n");
            addDivider(buffer);
            buffer.append("Database type: ").append(database.getDatabaseProductName()).append("\n");
            buffer.append("Database version: ").append(database.getDatabaseProductVersion()).append("\n");
            buffer.append("Database user: ").append(database.getConnection().getConnectionUserName()).append("\n");

            SnapshotControl snapshotControl = snapshot.getSnapshotControl();
            List<Class> includedTypes = sort(snapshotControl.getTypesToInclude());

            buffer.append("Included types:\n" ).append(StringUtil.indent(StringUtil.join(includedTypes, "\n", new StringUtil.StringUtilFormatter<Class>() {
                @Override
                public String toString(Class obj) {
                    return obj.getName();
                }
            }))).append("\n");


            List<Schema> schemas = sort(snapshot.get(Schema.class), new Comparator<Schema>() {
                @Override
                public int compare(Schema o1, Schema o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });

            for (Schema schema : schemas) {
                if (database.supportsSchemas()) {
                    buffer.append("\nCatalog & Schema: ").append(schema.getCatalogName()).append(" / ").append(schema.getName()).append("\n");
                } else {
                    buffer.append("\nCatalog: ").append(schema.getCatalogName()).append("\n");
                }

                StringBuilder catalogBuffer = new StringBuilder();
                for (Class type : includedTypes) {
                    if (type.equals(Schema.class) || type.equals(Catalog.class) || type.equals(Column.class)) {
                        continue;
                    }
                    List<DatabaseObject> objects = new ArrayList<DatabaseObject>(snapshot.get(type));
                    ListIterator<DatabaseObject> iterator = objects.listIterator();
                    while (iterator.hasNext()) {
                        DatabaseObject next = iterator.next();
                        if (next instanceof DatabaseLevelObject) {
                            continue;
                        }

                        Schema objectSchema = next.getSchema();
                        if (objectSchema == null) {
                            if (!(next instanceof CatalogLevelObject) || !((CatalogLevelObject) next).getCatalog().equals(schema.getCatalog())) {
                                iterator.remove();
                            }
                        } else if (!objectSchema.equals(schema)) {
                            iterator.remove();
                        }
                    }
                    outputObjects(objects, type, catalogBuffer);
                }
                buffer.append(StringUtil.indent(catalogBuffer.toString(), INDENT_LENGTH));

            }

            return buffer.toString().replace("\r\n", "\n").replace("\r", "\n"); //standardize all newline chars

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    protected void outputObjects(List objects, Class type, StringBuilder catalogBuffer) {
        List<? extends DatabaseObject> databaseObjects = sort(objects);
        if (!databaseObjects.isEmpty()) {
            catalogBuffer.append(type.getName()).append(":\n");

            StringBuilder typeBuffer = new StringBuilder();
            for (DatabaseObject databaseObject : databaseObjects) {
                typeBuffer.append(databaseObject.getName()).append("\n");
                typeBuffer.append(StringUtil.indent(serialize(databaseObject, null), INDENT_LENGTH)).append("\n");
            }

            catalogBuffer.append(StringUtil.indent(typeBuffer.toString(), INDENT_LENGTH)).append("\n");
        }
    }

    private String serialize(final DatabaseObject databaseObject, final DatabaseObject parentObject) {

        StringBuilder buffer = new StringBuilder();

        final List<String> attributes = sort(databaseObject.getAttributes());
        for (String attribute : attributes) {
            if ("name".equals(attribute)) {
                continue;
            }
            if ("schema".equals(attribute)) {
                continue;
            }
            if ("catalog".equals(attribute)) {
                continue;
            }
            Object value = databaseObject.getAttribute(attribute, Object.class);

            if (value instanceof Schema) {
                continue;
            }

            if (value instanceof DatabaseObject) {
                if (
                        (parentObject != null)
                        && ((DatabaseObject) value).getSnapshotId() != null
                        && ((DatabaseObject) value).getSnapshotId().equals(parentObject.getSnapshotId())
                   ) {
                    continue;
                }

                boolean expandContainedObjects = shouldExpandNestedObject(value, databaseObject);

                if (expandContainedObjects) {
                    value = ((DatabaseObject) value).getName()+"\n"+ StringUtil.indent(serialize((DatabaseObject) value, databaseObject), INDENT_LENGTH);
                } else {
                    value = databaseObject.getSerializableFieldValue(attribute);
                }
            } else if (value instanceof Collection) {
                if (((Collection) value).isEmpty()) {
                    value = null;
                } else {
                    if (((Collection) value).iterator().next() instanceof DatabaseObject) {
                        value = StringUtil.join(new TreeSet<>((Collection<DatabaseObject>) value), "\n", new StringUtil.StringUtilFormatter() {
                            @Override
                            public String toString(Object obj) {
                                if (obj instanceof DatabaseObject) {
                                    if (shouldExpandNestedObject(obj, databaseObject)) {
                                        return ((DatabaseObject) obj).getName()+"\n"+ StringUtil.indent(serialize(((DatabaseObject) obj), databaseObject), INDENT_LENGTH);
                                    } else {
                                        return ((DatabaseObject) obj).getName();
                                    }
                                } else {
                                    return obj.toString();
                                }
                            }
                        });
                        value = "\n"+ StringUtil.indent((String) value, INDENT_LENGTH);
                    } else {
                        value = databaseObject.getSerializableFieldValue(attribute);
                    }
                }
            } else {
                value = databaseObject.getSerializableFieldValue(attribute);
            }
            if (value != null) {
                buffer.append(attribute).append(": ").append(value).append("\n");
            }
        }

        return buffer.toString().replaceFirst("\n$", "");

    }

    protected boolean shouldExpandNestedObject(Object nestedValue, DatabaseObject container) {
        return (container instanceof Table) || (container instanceof View);
    }

    protected void addDivider(StringBuilder buffer) {
        buffer.append("-----------------------------------------------------------------\n");
    }

    private List sort(Collection objects) {
        return sort(objects, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof Comparable) {
                    return ((Comparable) o1).compareTo(o2);
                } else if (o1 instanceof Class) {
                    return ((Class) o1).getName().compareTo(((Class) o2).getName());
                } else {
                    throw new ClassCastException(o1.getClass().getName()+" cannot be cast to java.lang.Comparable or java.lang.Class");
                }
            }
        });
    }

    private <T> List<T> sort(Collection objects, Comparator<T> comparator) {
        List returnList = new ArrayList(objects);
        Collections.sort(returnList, comparator);

        return returnList;
    }

    @Override
    public void write(DatabaseSnapshot snapshot, OutputStream out) throws IOException {
        out.write(serialize(snapshot, true).getBytes(LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding()));
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
}
