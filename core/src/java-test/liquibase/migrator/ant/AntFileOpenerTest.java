package liquibase.migrator.ant;

import liquibase.migrator.AbstractFileOpenerTest;
import liquibase.migrator.FileOpener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

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

    @Test
    public void getResourceAsStreamNonExistantFile() throws Exception {
      assertNull(fileOpener.getResourceAsStream("non/existant/file.txt"));
    }

    @Test
    public void getResources() throws Exception {
      Enumeration<URL> resources = fileOpener.getResources("liquibase/migrator/ant");
      assertNotNull(resources);
    }
}