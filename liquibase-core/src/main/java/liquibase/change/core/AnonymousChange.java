package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.DatabaseChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@DatabaseChange(name="anonymous", description = "Anonymous change", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class AnonymousChange extends AbstractChange {

    @DatabaseChangeProperty(isChangeProperty = false)
    private List<SqlStatement> statements = new ArrayList<SqlStatement>();

    public AnonymousChange() {
        this(new SqlStatement[0]);
    }

    public AnonymousChange(SqlStatement... statement) {
        this.statements.addAll(Arrays.asList(statement));
    }

    public String getConfirmationMessage() {
        return statements.size()+" statements executed";
    }

    public SqlStatement[] generateStatements(Database database) {
        return statements.toArray(new SqlStatement[statements.size()]);
    }
}
