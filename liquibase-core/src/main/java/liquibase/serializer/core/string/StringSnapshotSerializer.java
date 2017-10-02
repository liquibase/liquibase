package liquibase.serializer.core.string;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.SnapshotSerializer;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class StringSnapshotSerializer implements SnapshotSerializer {

    private static final int INDENT_LENGTH = 4;

    @Override
    public String[] getValidFileExtensions() {
        return new String[]{"checksum"};
    }

    @Override
    public String serialize(LiquibaseSerializable object, boolean pretty) {
        return object.getSerializedObjectName() + ":" + serializeObject(object, 1);
    }

    private String serializeObject(LiquibaseSerializable objectToSerialize, int indent) {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("[");

            SortedSet<String> values = new TreeSet<>();
            for (String field : objectToSerialize.getSerializableFields()) {
                Object value = objectToSerialize.getSerializableFieldValue(field);

                if (value instanceof LiquibaseSerializable) {
                    values.add(indent(indent) + serializeObject((LiquibaseSerializable) value, indent + 1));
                } else {
                    if (value != null) {
                        if (value instanceof Map) {
                            values.add(indent(indent) + field + "=" + serializeObject((Map) value, indent + 1));
                        } else if (value instanceof Collection) {
                            values.add(indent(indent) + field + "=" + serializeObject((Collection) value, indent + 1));
                        } else if (value instanceof Object[]) {
                            values.add(indent(indent) + field + "=" + serializeObject((Object[]) value, indent + 1));
                        } else {
                            String valueString = value.toString();
                            if ((value instanceof Double) || (value instanceof Float)) { //java 6 adds additional zeros to the end of doubles and floats
                                if (valueString.contains(".")) {
                                    valueString = valueString.replaceFirst("0*$","");
                                }
                            }
                            values.add(indent(indent) + field + "=\"" + valueString + "\"");
                        }
                    }
                }
            }

            if (!values.isEmpty()) {
                buffer.append("\n");
                buffer.append(StringUtils.join(values, "\n"));
                buffer.append("\n");
            }
            buffer.append(indent(indent - 1)).append("]");
            return buffer.toString().replace("\r\n", "\n").replace("\r", "\n"); //standardize all newline chars

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    private String indent(int indent) {
        return StringUtils.repeat(" ", INDENT_LENGTH * indent);
    }

    private String serializeObject(Object[] collection, int indent) {
        if (collection.length == 0) {
            return "[]";
        }

        String returnString = "[\n";
        for (Object object : collection) {
            if (object instanceof LiquibaseSerializable) {
                returnString += indent(indent) + serializeObject((LiquibaseSerializable) object, indent + 1) + ",\n";
            } else {
                returnString += indent(indent) + object.toString() + ",\n";
            }
        }
        returnString = returnString.replaceFirst(",$", "");
        returnString += indent(indent - 1) + "]";

        return returnString;

    }

    private String serializeObject(Collection collection, int indent) {
        if (collection.isEmpty()) {
            return "[]";
        }

        String returnString = "[\n";
        for (Object object : collection) {
            if (object instanceof LiquibaseSerializable) {
                returnString += indent(indent) + serializeObject((LiquibaseSerializable) object, indent + 1) + ",\n";
            } else {
                returnString += indent(indent) + object.toString() + ",\n";
            }
        }
        returnString = returnString.replaceFirst(",$", "");
        returnString += indent(indent - 1) + "]";

        return returnString;

    }

    private String serializeObject(Map collection, int indent) {
        if (collection.isEmpty()) {
            return "[]";
        }

        String returnString = "{\n";
        TreeSet sortedCollection = new TreeSet(new Comparator() {
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
        sortedCollection.addAll(collection.keySet());
        for (Object key : sortedCollection) {
            returnString += indent(indent) + key.toString() + "=\"" + collection.get(key) + "\",\n";
        }
        returnString = returnString.replaceFirst(",$", "");
        returnString += indent(indent - 1) + "}";

        return returnString;

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
