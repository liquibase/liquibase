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

    public static boolean isBoolean(int dataType) {
        return dataType == Types.BOOLEAN;
    }

    public static boolean isDate(int dataType) {
        List<Integer> validTypes = Arrays.asList(
                Types.DATE,
                Types.TIME,
                Types.TIMESTAMP
        );

        return validTypes.contains(dataType);
    }
}
