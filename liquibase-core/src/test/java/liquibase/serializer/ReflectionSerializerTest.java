package liquibase.serializer;

import liquibase.sql.visitor.PrependSqlVisitor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReflectionSerializerTest {

    @Test
    public void getValue() {
        PrependSqlVisitor visitor = new PrependSqlVisitor();
        visitor.setValue("ValHere");
        visitor.setApplyToRollback(true);

        assertEquals("ValHere", ReflectionSerializer.getInstance().getValue(visitor, "value"));
        assertEquals(true, ReflectionSerializer.getInstance().getValue(visitor, "applyToRollback"));
    }
}
