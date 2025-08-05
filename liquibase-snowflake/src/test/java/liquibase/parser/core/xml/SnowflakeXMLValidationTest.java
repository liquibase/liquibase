package liquibase.parser.core.xml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test XML document validation against Snowflake XSD schema to catch validation errors
 * during unit testing rather than at test harness execution.
 */
@DisplayName("Snowflake XML Schema Validation")
public class SnowflakeXMLValidationTest {

    private static final String XSD_PATH = "www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd";
    
    @Test
    @DisplayName("CreateDatabase element should validate against Snowflake XSD")
    public void testCreateDatabaseXMLValidation() throws Exception {
        // Test just the Snowflake-specific element since our XSD only defines Snowflake elements
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<createDatabase xmlns=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                databaseName=\"TEST_DB\"\n" +
            "                comment=\"Test database\"\n" +
            "                transient=\"true\"\n" +
            "                orReplace=\"true\"\n" +
            "                ifNotExists=\"true\"\n" +
            "                dataRetentionTimeInDays=\"7\"\n" +
            "                maxDataExtensionTimeInDays=\"30\"/>";
            
        assertDoesNotThrow(() -> validateXMLAgainstSchema(xml), 
            "CreateDatabase element should validate successfully against Snowflake XSD");
    }
    
    /**
     * Validate XML string against Snowflake XSD schema
     */
    private void validateXMLAgainstSchema(String xmlContent) throws Exception {
        // Load XSD schema
        InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(XSD_PATH);
        assertNotNull(xsdStream, "Could not load XSD schema: " + XSD_PATH);
        
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsdStream));
        
        // Validate XML against schema
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8))));
    }
}