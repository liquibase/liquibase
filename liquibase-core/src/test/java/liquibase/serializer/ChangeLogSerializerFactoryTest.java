package liquibase.serializer;

import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class ChangeLogSerializerFactoryTest {

    @Before
    public void setup() {
        ChangeLogSerializerFactory.reset();

    }

    @Test
    public void getInstance() {
        assertNotNull(ChangeLogSerializerFactory.getInstance());
        
        assertTrue(ChangeLogSerializerFactory.getInstance() == ChangeLogSerializerFactory.getInstance());
    }

    @Test
    public void register() {
        ChangeLogSerializerFactory.getInstance().getSerializers().clear();

        assertEquals(0, ChangeLogSerializerFactory.getInstance().getSerializers().size());

        ChangeLogSerializerFactory.getInstance().register(new MockChangeLogSerializer("mock"));

        assertEquals(1, ChangeLogSerializerFactory.getInstance().getSerializers().size());
    }

    @Test
    public void unregister_instance() {
        ChangeLogSerializerFactory factory = ChangeLogSerializerFactory.getInstance();

        factory.getSerializers().clear();

        assertEquals(0, factory.getSerializers().size());

        XMLChangeLogSerializer changeLogSerializer = new XMLChangeLogSerializer();

        factory.register(new StringChangeLogSerializer());
        factory.register(changeLogSerializer);

        assertEquals(2, factory.getSerializers().size());

        factory.unregister(changeLogSerializer);
        assertEquals(1, factory.getSerializers().size());
    }

    @Test
    public void reset() {
        ChangeLogSerializerFactory instance1 = ChangeLogSerializerFactory.getInstance();
        ChangeLogSerializerFactory.reset();
        assertFalse(instance1 == ChangeLogSerializerFactory.getInstance());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void builtInGeneratorsAreFound() {
        Map<String, ChangeLogSerializer> generators = ChangeLogSerializerFactory.getInstance().getSerializers();
        assertEquals(5, generators.size());
    }

    @Test
    public void getSerializers() {
        ChangeLogSerializer serializer = ChangeLogSerializerFactory.getInstance().getSerializer("xml");

        assertNotNull(serializer);
        assertTrue(serializer instanceof XMLChangeLogSerializer);
    }


}