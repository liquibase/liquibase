package liquibase.change.core;

import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.LoggingExecutor;
import liquibase.executor.jvm.JdbcExecutor;
import liquibase.sdk.executor.MockExecutor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LoadDataChangeUnitTest {

    /**
     * Class allowing specified exception to be thrown to test catch clause handling in handleSelect().
     */
    private class MockExecutorException extends JdbcExecutor {
        private DatabaseException exception = null;

        public void setException(DatabaseException e) {
            this.exception = e;
        }

        @Override
        public <T> T queryForObject(SqlStatement sql, Class<T> requiredType, List<SqlVisitor> sqlVisitors) throws
                DatabaseException {
            if (exception == null) {
                try {
                    if (requiredType == String.class) {
                        return (T) sql.toString();
                    } else {
                        return (T) requiredType.getDeclaredConstructor().newInstance();
                    }
                } catch (Exception e) {
                    throw new DatabaseException(e);
                }
            } else {
                throw exception;
            }
        }
    }

    @Test
    public void test_handleSelect_NULL() {
        LoadDataChange loadDataChange = new LoadDataChange();
        Object val = loadDataChange.handleSelect(null, null, null, "NULL", null);
        assertEquals("NULL", val);
        val = loadDataChange.handleSelect(null, null, null, "Null", null);
        assertEquals("NULL", val);
        val = loadDataChange.handleSelect(null, null, null, "null", null);
        assertEquals("NULL", val);
    }

    @Test
    public void test_handleSelect_happyPath_jdbcExecutor() {
        LoadDataChange loadDataChange = new LoadDataChange();

        LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
        columnConfig.setName("col1");
        columnConfig.setSelect("select '${varname}' from b");

        String[] headers = new String[]{"varname", "col1", "col2"};
        String[] line = new String[]{"varval", null, "val2"};

        Executor executor = new MockExecutorException();
        Object ret = loadDataChange.handleSelect(columnConfig, headers, line, "", executor);
        assertNotNull(ret);
        assertTrue(ret instanceof String);
        assertEquals("select 'varval' from b", ret);
    }

    @Test
    public void test_handleSelect_happyPath_loggingExecutor() {
        LoadDataChange loadDataChange = new LoadDataChange();

        LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
        columnConfig.setName("col1");
        columnConfig.setSelect("select '${varname}' from b");

        String[] headers = new String[]{"varname", "col1", "col2"};
        String[] line = new String[]{"valval", null, "val3"};

        Executor executor = new MockExecutor();
        // Double check that MockExecutor is a LoggingExecutor, in case it changes in future which would
        // invalidate this test
        assertTrue(executor instanceof LoggingExecutor);
        Object ret = loadDataChange.handleSelect(columnConfig, headers, line, "", executor);
        assertNotNull(ret);
        assertTrue(ret instanceof String);
        assertEquals("selectResult", ret);
    }

    @Test
    public void test_handle_select_exception_IGNORE_value() {
        LoadDataChange loadDataChange = new LoadDataChange();

        LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
        columnConfig.setName("col1");
        columnConfig.setSelect("select '${varname}' from b");

        String[] headers = new String[]{"varname", "col1", "col2"};
        String[] line = new String[]{"varval", "Ignore", "val2"};

        MockExecutorException executor = new MockExecutorException();
        executor.setException(new DatabaseException("junk"));
        Object ret = loadDataChange.handleSelect(columnConfig, headers, line, "Ignore", executor);
        assertNotNull(ret);
        assertTrue(ret instanceof String);
        assertEquals("NULL", ret);
    }

    @Test
    public void test_handle_select_exception_NOINSERT_value() {
        LoadDataChange loadDataChange = new LoadDataChange();

        LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
        columnConfig.setName("col1");
        columnConfig.setSelect("select '${varname}' from b");

        String[] headers = new String[]{"varname", "col1", "col2"};
        String[] line = new String[]{"varval", "NoInsert", "val2"};

        MockExecutorException executor = new MockExecutorException();
        executor.setException(new DatabaseException("junk"));
        Object ret = loadDataChange.handleSelect(columnConfig, headers, line, "NoInsert", executor);
        assertNotNull(ret);
        assertTrue(ret instanceof String);
        assertEquals("NO_INSERT_RESULT", ret);
    }

    @Test
    public void test_handle_select_exception_ERROR_value() {
        LoadDataChange loadDataChange = new LoadDataChange();

        LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
        columnConfig.setName("col1");
        columnConfig.setSelect("select '${varname}' from b");

        String[] headers = new String[]{"varname", "col1", "col2"};
        String[] line = new String[]{"varval", "ERROR", "val2"};

        MockExecutorException executor = new MockExecutorException();
        executor.setException(new DatabaseException("junk"));
        try {
            loadDataChange.handleSelect(columnConfig, headers, line, "ERROR", executor);
            fail("Expected UnexpectedLiquibaseException, but no exception was thrown");
        } catch (UnexpectedLiquibaseException e) {
            assertEquals("'select' statement >select 'varval' from b< failed, message junk", e.getMessage());
        } catch (Exception e) {
            fail("Expected UnexpectedLiquibase Exception, encountered " + e.getMessage());
        }
    }

    @Test
    public void test_handle_select_exception_IGNORE_colConfig() {
        LoadDataChange loadDataChange = new LoadDataChange();

        LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
        columnConfig.setName("col1");
        columnConfig.setSelectNoResults("Ignore");
        columnConfig.setSelect("select '${varname}' from b");

        String[] headers = new String[]{"varname", "col1", "col2"};
        String[] line = new String[]{"varval", null, "val2"};

        MockExecutorException executor = new MockExecutorException();
        executor.setException(new DatabaseException("junk"));
        Object ret = loadDataChange.handleSelect(columnConfig, headers, line, "", executor);
        assertNotNull(ret);
        assertTrue(ret instanceof String);
        assertEquals("NULL", ret);
    }

    @Test
    public void test_handle_select_exception_NOINSERT_colConfig() {
        LoadDataChange loadDataChange = new LoadDataChange();

        LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
        columnConfig.setName("col1");
        columnConfig.setSelectNoResults("NoInsert");
        columnConfig.setSelect("select '${varname}' from b");

        String[] headers = new String[]{"varname", "col1", "col2"};
        String[] line = new String[]{"varval", null, "val2"};

        MockExecutorException executor = new MockExecutorException();
        executor.setException(new DatabaseException("junk"));
        Object ret = loadDataChange.handleSelect(columnConfig, headers, line, "", executor);
        assertNotNull(ret);
        assertTrue(ret instanceof String);
        assertEquals("NO_INSERT_RESULT", ret);
    }

    @Test
    public void test_handle_select_exception_null() {
        LoadDataChange loadDataChange = new LoadDataChange();

        LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
        columnConfig.setName("col1");
        columnConfig.setSelect("select '${varname}' from b");

        String[] headers = new String[]{"varname", "col1", "col2"};
        String[] line = new String[]{"varval", null, "val2"};

        MockExecutorException executor = new MockExecutorException();
        executor.setException(new DatabaseException("junk"));
        try {
            loadDataChange.handleSelect(columnConfig, headers, line, "", executor);
            fail("Expected UnexpectedLiquibaseException, but no exception was thrown");
        } catch (UnexpectedLiquibaseException e) {
            assertEquals("'select' statement >select 'varval' from b< failed, message junk", e.getMessage());
        } catch (Exception e) {
            fail("Expected UnexpectedLiquibase Exception, encountered " + e.getMessage());
        }
    }

    @Test
    public void test_formulateSelectSql() {
        LoadDataChange loadDataChange = new LoadDataChange();
        LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
        String[] headers = new String[]{"var", "col1", "col2"};
        String[] line = new String[]{"val1", "val2", "val3"};

        columnConfig.setSelect("select var from b");
        String res = loadDataChange.formulateSelectSql(columnConfig, headers, line);
        assertEquals("select var from b", res);

        columnConfig.setSelect("select ${var from b");
        res = loadDataChange.formulateSelectSql(columnConfig, headers, line);
        assertEquals("select ${var from b", res);

        columnConfig.setSelect("select ${var} from b");
        res = loadDataChange.formulateSelectSql(columnConfig, headers, line);
        assertEquals("select val1 from b", res);

        columnConfig.setSelect("select ${var} from ${var}");
        res = loadDataChange.formulateSelectSql(columnConfig, headers, line);
        assertEquals("select val1 from val1", res);

        columnConfig.setSelect("select ${junk} from b");
        try {
            loadDataChange.formulateSelectSql(columnConfig, headers, line);
            fail("Expected an exception while trying to replace variable not defined in headers");
        } catch (UnexpectedLiquibaseException e) {
            // expected
        } catch (Exception e) {
            fail("Unexpected exception when trying to replace variable not defined in headers");
        }
    }
}
