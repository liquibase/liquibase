package liquibase.sdk;

import java.util.Arrays;
import java.util.List;

public class TestExamples {
    public static List<String> getTableNames() {
        return Arrays.asList("table_name", "other_table", "CAPITAL_TABLE", "Mixed_Case_Table"
//                "table_name (with parentheses)", "crazy!@#$%^&*()_-+={[}]|\\:;\"'<,>.?/`~"
        );
    }
}
