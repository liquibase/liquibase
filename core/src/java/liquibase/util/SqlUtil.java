package liquibase.util;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;

import java.util.List;
import java.util.Arrays;
import java.sql.Types;

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
