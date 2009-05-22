package liquibase.change;

import liquibase.statement.SqlStatement;
import liquibase.database.Database;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class AnonymousChange extends AbstractChange {

    private List<SqlStatement> statements = new ArrayList<SqlStatement>();

    public AnonymousChange(SqlStatement... statement) {
        super("anonymous", "Anonymous change", ChangeMetaData.PRIORITY_DEFAULT);
        this.statements.addAll(Arrays.asList(statement));
    }

    public String getConfirmationMessage() {
        return statements.size()+" statements executed";
    }

    public SqlStatement[] generateStatements(Database database) {
        return statements.toArray(new SqlStatement[statements.size()]);
    }
}
