package liquibase.ext.sample2;

import liquibase.change.AbstractChange;
import liquibase.database.Database;
import liquibase.statement.CreateTableStatement;
import liquibase.statement.SqlStatement;

public class Sample2Change extends AbstractChange {
    public Sample2Change() {
        super("sample2", "Sample 2", 15);
    }

    public String getConfirmationMessage() {
        return "Sample 2 executed";
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new CreateTableStatement(null, "sample2").addColumn("id", "int").addColumn("name", "varchar(5)")
        };
    }
}
