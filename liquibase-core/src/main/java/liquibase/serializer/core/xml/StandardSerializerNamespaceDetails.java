package liquibase.serializer.core.xml;

import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.serializer.SerializerNamespaceDetails;

public class StandardSerializerNamespaceDetails implements SerializerNamespaceDetails {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(LiquibaseSerializer serializer, String namespace) {
        return serializer instanceof XMLChangeLogSerializer;
    }

    @Override
    public String getShortName(String namespace) {
        if (namespace.equals(LiquibaseSerializable.STANDARD_OBJECTS_NAMESPACE)) {
            return "";
        }
        return "ext";
    }

    @Override
    public String getSchemaUrl(String namespace) {
        if (namespace.equals(LiquibaseSerializable.STANDARD_OBJECTS_NAMESPACE)) {
            return "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-" + XMLChangeLogSAXParser.getSchemaVersion() + ".xsd";
        }
        return "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd";
    }
}
