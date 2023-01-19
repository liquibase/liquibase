package liquibase.changelog.visitor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.MockedStatic;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.PathResource;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

@RunWith(value = Parameterized.class)
public class DBDocVisitorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Database database;
    private long expectedNumberOfColumnHtmlFiles;

    public DBDocVisitorTest(Database database, long expectedNumberOfColumnHtmlFiles) {
        this.database = database;
        this.expectedNumberOfColumnHtmlFiles = expectedNumberOfColumnHtmlFiles;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new PostgresDatabase(), 1},
                {new OracleDatabase(), 1}
        });
    }

    @Test
    public void testWriteHTMLWhenOracleDatabaseWithComputedColumn() throws Exception {
        // given
        Column normalColumn = new Column("normal_column");
        Column computedColumn = new Column("computed_column");
        computedColumn.setComputed(true);
        Set<Column> columns = new HashSet<>(Arrays.asList(normalColumn, computedColumn));

        DatabaseSnapshot mockSnapshot = mock(DatabaseSnapshot.class);
        when(mockSnapshot.get(Table.class)).thenReturn(Collections.emptySet());
        when(mockSnapshot.get(Column.class)).thenReturn(columns);

        SnapshotGeneratorFactory mockSnapshotGeneratorFactory = mock(SnapshotGeneratorFactory.class);
        when(mockSnapshotGeneratorFactory.createSnapshot(any(CatalogAndSchema[].class), any(Database.class), any(SnapshotControl.class))).thenReturn(mockSnapshot);

        Path tempOutputDirPath = temporaryFolder.newFolder().toPath();
        PathResource rootOutputDir = new PathResource(tempOutputDirPath.toString(), tempOutputDirPath);
        ResourceAccessor resourceAccessor = new DirectoryResourceAccessor(tempOutputDirPath);

        DBDocVisitor dbDocVisitor = new DBDocVisitor(database);

        // when
        try (MockedStatic<SnapshotGeneratorFactory> staticMockSnapshotGeneratorFactory = mockStatic(SnapshotGeneratorFactory.class)) {
            staticMockSnapshotGeneratorFactory.when(SnapshotGeneratorFactory::getInstance).thenReturn(mockSnapshotGeneratorFactory);

            dbDocVisitor.writeHTML(rootOutputDir, resourceAccessor);
        }

        // then
        Path columnsOutputDirPath = tempOutputDirPath.resolve("columns");
        assertTrue(Files.isDirectory(columnsOutputDirPath));
        assertEquals(expectedNumberOfColumnHtmlFiles, Files.list(columnsOutputDirPath).count());
    }
}
