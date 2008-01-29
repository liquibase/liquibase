package liquibase.ant;

import liquibase.FileOpener;
import liquibase.AbstractFileOpenerTest;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Tests for {@link AntFileOpener}
 */
public class AntFileOpenerTest extends AbstractFileOpenerTest {

    /**
     * @see liquibase.AbstractFileOpenerTest#createFileOpener()
     */
    @Override
    protected FileOpener createFileOpener() {
        Project project = new Project();
        return new AntFileOpener(project, new Path(project));
    }

    @Test
    public void getResourceAsStream() throws Exception {
      InputStream inputStream = fileOpener.getResourceAsStream("liquibase/ant/AntFileOpenerTest.class");
      assertNotNull(inputStream);
    }

    @Test
    public void getResourceAsStreamNonExistantFile() throws Exception {
      assertNull(fileOpener.getResourceAsStream("non/existant/file.txt"));
    }

    @Test
    public void getResources() throws Exception {
      Enumeration<URL> resources = fileOpener.getResources("liquibase/ant");
      assertNotNull(resources);
    }
}