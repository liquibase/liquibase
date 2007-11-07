package liquibase.util;

import java.sql.Types;
import java.util.Arrays;
import java.util.List;

public class SqlUtil {

    public static boolean isNumeric(int dataType) {
        List<Integer> numericTypes = Arrays.asList(
                Types.BIGINT,
                Types.BIT,
                Types.INTEGER,
                Types.SMALLINT,
                Types.TINYINT,
                Types.DECIMAL,
                Types.DOUBLE,
                Types.FLOAT,
                Types.NUMERIC,
                Types.REAL
        );

        return numericTypes.contains(dataType);
    }
}
