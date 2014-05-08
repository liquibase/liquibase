package liquibase.serializer;

import liquibase.exception.SetupException;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ResourceAccessor;

import java.text.ParseException;
import java.util.Set;

public interface LiquibaseSerializable {

    String STANDARD_CHANGELOG_NAMESPACE = "http://www.liquibase.org/xml/ns/dbchangelog";
    String GENERIC_CHANGELOG_EXTENSION_NAMESPACE = "http://www.liquibase.org/xml/ns/dbchangelog-ext";

    String STANDARD_SNAPSHOT_NAMESPACE = "http://www.liquibase.org/xml/ns/snapshot";
    String GENERIC_SNAPSHOT_EXTENSION_NAMESPACE = "http://www.liquibase.org/xml/ns/snapshot-ext";

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

    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParseException, SetupException;

    public ParsedNode serialize();

}
