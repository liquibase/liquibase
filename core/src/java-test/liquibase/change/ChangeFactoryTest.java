package liquibase.change;

import liquibase.change.Change;
import liquibase.change.CreateTableChange;
import liquibase.change.ChangeFactory;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ChangeFactory}
 */
public class ChangeFactoryTest
{

  private ChangeFactory factory;

  @Before
  public void setUp()
  {
    factory = new ChangeFactory();
  }

  @Test
  public void createTable()
  {
    Change createTableChange = factory.create("createTable");
    assertEquals(CreateTableChange.class, createTableChange.getClass());
  }

  @Test(expected = RuntimeException.class)
  public void createInvalidChange()
  {
    factory.create("invalidChange");
  }
}
