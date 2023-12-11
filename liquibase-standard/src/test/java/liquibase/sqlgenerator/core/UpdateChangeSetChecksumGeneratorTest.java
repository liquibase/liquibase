package liquibase.sqlgenerator.core;

import liquibase.ChecksumVersion;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.UpdateChangeSetChecksumStatement;
import liquibase.statement.core.UpdateStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateChangeSetChecksumGeneratorTest {

    public static final String SOME_UPDATED_CHECK_SUM = "SomeUpdatedCheckSum";
    public static final String CHANGESET_SOME_ID = "SomeId";
    public static final String CHANGESET_SOME_AUTHOR = "SomeAuthor";
    public static final String CHANGESET_NORMALIZED_FILE_PATH = "SomeNormalizedFilePath";
    public static final String CHANGESET_STORED_FILE_PATH = "SomeStoredFilePath";

    UpdateChangeSetChecksumGenerator sqlGenerator = new UpdateChangeSetChecksumGenerator();

    @Mock
    Database mockedDatabase;

    @Mock
    ChangeSet mockedChangeSet;

    @Mock
    CheckSum mockedUpdatedCheckSum;

    @Mock
    SqlGeneratorFactory sqlGeneratorFactory;

    @Before
    public void setUp() throws Exception {
        doReturn("SomeCatalogName").when(mockedDatabase).getLiquibaseCatalogName();
        doReturn("SomeSchemaName").when(mockedDatabase).getLiquibaseSchemaName();
        doReturn("SomeChangeLogTableName").when(mockedDatabase).getDatabaseChangeLogTableName();
        doReturn(SOME_UPDATED_CHECK_SUM).when(mockedUpdatedCheckSum).toString();
        doReturn(mockedUpdatedCheckSum).when(mockedChangeSet).generateCheckSum(ChecksumVersion.latest());
        doReturn(CHANGESET_SOME_ID).when(mockedChangeSet).getId();
        doReturn(CHANGESET_SOME_AUTHOR).when(mockedChangeSet).getAuthor();
        doReturn(CHANGESET_NORMALIZED_FILE_PATH).when(mockedChangeSet).getFilePath();
        doReturn(CHANGESET_STORED_FILE_PATH).when(mockedChangeSet).getStoredFilePath();
        doAnswer((invocation) -> invocation.getArgument(0)).when(mockedDatabase).escapeObjectName(anyString(),any());
    }

    @Test
    public void generateSqlUsesStoredFilePathByDefault() {
        List<String> expectedWhereParams = Arrays.asList(CHANGESET_SOME_ID, CHANGESET_SOME_AUTHOR, CHANGESET_STORED_FILE_PATH);
        verifyWhereParamsListInTheGeneratedStatement(expectedWhereParams);
    }

    @Test
    public void generateSqlUsesChangesetsNormalizedFilePathWhenChangeSetStoredFilePathIsNull() {
        List<String> expectedWhereParams = Arrays.asList(CHANGESET_SOME_ID, CHANGESET_SOME_AUTHOR, CHANGESET_NORMALIZED_FILE_PATH);
        doReturn(null).when(mockedChangeSet).getStoredFilePath();
        verifyWhereParamsListInTheGeneratedStatement(expectedWhereParams);
    }


    private void verifyWhereParamsListInTheGeneratedStatement(List<String> expectedWhereParams) {

        try (MockedStatic<SqlGeneratorFactory> staticSqlGeneratorFactory = mockStatic(SqlGeneratorFactory.class)) {
            staticSqlGeneratorFactory.when(SqlGeneratorFactory::getInstance).thenReturn(sqlGeneratorFactory);
            UpdateChangeSetChecksumStatement statement = new UpdateChangeSetChecksumStatement(mockedChangeSet);

            sqlGenerator.generateSql(statement, mockedDatabase, null);
            ArgumentCaptor<SqlStatement> statementCaptor = ArgumentCaptor.forClass(SqlStatement.class);
            verify(sqlGeneratorFactory, times(1))
                    .generateSql(
                            statementCaptor.capture(), eq(mockedDatabase));
            SqlStatement suppliedSqlStatement = statementCaptor.getValue();
            assertTrue("Expect the supplied Statement to be a UpdateStatmeent Instance",
                    UpdateStatement.class.isInstance(suppliedSqlStatement));
            UpdateStatement suppliedUpdateStmt = (UpdateStatement) suppliedSqlStatement;
            List<Object> suppliedParams = suppliedUpdateStmt.getWhereParameters();
            assertEquals(expectedWhereParams, suppliedParams);

        }
    }

}
