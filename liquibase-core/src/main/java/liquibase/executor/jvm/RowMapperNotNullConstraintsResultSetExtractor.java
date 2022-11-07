package liquibase.executor.jvm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter implementation of the ResultSetExtractor interface that delegates
 * to a RowMapper which is supposed to create a not null constraint object for each row.
 *
 * Special mapper needed because sys table ALL_CONSTRAINTS provides info about not null constraints
 * that have type 'C' and SEARCH_CONDITION(Text of search condition for a check constraint) field with 'is not null' string
 * of data type: 'LONG' But type 'LONG' limits us to do any text operations (match, contains, etc.) on DB level
 * Only one way to do this on backend side
 *
 * @see RowMapper
 * @see RowMapperResultSetExtractor
 * @see liquibase.executor.Executor
 */
public class RowMapperNotNullConstraintsResultSetExtractor extends RowMapperResultSetExtractor {
    /**
     * Field describes condition for NotNullConstraint in String format e.g. "ID" IS NOT NULL |  name = 'foo' | etc.
     */
    private static final String SEARCH_CONDITION_FIELD = "SEARCH_CONDITION";
    private static final String SEARCH_CONDITION = " is not null";

    public RowMapperNotNullConstraintsResultSetExtractor(RowMapper rowMapper) {
        super(rowMapper);

        if (!(rowMapper instanceof ColumnMapRowMapper)) {
            throw new AssertionError(String.format("Class %s should work only with %s",
                    RowMapperNotNullConstraintsResultSetExtractor.class, ColumnMapRowMapper.class));
        }
    }

    @Override
    public Object extractData(ResultSet resultSet) throws SQLException {
        List<Object> resultList = (this.rowsExpected > 0 ? new ArrayList<Object>(this.rowsExpected) : new ArrayList<Object>());
        int rowNum = 0;
        while (resultSet.next()) {
            Map mapOfColValues = (Map) this.rowMapper.mapRow(resultSet, rowNum++);
            Object searchCondition = mapOfColValues.get(SEARCH_CONDITION_FIELD);

            if (searchCondition == null) {
                continue;
            }
            String searchConditionString = searchCondition.toString().toLowerCase();
            if (searchConditionString.contains(" or ") || searchConditionString.contains(" and ")) {
                continue;
            }
            if (!searchConditionString.contains(SEARCH_CONDITION)) {
                continue;
            }

            resultList.add(mapOfColValues);
        }
        return resultList;
    }
}