package liquibase.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbstractPathResourceAccessorTest {

    @TempDir
    Path tempDir;

    @Test
    void searchPropagatesDirectoryTraversalFailure() throws Exception {
        IOException traversalFailure = new IOException("Unable to iterate directory");
        DirectoryResourceAccessor accessor = new DirectoryResourceAccessor(tempDir);

        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            files.when(() -> Files.walkFileTree(
                            Mockito.any(Path.class),
                            Mockito.anySet(),
                            Mockito.anyInt(),
                            Mockito.<FileVisitor<Path>>any()))
                    .thenAnswer(invocation -> {
                        FileVisitor<Path> visitor = invocation.getArgument(3);
                        visitor.postVisitDirectory(tempDir, traversalFailure);
                        return tempDir;
                    });

            IOException thrown = assertThrows(
                    IOException.class,
                    () -> accessor.search("", new ResourceAccessor.SearchOptions()));

            assertSame(traversalFailure, thrown);
        }
    }
}
