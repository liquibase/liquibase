package liquibase.serializer.core.string;

import liquibase.change.Change;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.custom.CustomChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.util.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.OutputStream;
import java.io.IOException;

public class StringChangeLogSerializer implements ChangeLogSerializer {

    private static final int INDENT_LENGTH = 4;

    public String[] getValidFileExtensions() {
        return new String[] {"txt"};
    }

    public String serialize(DatabaseChangeLog databaseChangeLog) {
        return null; //todo
    }
    
    public String serialize(Change change) {
        return change.getChangeMetaData().getName() + ":" + serializeObject(change, 1);
    }

    public String serialize(SqlVisitor visitor) {
        return visitor.getName() + ":" + serializeObject(visitor, 1);
    }

    private String serializeObject(Object objectToSerialize, int indent) {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("[");

            SortedSet<String> values = new TreeSet<String>();
            Class<? extends Object> classToCheck = objectToSerialize.getClass();
            while (!classToCheck.equals(Object.class)) {
                for (Field field : classToCheck.getDeclaredFields()) {
                    field.setAccessible(true);
                    DatabaseChangeProperty changePropertyAnnotation = field.getAnnotation(DatabaseChangeProperty.class);
                    if (changePropertyAnnotation != null && !changePropertyAnnotation.includeInSerialization()) {
                        continue;
                    }
                    if (field.getName().equals("serialVersionUID")) {
                        continue;
                    }
                    if (field.isSynthetic() || field.getName().equals("$VRc")) { //from emma
                        continue;
                    }
                    if (field.getName().equals("serialVersionUID")) {
                        continue;
                    }

                    String propertyName = field.getName();

                    Object value = field.get(objectToSerialize);
                    if (value instanceof ColumnConfig) {
                        values.add(indent(indent) + serializeColumnConfig((ColumnConfig) field.get(objectToSerialize), indent + 1));
                    } else if (value instanceof ConstraintsConfig) {
                        values.add(indent(indent) + serializeConstraintsConfig((ConstraintsConfig) field.get(objectToSerialize), indent + 1));
                    } else if (value instanceof CustomChange) {
                        values.add(indent(indent) + serializeCustomChange((CustomChange) field.get(objectToSerialize), indent + 1));
                    } else {
                        if (value != null) {
                            if (value instanceof Map) {
                                values.add(indent(indent) + propertyName + "=" + serializeObject((Map) value, indent + 1));
                            } else if (value instanceof Collection) {
                                values.add(indent(indent) + propertyName + "=" + serializeObject((Collection) value, indent + 1));
                            } else if (value instanceof Object[]) {
                                values.add(indent(indent) + propertyName + "=" + serializeObject((Object[]) value, indent + 1));
                            } else {
                                values.add(indent(indent) + propertyName + "=\"" + value.toString() + "\"");
                            }
                        }
                    }
                }
                classToCheck = classToCheck.getSuperclass();
            }

            if (values.size() > 0) {
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
            if (object instanceof ColumnConfig) {
                returnString += indent(indent) + serializeColumnConfig((ColumnConfig) object, indent + 1) + ",\n";
            } else {
                returnString += indent(indent) + object.toString()+ ",\n";
            }
        }
        returnString = returnString.replaceFirst(",$", "");
        returnString += indent(indent - 1) + "]";

        return returnString;

    }

    private String serializeObject(Collection collection, int indent) {
        if (collection.size() == 0) {
            return "[]";
        }

        String returnString = "[\n";
        for (Object object : collection) {
            if (object instanceof ColumnConfig) {
                returnString += indent(indent) + serializeColumnConfig((ColumnConfig) object, indent + 1) + ",\n";
            } else {
                returnString += indent(indent) + object.toString()+ ",\n";
            }
        }
        returnString = returnString.replaceFirst(",$", "");
        returnString += indent(indent - 1) + "]";

        return returnString;

    }

    private String serializeObject(Map collection, int indent) {
        if (collection.size() == 0) {
            return "[]";
        }

        String returnString = "{\n";
        for (Object key : new TreeSet(collection.keySet())) {
            returnString += indent(indent) +  key.toString() +"=\""+collection.get(key)+ "\",\n";
        }
        returnString = returnString.replaceFirst(",$", "");
        returnString += indent(indent - 1) + "}";

        return returnString;

    }

    public String serialize(ColumnConfig columnConfig) {
        return null;
    }

    private String serializeColumnConfig(ColumnConfig columnConfig, int indent) {
        return "column:" + serializeObject(columnConfig, indent);
    }

    private String serializeConstraintsConfig(ConstraintsConfig constraintsConfig, int indent) {
        return "constraints:" + serializeObject(constraintsConfig, indent);
    }

    private String serializeCustomChange(CustomChange customChange, int indent) {
        return "customChange:" + serializeObject(customChange, indent);
    }

    public String serialize(ChangeSet changeSet) {
        return null;
    }

	public void write(List<ChangeSet> changeSets, OutputStream out) throws IOException {
		
	}

    public void append(ChangeSet changeSet, File changeLogFile) throws IOException {

    }
}
