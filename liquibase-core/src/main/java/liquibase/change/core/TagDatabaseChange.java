package liquibase.change.core;

import liquibase.action.ActionStatus;
import liquibase.change.*;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;

@DatabaseChange(name="tagDatabase", description = "Applies a tag to the database for future rollback", priority = ChangeMetaData.PRIORITY_DEFAULT, since = "1.6")
public class TagDatabaseChange extends AbstractChange {

    private String tag;

    @DatabaseChangeProperty(description = "Tag to apply", exampleValue = "version_1.3")
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[0];
    }

    @Override
    public ActionStatus checkStatus(Database database) {
        try {
            return new ActionStatus().assertApplied(ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).tagExists(getTag()), "Database not tagged");
        } catch (DatabaseException e) {
            return new ActionStatus().unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Tag '"+tag+"' applied to database";
    }

    @Override
    protected Change[] createInverses() {
        return new Change[0];
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
