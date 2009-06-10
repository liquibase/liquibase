package liquibase.serializer;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.AddAutoIncrementStatement;
import liquibase.statement.SqlStatement;
import liquibase.sqlgenerator.core.AddColumnGenerator;
import liquibase.sqlgenerator.core.AddAutoIncrementGenerator;
import liquibase.sqlgenerator.core.AddAutoIncrementGeneratorHsql;
import liquibase.sqlgenerator.core.AddAutoIncrementGeneratorDB2;
import liquibase.serializer.xml.XMLChangeLogSerializer;
import liquibase.serializer.string.StringChangeLogSerializer;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.SortedSet;
import java.util.Collection;
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
        assertEquals(2, generators.size());
    }

    @Test
    public void getSerializers() {
        ChangeLogSerializer serializer = ChangeLogSerializerFactory.getInstance().getSerializer("xml");

        assertNotNull(serializer);
        assertTrue(serializer instanceof XMLChangeLogSerializer);
    }


}