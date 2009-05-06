package liquibase;

import org.junit.Before;

/**
 * Base test class for file openers
 */
public abstract class AbstractFileOpenerTest
{
  protected FileOpener fileOpener;

  protected abstract FileOpener createFileOpener();

  @Before
  public void setUp() throws Exception
  {
    fileOpener = createFileOpener();
  }
}
