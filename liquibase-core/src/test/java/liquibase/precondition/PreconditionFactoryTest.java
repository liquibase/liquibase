package liquibase.precondition;

import liquibase.precondition.core.AndPrecondition;
import liquibase.precondition.core.OrPrecondition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class PreconditionFactoryTest {

    @Before
    public void setup() {
        PreconditionFactory.reset();

    }

    @After
    public void after() {
        PreconditionFactory.reset();

    }

    @Test
    public void getInstance() {
        assertNotNull(PreconditionFactory.getInstance());
        
        assertTrue(PreconditionFactory.getInstance() == PreconditionFactory.getInstance());
    }

    @Test
    public void register() {
        PreconditionFactory.getInstance().getPreconditions().clear();

        assertEquals(0, PreconditionFactory.getInstance().getPreconditions().size());

        PreconditionFactory.getInstance().register(MockPrecondition.class);

        assertEquals(1, PreconditionFactory.getInstance().getPreconditions().size());
    }

    @Test
    public void unregister_instance() {
        PreconditionFactory factory = PreconditionFactory.getInstance();

        factory.getPreconditions().clear();

        assertEquals(0, factory.getPreconditions().size());

        Class<? extends Precondition> precondition = AndPrecondition.class;

        factory.register(OrPrecondition.class);
        factory.register(precondition);

        assertEquals(2, factory.getPreconditions().size());

        factory.unregister("and");
        assertEquals(1, factory.getPreconditions().size());
    }

    @Test
    public void reset() {
        PreconditionFactory instance1 = PreconditionFactory.getInstance();
        PreconditionFactory.reset();
        assertFalse(instance1 == PreconditionFactory.getInstance());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void builtInGeneratorsAreFound() {
        Map<String, Class<? extends Precondition>> generators = PreconditionFactory.getInstance().getPreconditions();
        assertTrue(generators.size() > 5);
    }

    @Test
    public void createPreconditions() {
        Precondition precondtion = PreconditionFactory.getInstance().create("and");

        assertNotNull(precondtion);
        assertTrue(precondtion instanceof AndPrecondition);
    }


}