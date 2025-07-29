package liquibase.parser;

import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import liquibase.changelog.ChangeLogParameters;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SnowflakeNamespaceAwareXMLParser
 */
@DisplayName("SnowflakeNamespaceAwareXMLParser")
public class SnowflakeNamespaceAwareXMLParserTest {
    
    private SnowflakeNamespaceAwareXMLParser parser;
    
    @Mock
    private ResourceAccessor resourceAccessor;
    
    @Mock
    private Resource resource;
    
    @Mock
    private ChangeLogParameters changeLogParameters;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parser = new SnowflakeNamespaceAwareXMLParser();
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @AfterEach
    void tearDown() {
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @Test
    @DisplayName("Should have higher priority than default parser")
    void shouldHaveHigherPriorityThanDefault() {
        assertTrue(parser.getPriority() > ChangeLogParser.PRIORITY_DEFAULT);
    }
    
    @Test
    @DisplayName("Should capture snowflake namespace attributes for createTable")
    void shouldCaptureSnowflakeNamespaceAttributesForCreateTable() throws Exception {
        // Given
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog\n" +
            "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet id=\"1\" author=\"test\">\n" +
            "        <createTable tableName=\"TEST_TABLE\" \n" +
            "                   snowflake:transient=\"true\"\n" +
            "                   snowflake:clusterBy=\"id,created_at\"\n" +
            "                   snowflake:dataRetentionTimeInDays=\"7\">\n" +
            "            <column name=\"id\" type=\"INT\"/>\n" +
            "            <column name=\"created_at\" type=\"TIMESTAMP\"/>\n" +
            "        </createTable>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>\n";
        
        when(resource.openInputStream()).thenReturn(
            new ByteArrayInputStream(xml.getBytes()),
            new ByteArrayInputStream(xml.getBytes())
        );
        when(resource.exists()).thenReturn(true);
        when(resource.getPath()).thenReturn("test.xml");
        List<Resource> resources = new ArrayList<>();
        resources.add(resource);
        when(resourceAccessor.getAll(anyString())).thenReturn(resources);
        when(resourceAccessor.get(anyString())).thenReturn(resource);
        when(resourceAccessor.getExisting(anyString())).thenReturn(resource);
        
        // When
        parser.parseToNode("test.xml", changeLogParameters, resourceAccessor);
        
        // Then
        Map<String, String> attrs = SnowflakeNamespaceAttributeStorage.getAttributes("TEST_TABLE");
        assertNotNull(attrs);
        assertEquals("true", attrs.get("transient"));
        assertEquals("id,created_at", attrs.get("clusterBy"));
        assertEquals("7", attrs.get("dataRetentionTimeInDays"));
    }
    
    @Test
    @DisplayName("Should capture attributes for multiple tables")
    void shouldCaptureAttributesForMultipleTables() throws Exception {
        // Given
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog\n" +
            "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "    xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\">\n" +
            "    \n" +
            "    <changeSet id=\"1\" author=\"test\">\n" +
            "        <createTable tableName=\"TABLE1\" snowflake:transient=\"true\">\n" +
            "            <column name=\"id\" type=\"INT\"/>\n" +
            "        </createTable>\n" +
            "        <createTable tableName=\"TABLE2\" snowflake:temporary=\"true\">\n" +
            "            <column name=\"id\" type=\"INT\"/>\n" +
            "        </createTable>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>\n";
        
        when(resource.openInputStream()).thenReturn(
            new ByteArrayInputStream(xml.getBytes()),
            new ByteArrayInputStream(xml.getBytes())
        );
        when(resource.exists()).thenReturn(true);
        when(resource.getPath()).thenReturn("test.xml");
        List<Resource> resources = new ArrayList<>();
        resources.add(resource);
        when(resourceAccessor.getAll(anyString())).thenReturn(resources);
        when(resourceAccessor.get(anyString())).thenReturn(resource);
        when(resourceAccessor.getExisting(anyString())).thenReturn(resource);
        
        // When
        parser.parseToNode("test.xml", changeLogParameters, resourceAccessor);
        
        // Then
        Map<String, String> attrs1 = SnowflakeNamespaceAttributeStorage.getAttributes("TABLE1");
        assertNotNull(attrs1);
        assertEquals("true", attrs1.get("transient"));
        
        Map<String, String> attrs2 = SnowflakeNamespaceAttributeStorage.getAttributes("TABLE2");
        assertNotNull(attrs2);
        assertEquals("true", attrs2.get("temporary"));
    }
    
    @Test
    @DisplayName("Should ignore non-snowflake namespace attributes")
    void shouldIgnoreNonSnowflakeNamespaceAttributes() throws Exception {
        // Given
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog\n" +
            "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "    xmlns:custom=\"http://example.com/custom\">\n" +
            "    \n" +
            "    <changeSet id=\"1\" author=\"test\">\n" +
            "        <createTable tableName=\"TEST_TABLE\" \n" +
            "                   custom:attribute=\"value\"\n" +
            "                   schemaName=\"PUBLIC\">\n" +
            "            <column name=\"id\" type=\"INT\"/>\n" +
            "        </createTable>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>\n";
        
        when(resource.openInputStream()).thenReturn(
            new ByteArrayInputStream(xml.getBytes()),
            new ByteArrayInputStream(xml.getBytes())
        );
        when(resource.exists()).thenReturn(true);
        when(resource.getPath()).thenReturn("test.xml");
        List<Resource> resources = new ArrayList<>();
        resources.add(resource);
        when(resourceAccessor.getAll(anyString())).thenReturn(resources);
        when(resourceAccessor.get(anyString())).thenReturn(resource);
        when(resourceAccessor.getExisting(anyString())).thenReturn(resource);
        
        // When
        parser.parseToNode("test.xml", changeLogParameters, resourceAccessor);
        
        // Then
        Map<String, String> attrs = SnowflakeNamespaceAttributeStorage.getAttributes("TEST_TABLE");
        assertNull(attrs);
    }
    
    @Test
    @DisplayName("Should capture attributes for alterTable")
    void shouldCaptureAttributesForAlterTable() throws Exception {
        // Given
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog\n" +
            "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "    xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\">\n" +
            "    \n" +
            "    <changeSet id=\"1\" author=\"test\">\n" +
            "        <alterTable tableName=\"TEST_TABLE\" \n" +
            "                   snowflake:clusterBy=\"id,date\">\n" +
            "            <addColumn>\n" +
            "                <column name=\"date\" type=\"DATE\"/>\n" +
            "            </addColumn>\n" +
            "        </alterTable>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>\n";
        
        when(resource.openInputStream()).thenReturn(
            new ByteArrayInputStream(xml.getBytes()),
            new ByteArrayInputStream(xml.getBytes())
        );
        when(resource.exists()).thenReturn(true);
        when(resource.getPath()).thenReturn("test.xml");
        List<Resource> resources = new ArrayList<>();
        resources.add(resource);
        when(resourceAccessor.getAll(anyString())).thenReturn(resources);
        when(resourceAccessor.get(anyString())).thenReturn(resource);
        when(resourceAccessor.getExisting(anyString())).thenReturn(resource);
        
        // When
        parser.parseToNode("test.xml", changeLogParameters, resourceAccessor);
        
        // Then
        Map<String, String> attrs = SnowflakeNamespaceAttributeStorage.getAttributes("TEST_TABLE");
        assertNotNull(attrs);
        assertEquals("id,date", attrs.get("clusterBy"));
    }
    
    @Test
    @DisplayName("Should capture attributes for createSequence")
    void shouldCaptureAttributesForCreateSequence() throws Exception {
        // Given
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog\n" +
            "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "    xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\">\n" +
            "    \n" +
            "    <changeSet id=\"1\" author=\"test\">\n" +
            "        <createSequence sequenceName=\"TEST_SEQ\" \n" +
            "                      snowflake:order=\"true\"\n" +
            "                      snowflake:orReplace=\"true\">\n" +
            "        </createSequence>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>\n";
        
        when(resource.openInputStream()).thenReturn(
            new ByteArrayInputStream(xml.getBytes()),
            new ByteArrayInputStream(xml.getBytes())
        );
        when(resource.exists()).thenReturn(true);
        when(resource.getPath()).thenReturn("test.xml");
        List<Resource> resources = new ArrayList<>();
        resources.add(resource);
        when(resourceAccessor.getAll(anyString())).thenReturn(resources);
        when(resourceAccessor.get(anyString())).thenReturn(resource);
        when(resourceAccessor.getExisting(anyString())).thenReturn(resource);
        
        // When
        parser.parseToNode("test.xml", changeLogParameters, resourceAccessor);
        
        // Then
        Map<String, String> attrs = SnowflakeNamespaceAttributeStorage.getAttributes("TEST_SEQ");
        assertNotNull(attrs);
        assertEquals("true", attrs.get("order"));
        assertEquals("true", attrs.get("orReplace"));
    }
    
    @Test
    @DisplayName("Should handle empty snowflake attributes")
    void shouldHandleEmptySnowflakeAttributes() throws Exception {
        // Given
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog\n" +
            "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "    xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\">\n" +
            "    \n" +
            "    <changeSet id=\"1\" author=\"test\">\n" +
            "        <createTable tableName=\"TEST_TABLE\">\n" +
            "            <column name=\"id\" type=\"INT\"/>\n" +
            "        </createTable>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>\n";
        
        when(resource.openInputStream()).thenReturn(
            new ByteArrayInputStream(xml.getBytes()),
            new ByteArrayInputStream(xml.getBytes())
        );
        when(resource.exists()).thenReturn(true);
        when(resource.getPath()).thenReturn("test.xml");
        List<Resource> resources = new ArrayList<>();
        resources.add(resource);
        when(resourceAccessor.getAll(anyString())).thenReturn(resources);
        when(resourceAccessor.get(anyString())).thenReturn(resource);
        when(resourceAccessor.getExisting(anyString())).thenReturn(resource);
        
        // When
        parser.parseToNode("test.xml", changeLogParameters, resourceAccessor);
        
        // Then
        Map<String, String> attrs = SnowflakeNamespaceAttributeStorage.getAttributes("TEST_TABLE");
        assertNull(attrs);
    }
    
    @Test
    @DisplayName("Should continue parsing even if namespace capture fails")
    void shouldContinueParsingEvenIfNamespaceCaptureFails() throws Exception {
        // Given - malformed XML for namespace parsing but valid for regular parsing
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog\n" +
            "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\">\n" +
            "    \n" +
            "    <changeSet id=\"1\" author=\"test\">\n" +
            "        <createTable tableName=\"TEST_TABLE\">\n" +
            "            <column name=\"id\" type=\"INT\"/>\n" +
            "        </createTable>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>\n";
        
        // First call returns malformed stream, second returns good stream
        InputStream malformedStream = new ByteArrayInputStream("malformed".getBytes());
        InputStream goodStream = new ByteArrayInputStream(xml.getBytes());
        
        when(resource.openInputStream())
            .thenReturn(malformedStream)
            .thenReturn(goodStream);
        List<Resource> resources = new ArrayList<>();
        resources.add(resource);
        when(resourceAccessor.getAll(anyString())).thenReturn(resources);
        
        // When - should not throw exception
        assertDoesNotThrow(() -> 
            parser.parseToNode("test.xml", changeLogParameters, resourceAccessor)
        );
    }
}