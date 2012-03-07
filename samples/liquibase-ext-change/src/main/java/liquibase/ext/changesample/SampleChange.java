package liquibase.ext.changesample;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeClass;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.SqlStatement;

@ChangeClass(name="sampleChange", description = "Sample Change", priority = 15)
public class SampleChange extends AbstractChange {

    public String getConfirmationMessage() {
        return "Sample Change executed";
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new CreateTableStatement(null, null, "samplechange").addColumn("id", DataTypeFactory.getInstance().fromDescription("int"))
                    .addColumn("name", DataTypeFactory.getInstance().fromDescription("varchar(5)"))
        };
    }
}
