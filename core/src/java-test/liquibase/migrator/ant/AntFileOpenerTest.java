package liquibase.migrator.ant;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import liquibase.migrator.AbstractFileOpenerTest;
import liquibase.migrator.FileOpener;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.junit.Test;

/**
 * Tests for {@link AntFileOpener}
 */
public class AntFileOpenerTest extends AbstractFileOpenerTest {

    /**
     * @see liquibase.migrator.AbstractFileOpenerTest#createFileOpener()
     */
    @Override
    protected FileOpener createFileOpener() {
        Project project = new Project();
        return new AntFileOpener(project, new Path(project));
    }

    @Test
    public void getResourceAsStream() throws Exception {
      InputStream inputStream = fileOpener.getResourceAsStream("liquibase/migrator/ant/AntFileOpenerTest.class");
      assertNotNull(inputStream);
    }

    @Test(expected = IOException.class)
    public void getResourceAsStreamNonExistantFile() throws Exception {
      fileOpener.getResourceAsStream("non/existant/file.txt");
    }

    @Test
    public void getResources() throws Exception {
      Enumeration<URL> resources = fileOpener.getResources("liquibase/migrator/ant");
      assertNotNull(resources);
    }
}