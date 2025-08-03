package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateFileFormatStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CreateFileFormatGeneratorSnowflake
 */
@DisplayName("CreateFileFormatGeneratorSnowflake")
public class CreateFileFormatGeneratorSnowflakeTest {
    
    private CreateFileFormatGeneratorSnowflake generator;
    private CreateFileFormatStatement statement;
    
    @Mock
    private SnowflakeDatabase database;
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new CreateFileFormatGeneratorSnowflake();
        statement = new CreateFileFormatStatement();
        
        // Setup database mock
        when(database.escapeObjectName("MY_CSV_FORMAT", liquibase.structure.core.Table.class))
            .thenReturn("MY_CSV_FORMAT");
        when(database.escapeObjectName("MY_JSON_FORMAT", liquibase.structure.core.Table.class))
            .thenReturn("MY_JSON_FORMAT");
        when(database.escapeObjectName("MY_FORMAT", liquibase.structure.core.Table.class))
            .thenReturn("MY_FORMAT");
        when(database.escapeObjectName("TEMP_FORMAT", liquibase.structure.core.Table.class))
            .thenReturn("TEMP_FORMAT");
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(generator.supports(statement, database));
    }
    
    @Test
    @DisplayName("Should require file format name")
    void shouldRequireFileFormatName() {
        statement.setFileFormatName(null);
        
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("File format name is required")));
    }
    
    @Test
    @DisplayName("Should reject OR REPLACE with IF NOT EXISTS")
    void shouldRejectOrReplaceWithIfNotExists() {
        statement.setFileFormatName("MY_FORMAT");
        statement.setOrReplace(true);
        statement.setIfNotExists(true);
        
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot use both OR REPLACE and IF NOT EXISTS")));
    }
    
    @Test
    @DisplayName("Should reject TEMPORARY with VOLATILE")
    void shouldRejectTemporaryWithVolatile() {
        statement.setFileFormatName("MY_FORMAT");
        statement.setTemporary(true);
        statement.setVolatile(true);
        
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot use both TEMPORARY and VOLATILE")));
    }
    
    @Test
    @DisplayName("Should generate basic CREATE FILE FORMAT SQL")
    void shouldGenerateBasicCreateFileFormatSql() {
        statement.setFileFormatName("MY_CSV_FORMAT");
        statement.setFileFormatType("CSV");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        
        assertTrue(sql.contains("CREATE FILE FORMAT MY_CSV_FORMAT"));
        assertTrue(sql.contains("TYPE = CSV"));
    }
    
    @Test
    @DisplayName("Should generate CREATE OR REPLACE FILE FORMAT SQL")
    void shouldGenerateCreateOrReplaceFileFormatSql() {
        statement.setFileFormatName("MY_FORMAT");
        statement.setOrReplace(true);
        statement.setFileFormatType("JSON");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        
        assertTrue(sql.startsWith("CREATE OR REPLACE FILE FORMAT"));
        assertTrue(sql.contains("MY_FORMAT"));
        assertTrue(sql.contains("TYPE = JSON"));
    }
    
    @Test
    @DisplayName("Should generate CREATE FILE FORMAT IF NOT EXISTS SQL")
    void shouldGenerateCreateFileFormatIfNotExistsSql() {
        statement.setFileFormatName("MY_FORMAT");
        statement.setIfNotExists(true);
        statement.setFileFormatType("PARQUET");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        
        assertTrue(sql.contains("CREATE FILE FORMAT IF NOT EXISTS"));
        assertTrue(sql.contains("MY_FORMAT"));
        assertTrue(sql.contains("TYPE = PARQUET"));
    }
    
    @Test
    @DisplayName("Should generate TEMPORARY FILE FORMAT SQL")
    void shouldGenerateTemporaryFileFormatSql() {
        statement.setFileFormatName("TEMP_FORMAT");
        statement.setTemporary(true);
        statement.setFileFormatType("CSV");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        
        assertTrue(sql.contains("CREATE TEMPORARY FILE FORMAT"));
        assertTrue(sql.contains("TEMP_FORMAT"));
        assertTrue(sql.contains("TYPE = CSV"));
    }
    
    @Test
    @DisplayName("Should generate CSV format with specific options")
    void shouldGenerateCsvFormatWithOptions() {
        statement.setFileFormatName("MY_CSV_FORMAT");
        statement.setFileFormatType("CSV");
        statement.setFieldDelimiter(",");
        statement.setRecordDelimiter("\\n");
        statement.setSkipHeader(1);
        statement.setTrimSpace(true);
        statement.setComment("My CSV format");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        
        assertTrue(sql.contains("TYPE = CSV"));
        assertTrue(sql.contains("FIELD_DELIMITER = ','"));
        assertTrue(sql.contains("RECORD_DELIMITER = '\\n'"));
        assertTrue(sql.contains("SKIP_HEADER = 1"));
        assertTrue(sql.contains("TRIM_SPACE = true"));
        assertTrue(sql.contains("COMMENT = 'My CSV format'"));
    }
    
    @Test
    @DisplayName("Should generate JSON format with specific options")
    void shouldGenerateJsonFormatWithOptions() {
        statement.setFileFormatName("MY_JSON_FORMAT");
        statement.setFileFormatType("JSON");
        statement.setStripOuterArray(true);
        statement.setAllowDuplicate(false);
        statement.setCompression("GZIP");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        
        assertTrue(sql.contains("TYPE = JSON"));
        assertTrue(sql.contains("STRIP_OUTER_ARRAY = true"));
        assertTrue(sql.contains("ALLOW_DUPLICATE = false"));
        assertTrue(sql.contains("COMPRESSION = GZIP"));
    }
    
    @Test
    @DisplayName("Should handle NULL_IF with multiple values")
    void shouldHandleNullIfWithMultipleValues() {
        statement.setFileFormatName("MY_FORMAT");
        statement.setFileFormatType("CSV");
        statement.setNullIf("NULL, '', N/A");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        
        assertTrue(sql.contains("NULL_IF = ("));
        assertTrue(sql.contains("'NULL'"));
        assertTrue(sql.contains("'N/A'"));
    }
    
    @Test
    @DisplayName("Should validate negative skip header")
    void shouldValidateNegativeSkipHeader() {
        statement.setFileFormatName("MY_FORMAT");
        statement.setSkipHeader(-1);
        
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("skipHeader must be 0 or positive")));
    }
}