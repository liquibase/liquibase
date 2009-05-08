package liquibase.parser.string;

import liquibase.change.Change;
import liquibase.change.ChangeMetaDataField;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.ChangeLogSerializer;
import liquibase.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

public class StringChangeLogSerializer implements ChangeLogSerializer {

    private static final int INDENT_LENGTH = 4;

    public String serialize(Change change) {
        return change.getChangeMetaData().getName() + ":" + serializeObject(change, 1);
    }

    private String serializeObject(Object objectToSerialize, int indent) {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("[");

            SortedSet<String> values = new TreeSet<String>();
            for (Field field : objectToSerialize.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getAnnotation(ChangeMetaDataField.class) != null) {
                    continue;
                }

                String propertyName = field.getName();

                Object value = field.get(objectToSerialize);
                if (value instanceof ColumnConfig) {
                    values.add(indent(indent) + serializeColumnConfig((ColumnConfig) field.get(objectToSerialize), indent + 1));
                } else {
                    if (value != null) {
                        if (value instanceof Collection) {
                            values.add(indent(indent) + propertyName + "=" + serializeObject((Collection) value, indent + 1));
                        } else {
                            values.add(indent(indent) + propertyName + "=\"" + value.toString() + "\"");
                        }
                    }
                }
            }

            if (values.size() > 0) {
                buffer.append("\n");
                buffer.append(StringUtils.join(values, "\n"));
                buffer.append("\n");
            }
            buffer.append(indent(indent - 1)).append("]");
            return buffer.toString();

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    private String indent(int indent) {
        return StringUtils.repeat(" ", INDENT_LENGTH * indent);
    }

    private String serializeObject(Collection collection, int indent) {
        if (collection.size() == 0) {
            return "[]";
        }

        String returnString = "[\n";
        for (Object object : collection) {
            if (object instanceof ColumnConfig) {
                returnString += indent(indent) + serializeColumnConfig((ColumnConfig) object, indent + 1)+",\n";
            } else {
                returnString += indent(indent) + "[" + object.toString() + "],\n";
            }
        }
        returnString = returnString.replaceFirst(",$","");
        returnString += indent(indent - 1) + "]";

        return returnString;

    }

    public String serialize(ColumnConfig columnConfig) {
        return null;
    }

    private String serializeColumnConfig(ColumnConfig columnConfig, int indent) {
        return "column:" + serializeObject(columnConfig, indent);
    }

    public String serialize(ChangeSet changeSet) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
