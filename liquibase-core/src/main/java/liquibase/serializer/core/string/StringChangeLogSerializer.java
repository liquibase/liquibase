package liquibase.serializer.core.string;

import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.ChangeSet;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class StringChangeLogSerializer implements ChangeLogSerializer {

    private static final int INDENT_LENGTH = 4;

    private FieldFilter fieldFilter;

    public StringChangeLogSerializer() {
        this(new FieldFilter());
    }

    public StringChangeLogSerializer(FieldFilter fieldFilter) {
        this.fieldFilter = fieldFilter;
    }

    @Override
    public String[] getValidFileExtensions() {
        return new String[]{"txt"};
    }

    @Override
    public String serialize(LiquibaseSerializable object, boolean pretty) {
        return object.getSerializedObjectName() + ":" + serializeObject(object, 1);
    }

    private String serializeObject(LiquibaseSerializable objectToSerialize, int indent) {
        try {
            StringBuilder buffer = new StringBuilder();
            buffer.append("[");

            SortedSet<String> values = new TreeSet<>();
            for (String field : objectToSerialize.getSerializableFields()) {
                Object value = objectToSerialize.getSerializableFieldValue(field);
                if (value == null) {
                    continue;
                }
                if (!fieldFilter.include(objectToSerialize, field, value)) {
                    continue;
                }
                if (value instanceof LiquibaseSerializable) {
                    values.add(indent(indent) + serializeObject((LiquibaseSerializable) value, indent + 1));
                } else {
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
                                    valueString = valueString.replaceFirst("(\\.[0-9]+)0+$","$1");
                                    valueString = valueString.replaceFirst("\\.0+$", "");
                                }
                            }
                            values.add(indent(indent) + field + "=\"" + valueString + "\"");
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
            if (object instanceof LiquibaseSerializable) {
                returnString.append(indent(indent)).append(serializeObject((LiquibaseSerializable) object, indent + 1)).append(",\n");
            } else {
                returnString.append(indent(indent)).append(object.toString()).append(",\n");
            }
        }
        returnString = new StringBuilder(returnString.toString().replaceFirst(",$", ""));
        returnString.append(indent(indent - 1)).append("]");

        return returnString.toString();

    }

    private String serializeObject(Collection collection, int indent) {
        if (collection.isEmpty()) {
            return "[]";
        }

        StringBuilder returnString = new StringBuilder("[\n");
        for (Object object : collection) {
            if (object instanceof LiquibaseSerializable) {
                returnString.append(indent(indent)).append(serializeObject((LiquibaseSerializable) object, indent + 1)).append(",\n");
            } else {
                returnString.append(indent(indent)).append(object.toString()).append(",\n");
            }
        }
        returnString = new StringBuilder(returnString.toString().replaceFirst(",$", ""));
        returnString.append(indent(indent - 1)).append("]");

        return returnString.toString();

    }

    private String serializeObject(Map collection, int indent) {
        if (collection.isEmpty()) {
            return "[]";
        }

        StringBuilder returnString = new StringBuilder("{\n");
        for (Object key : new TreeSet(collection.keySet())) {
            returnString
                .append(indent(indent))
                .append(key.toString())
                .append("=\"")
                .append(collection.get(key))
                .append("\",\n");
        }
        return String.format("%s%s}",
            returnString.toString().replaceFirst(",$", ""),
            indent(indent - 1)
        );
    }

    @Override
    public <T extends ChangeLogChild> void write(List<T> children, OutputStream out) throws IOException {

    }

    @Override
    public void append(ChangeSet changeSet, File changeLogFile) throws IOException {

    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public static class FieldFilter {
        public boolean include(Object obj, String field, Object value) {
            return true;
        }
    }
}
