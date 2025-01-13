package liquibase.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import liquibase.api.generated.DatabaseChangeLog;
import liquibase.api.generated.ObjectQuotingStrategy;
import liquibase.api.generated.OnChangeSetValidationFail;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DatabaseChangelogTest {

    @Test
    public void testSerializeDatabaseChangeLogToJson() throws IOException {
        // Create a concrete implementation of DatabaseChangeLog
        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        changeLog.setLogicalFilePath("path/to/changelog.xml");
        changeLog.setContext("testContext");
        changeLog.setContextFilter("testContextFilter");
        changeLog.setChangeLogId("testChangeLogId");
        changeLog.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);

        // Add a property
        DatabaseChangeLog.Property property = new DatabaseChangeLog.Property();
        property.setFile("path/to/file");
        property.setRelativeToChangelogFile("true");
        property.setErrorIfMissing("false");
        property.setName("testName");
        property.setValue("testValue");
        property.setDbms("testDbms");
        property.setContext("testContext");
        property.setContextFilter("testContextFilter");
        property.setLabels("testLabels");
        property.setGlobal(true);
        property.setTarget("testTarget");
        changeLog.getProperty().add(property);

        // Add a removeChangeSetProperty
        DatabaseChangeLog.RemoveChangeSetProperty removeChangeSetProperty = new DatabaseChangeLog.RemoveChangeSetProperty();
        removeChangeSetProperty.setChange("testChange");
        removeChangeSetProperty.setDbms("testDbms");
        removeChangeSetProperty.setRemove("testRemove");
        changeLog.getRemoveChangeSetProperty().add(removeChangeSetProperty);

        // Add a changeSet with changeSetChildren
        DatabaseChangeLog.ChangeSet changeSet = new DatabaseChangeLog.ChangeSet();
        changeSet.setId("testId");
        changeSet.setAuthor("testAuthor");
        changeSet.setContext("testContext");
        changeSet.setContextFilter("testContextFilter");
        changeSet.setLabels("testLabels");
        changeSet.setDbms("testDbms");
        changeSet.setRunOnChange("true");
        changeSet.setRunAlways("false");
        changeSet.setFailOnError("true");
        changeSet.setOnValidationFail(OnChangeSetValidationFail.HALT);
        changeSet.setRunInTransaction("true");
        changeSet.setLogicalFilePath("path/to/logicalFilePath");
        changeSet.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
        changeSet.setCreated("2025-01-10T10:45:55");
        changeSet.setRunOrder("first");
        changeSet.setIgnore("false");
        changeSet.setRunWith("testRunWith");
        changeSet.setRunWithSpoolFile("testRunWithSpoolFile");

        // Add changeSetChildren
        DatabaseChangeLog.ChangeSet.ValidCheckSum validCheckSum = new DatabaseChangeLog.ChangeSet.ValidCheckSum();
        validCheckSum.getContent().add("Test");
        changeSet.getValidCheckSum().add(validCheckSum);

        changeLog.getChangeSetOrIncludeOrIncludeAll().add(changeSet);

        // Serialize to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(changeLog);

        // Validate that the JSON is not null
        assertNotNull(json);
        System.out.println(json);
    }

    @Test
    public void testReadChangeLogFile() throws Exception {
        // Create an XmlMapper instance
        JAXBContext jc = JAXBContext.newInstance(DatabaseChangeLog.class);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        // Read the XML file into a DatabaseChangeLog object
        File file = new File("src/test/java/liquibase/api/change.xml");
        DatabaseChangeLog changeLog = (DatabaseChangeLog) unmarshaller.unmarshal(file);

        // Validate that the changeLog is not null
        assertNotNull(changeLog, "The change log should not be null");
        assertFalse(changeLog.getChangeSetOrIncludeOrIncludeAll().isEmpty(), "The change log should contain change sets");
    }
}
