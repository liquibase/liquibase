package liquibase.serializer;

import java.util.Set;

public interface LiquibaseSerializable {

    String STANDARD_OBJECTS_NAMESPACE = "http://www.liquibase.org/xml/ns/dbchangelog";
    String GENERIC_EXTENSION_NAMESPACE = "http://www.liquibase.org/xml/ns/dbchangelog-ext";

    public enum SerializationType {
        NAMED_FIELD,
        NESTED_OBJECT,
        DIRECT_VALUE
    }

    String getSerializedObjectName();

    Set<String> getSerializableFields();

    Object getSerializableFieldValue(String field);

    public SerializationType getSerializableFieldType(String field);

    String getSerializedObjectNamespace();

}
