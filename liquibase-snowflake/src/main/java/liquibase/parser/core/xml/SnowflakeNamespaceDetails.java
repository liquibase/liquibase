package liquibase.parser.core.xml;

import liquibase.parser.LiquibaseParser;
import liquibase.parser.NamespaceDetails;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;

/**
 * NamespaceDetails implementation for Snowflake-specific change types.
 * This class registers the Snowflake namespace and maps it to the appropriate XSD file.
 * 
 * IMPORTANT: This class is critical for XSD resolution in XML changelogs.
 * It maps the namespace URL to the actual XSD file location in the JAR.
 * Without this, you'll get "Unable to resolve xml entity" errors.
 * 
 * Requirements:
 * 1. The XSD file must exist at: /www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd
 * 2. This class must be registered in: META-INF/services/liquibase.parser.NamespaceDetails
 * 3. The liquibase-snowflake JAR must be on the classpath
 */
public class SnowflakeNamespaceDetails implements NamespaceDetails {

    public static final String SNOWFLAKE_NAMESPACE = "http://www.liquibase.org/xml/ns/snowflake";
    
    @Override
    public int getPriority() {
        return PRIORITY_EXTENSION;
    }

    @Override
    public boolean supports(LiquibaseSerializer serializer, String namespaceOrUrl) {
        return serializer instanceof XMLChangeLogSerializer && 
               SNOWFLAKE_NAMESPACE.equals(namespaceOrUrl);
    }

    @Override
    public boolean supports(LiquibaseParser parser, String namespaceOrUrl) {
        return parser instanceof XMLChangeLogSAXParser && 
               SNOWFLAKE_NAMESPACE.equals(namespaceOrUrl);
    }

    @Override
    public String getShortName(String namespaceOrUrl) {
        return "snowflake";
    }

    @Override
    public String getSchemaUrl(String namespaceOrUrl) {
        if (SNOWFLAKE_NAMESPACE.equals(namespaceOrUrl)) {
            return "http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd";
        }
        return null;
    }

    @Override
    public String[] getNamespaces() {
        return new String[] { SNOWFLAKE_NAMESPACE };
    }
}