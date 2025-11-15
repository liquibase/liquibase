package liquibase.resource;

import liquibase.ContextExpression;
import liquibase.GlobalConfiguration;
import liquibase.Labels;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


public class UrlDrivenResourceAccessorTest {

    UrlDrivenResourceAccessor accessor;
    DatabaseChangeLog changeLog;

    @BeforeEach
    public void setUp() {
        this.accessor = new UrlDrivenResourceAccessor();
        this.changeLog = new DatabaseChangeLog();
        changeLog.setChangeLogParameters(new ChangeLogParameters());
    }

    @Test
    public void rootChangeSet() throws LiquibaseException {
        assertTrue(changeLog.include(
                "classpath:liquibase/resource/changelog1.yml",
                false,
                true,
                accessor,
                new ContextExpression(),
                new Labels(),
                false,
                DatabaseChangeLog.OnUnknownFileFormat.FAIL));

        assertEquals(1, changeLog.getChangeSets().size());
        assertEquals("cs1", changeLog.getChangeSets().get(0).getId());
    }

    @Test
    public void nestedChangeSet_RelativePath_ThrowsByDefault() {
        assertThrows(LiquibaseException.class, () -> changeLog.include(
                "classpath:liquibase/resource/changelog2.yml",
                false,
                true,
                accessor,
                new ContextExpression(),
                new Labels(),
                false,
                DatabaseChangeLog.OnUnknownFileFormat.FAIL));
    }

    @Test
    public void nestedChangeSet_RelativePath_Allowed() throws LiquibaseException {
        Properties originalProps = (Properties) System.getProperties().clone();
        System.setProperty(GlobalConfiguration.PRESERVE_CLASSPATH_PREFIX_IN_NORMALIZED_PATHS.getKey(), "true");

        try {
            assertTrue(changeLog.include(
                    "classpath:liquibase/resource/changelog2.yml",
                    false,
                    true,
                    accessor,
                    new ContextExpression(),
                    new Labels(),
                    false,
                    DatabaseChangeLog.OnUnknownFileFormat.FAIL));
        }
        finally {
            System.setProperties(originalProps);
        }

        assertEquals(1, changeLog.getChangeSets().size());
        assertEquals("cs2", changeLog.getChangeSets().get(0).getId());
    }

    static class UrlResource implements Resource {

        private final String path;
        private final URL url;

        private volatile URI uri;

        UrlResource(String path, URL url) {
            this.path = Objects.requireNonNull(path);
            this.url = Objects.requireNonNull(url);
        }

        @Override
        public boolean exists() {
            // we don't really know if the URL exists
            return true;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return url.openStream();
        }

        @Override
        public OutputStream openOutputStream(OpenOptions openOptions) {
            throw new UnsupportedOperationException("The resource is not writable: " + url);
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public Resource resolve(String other) {
            throw new UnsupportedOperationException("The resource is not a directory: " + url);
        }

        @Override
        public Resource resolveSibling(String other) {

            String siblingPath = resolveSiblingPath(this.path, other);
            URL siblingUrl;
            try {
                siblingUrl = new URL(resolveSiblingPath(url.toString(), other));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            return new UrlResource(siblingPath, siblingUrl);
        }

        @Override
        public URI getUri() {
            if (uri == null) {
                try {
                    uri = url.toURI();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            return uri;
        }

        private String resolveSiblingPath(String path, String other) {
            if (path.contains("/")) {
                return path.replaceFirst("/[^/]*$", "") + "/" + other;
            } else {
                return "/" + other;
            }
        }

        @Override
        public boolean isFile() {
            return true;
        }
    }
}
