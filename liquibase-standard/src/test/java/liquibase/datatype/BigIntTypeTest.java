package liquibase.datatype;

import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.core.BigIntType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BigIntTypeTest {

    // Test for https://github.com/liquibase/liquibase/issues/3661
    @Test
    public void postgresqlBigintWithoutParenthesis() {
        BigIntType bit = new BigIntType();
        bit.finishInitialization("bigint(20)");
        
        String expected = "BIGINT";

        assertEquals(expected, bit.toDatabaseDataType(new PostgresDatabase()).toString());
    }
}
