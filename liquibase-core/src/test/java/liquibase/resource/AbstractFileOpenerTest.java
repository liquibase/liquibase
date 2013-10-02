package liquibase.resource;

import org.junit.Before;

/**
 * Base test class for file openers
 */
public abstract class AbstractFileOpenerTest
{
  protected ResourceAccessor resourceAccessor;

    protected abstract ResourceAccessor createFileOpener();

  @Before
  public void setUp() throws Exception
  {
    resourceAccessor = createFileOpener();
  }
}
