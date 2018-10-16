package liquibase.change.core;

import liquibase.change.ColumnConfig;
import liquibase.exception.UnexpectedLiquibaseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LoadDataChangeUnitTest {

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
