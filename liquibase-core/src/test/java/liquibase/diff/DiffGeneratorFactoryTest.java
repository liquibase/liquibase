package liquibase.diff;

import liquibase.database.core.H2DatabaseTemp;
import liquibase.diff.core.StandardDiffGenerator;
import liquibase.exception.DatabaseException;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DiffGeneratorFactoryTest {
	@Test
	public void getGenerator() throws DatabaseException {
        DiffGenerator generator = DiffGeneratorFactory.getInstance().getGenerator(new H2DatabaseTemp(), new H2DatabaseTemp());
        assertNotNull(generator);
        assertTrue(generator instanceof StandardDiffGenerator);
    }
}
