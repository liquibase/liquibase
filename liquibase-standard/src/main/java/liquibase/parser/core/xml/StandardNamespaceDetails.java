package liquibase.parser.core.xml;

import liquibase.parser.LiquibaseParser;
import liquibase.parser.NamespaceDetails;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;

import java.util.regex.Pattern;

public class StandardNamespaceDetails implements NamespaceDetails {

    public static final String GENERIC_EXTENSION_XSD = "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd";
    private static final String STANDARD_XSD_URL_REGEX = "http://www.liquibase.org/xml/ns/dbchangelog/(dbchangelog-[\\w\\.]+.xsd)";
    private static final Pattern STANDARD_XSD_URL_PATTERN = Pattern.compile(STANDARD_XSD_URL_REGEX);

    private static final String OLD_STANDARD_XSD_URL_REGEX = "http://www.liquibase.org/xml/ns/migrator/(dbchangelog-[\\w\\.]+.xsd)";

    private static final Pattern OLD_STANDARD_XSD_URL_PATTERN = Pattern.compile(OLD_STANDARD_XSD_URL_REGEX);

    public StandardNamespaceDetails() {

    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(LiquibaseSerializer serializer, String namespaceOrUrl) {
        return serializer instanceof XMLChangeLogSerializer;
    }

    @Override
    public boolean supports(LiquibaseParser parser, String namespaceOrUrl) {
        return parser instanceof XMLChangeLogSAXParser;
    }

    @Override
    public String getShortName(String namespaceOrUrl) {
        if (namespaceOrUrl.equals(LiquibaseSerializable.STANDARD_CHANGELOG_NAMESPACE)) {
            return "";
        }
        return "ext";
    }

    @Override
    public String[] getNamespaces() {
        return new String[] {
                LiquibaseSerializable.STANDARD_CHANGELOG_NAMESPACE,
                LiquibaseSerializable.GENERIC_CHANGELOG_EXTENSION_NAMESPACE
        };
    }

    @Override
    public String getSchemaUrl(String namespaceOrUrl) {
        if (namespaceOrUrl.equals(LiquibaseSerializable.STANDARD_CHANGELOG_NAMESPACE)) {
            return "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd";
        }
        return GENERIC_EXTENSION_XSD;
    }
}
