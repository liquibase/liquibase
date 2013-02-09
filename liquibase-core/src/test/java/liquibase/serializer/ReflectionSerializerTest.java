package liquibase.serializer;

import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.sql.visitor.PrependSqlVisitor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.structure.core.Table;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

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
