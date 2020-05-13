package liquibase.serializer;

import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ChangeLogSerializerFactoryTest {

    @Before
    public void setUp() {
        ChangeLogSerializerFactory.reset();
    }

    @After
    public void tearDown() {
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

    @Test
    public void builtInSerializersAreFound() {
        Map<String, List<ChangeLogSerializer>> serializers = ChangeLogSerializerFactory.getInstance().getSerializers();
        assertEquals(6, serializers.size());
    }

    @Test
    public void getSerializers() {
        ChangeLogSerializer serializer = ChangeLogSerializerFactory.getInstance().getSerializer("xml");

        assertNotNull(serializer);
        assertSame(XMLChangeLogSerializer.class, serializer.getClass());
        assertEquals(1, ChangeLogSerializerFactory.getInstance().getSerializers("xml").size());
    }

    @Test
    public void highestPrioritySerializerReturned() {
        ChangeLogSerializerFactory factory = ChangeLogSerializerFactory.getInstance();

        XMLChangeLogSerializer highestPriority = new XMLChangeLogSerializer() {
            @Override
            public int getPriority() {
                return super.getPriority() + 4;
            }
        };
        factory.register(highestPriority);

        XMLChangeLogSerializer higherPriority = new XMLChangeLogSerializer() {
            @Override
            public int getPriority() {
                return super.getPriority() + 1;
            }
        };
        factory.register(higherPriority);

        assertSame(highestPriority, factory.getSerializer("xml"));
        assertEquals(3, factory.getSerializers().get("xml").size());
    }
}
