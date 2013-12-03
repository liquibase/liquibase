package liquibase.serializer.core.xml;

import liquibase.parser.LiquibaseParser;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.serializer.SerializerNamespaceDetails;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardSerializerNamespaceDetails implements SerializerNamespaceDetails {

    public static final String GENERIC_EXTENSION_XSD = "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd";
    private final Pattern standardUrlPattern;
    private final Pattern oldStandardUrlPattern;

    public StandardSerializerNamespaceDetails() {
        standardUrlPattern = Pattern.compile("http://www.liquibase.org/xml/ns/dbchangelog/(dbchangelog-[\\d\\.]+.xsd)");
        oldStandardUrlPattern = Pattern.compile("http://www.liquibase.org/xml/ns/migrator/(dbchangelog-[\\d\\.]+.xsd)");
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(LiquibaseSerializer serializer, String namespace) {
        return serializer instanceof XMLChangeLogSerializer;
    }

    @Override
    public boolean supports(LiquibaseParser parser, String namespace) {
        return parser instanceof XMLChangeLogSAXParser;
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
        return GENERIC_EXTENSION_XSD;
    }

    @Override
    public String getLocalPath(String url) {

        if (url.equals(GENERIC_EXTENSION_XSD)) {
            return "liquibase/parser/core/xml/dbchangelog-ext.xsd";
        }
        Matcher matcher = standardUrlPattern.matcher(url);
        if (matcher.matches()) {
            return "liquibase/parser/core/xml/"+matcher.group(1);
        }

        matcher = oldStandardUrlPattern.matcher(url);
        if (matcher.matches()) {
            return "liquibase/parser/core/xml/"+matcher.group(1);
        }

        return null;
    }
}
