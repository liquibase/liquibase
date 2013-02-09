package liquibase.serializer;

import java.util.Set;

public interface LiquibaseSerializable {

    public enum SerializationType {
        NAMED_FIELD,
        NESTED_OBJECT,
        DIRECT_VALUE
    }

    String getSerializedObjectName();

    Set<String> getSerializableFields();

    Object getSerializableFieldValue(String field);

    public SerializationType getSerializableFieldType(String field);

}
