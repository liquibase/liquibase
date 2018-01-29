package liquibase.executor.jvm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter implementation of the ResultSetExtractor interface that delegates
 * to a RowMapper which is supposed to create an object for each row.
 * Each object is added to the results List of this ResultSetExtractor.
 * <p/>
 * <p>Useful for the typical case of one object per row in the database table.
 * The number of entries in the results list will match the number of rows.
 * <p/>
 * <p>Note that a RowMapper object is typically stateless and thus reusable;
 * just the RowMapperResultSetExtractor adapter is stateful.
 * <p/>
 * <p>A usage example with JdbcTemplate:
 * <p/>
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // reusable object
 * RowMapper rowMapper = new UserRowMapper();  // reusable object
 * <p/>
 * List allUsers = (List) jdbcTemplate.query(
 * "select * from user",
 * new RowMapperResultSetExtractor(rowMapper, 10));
 * <p/>
 * User user = (User) jdbcTemplate.queryForObject(
 * "select * from user where id=?", new Object[] {id},
 * new RowMapperResultSetExtractor(rowMapper, 1));</pre>
 * <p/>
 *
 * @author Spring Framework
 * @see RowMapper
 * @see liquibase.executor.Executor
 */
@SuppressWarnings({"unchecked"})
public class RowMapperResultSetExtractor implements ResultSetExtractor {

    private final RowMapper rowMapper;

    private final int rowsExpected;


    /**
     * Create a new RowMapperResultSetExtractor.
     *
     * @param rowMapper the RowMapper which creates an object for each row
     */
    public RowMapperResultSetExtractor(RowMapper rowMapper) {
        this(rowMapper, 0);
    }

    /**
     * Create a new RowMapperResultSetExtractor.
     *
     * @param rowMapper    the RowMapper which creates an object for each row
     * @param rowsExpected the number of expected rows
     *                     (just used for optimized collection handling)
     */
    public RowMapperResultSetExtractor(RowMapper rowMapper, int rowsExpected) {
        this.rowMapper = rowMapper;
        this.rowsExpected = rowsExpected;
    }


    @Override
    public Object extractData(ResultSet rs) throws SQLException {
        List results = ((this.rowsExpected > 0) ? new ArrayList(this.rowsExpected) : new ArrayList());
        int rowNum = 0;
        while (rs.next()) {
            results.add(this.rowMapper.mapRow(rs, rowNum++));
        }
        return results;
    }

}
