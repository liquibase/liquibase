package liquibase.serializer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import liquibase.sql.visitor.PrependSqlVisitor;

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
