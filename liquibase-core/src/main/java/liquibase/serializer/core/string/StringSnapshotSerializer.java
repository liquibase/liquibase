package liquibase.serializer.core.string;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.SnapshotSerializer;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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

    private String serializeObject(Object object, int indent) {
        return object instanceof LiquibaseSerializable
            ? serializeObject((LiquibaseSerializable) object, indent)
            : object.toString();
    }

    private String serializeObject(LiquibaseSerializable objectToSerialize, int indent) {
        try {
            StringBuilder buffer = new StringBuilder();
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
                buffer.append(StringUtil.join(values, "\n"));
                buffer.append("\n");
            }
            buffer.append(indent(indent - 1)).append("]");
            return buffer.toString().replace("\r\n", "\n").replace("\r", "\n"); //standardize all newline chars

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    private String indent(int indent) {
        return StringUtil.repeat(" ", INDENT_LENGTH * indent);
    }

    private String serializeObject(Object[] collection, int indent) {
        if (collection.length == 0) {
            return "[]";
        }

        StringBuilder returnString = new StringBuilder("[\n");
        for (Object object : collection) {
            returnString
                .append(indent(indent))
                .append(serializeObject(object, indent + 1))
                .append(",\n");
        }
        String result = returnString.toString().replaceFirst(",$", "");
        return result + indent(indent - 1) + "]";

    }

    private String serializeObject(Collection collection, int indent) {
        if (collection.isEmpty()) {
            return "[]";
        }

        StringBuilder returnString = new StringBuilder("[\n");
        for (Object object : collection) {
            returnString.append(indent(indent));
            returnString.append(serializeObject(object, indent + 1));
            returnString.append(",\n");
        }
        String result = returnString.toString().replaceFirst(",$", "");
        return result + indent(indent - 1) + "]";

    }

    private String serializeObject(Map collection, int indent) {
        if (collection.isEmpty()) {
            return "[]";
        }

        StringBuilder returnString = new StringBuilder("{\n");
        TreeSet sortedCollection = new TreeSet((o1, o2) -> {
            if (o1 instanceof Comparable) {
                return ((Comparable) o1).compareTo(o2);
            } else if (o1 instanceof Class) {
                return ((Class) o1).getName().compareTo(((Class) o2).getName());
            } else {
                throw new ClassCastException(o1.getClass().getName()+" cannot be cast to java.lang.Comparable or java.lang.Class");
            }
        });
        sortedCollection.addAll(collection.keySet());
        for (Object key : sortedCollection) {
            returnString
                .append(indent(indent))
                .append(key.toString())
                .append("=\"")
                .append(collection.get(key))
                .append("\",\n");
        }
        String result = returnString.toString().replaceFirst(",$", "");
        return result + indent(indent - 1) + "}";

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
