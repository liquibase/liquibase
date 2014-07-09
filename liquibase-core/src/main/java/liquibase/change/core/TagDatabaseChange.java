package liquibase.change.core;

import liquibase.change.*;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.exception.DatabaseException;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.Statement;
import liquibase.statement.core.TagDatabaseStatement;

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
    public Statement[] generateStatements(ExecutionEnvironment env) {
        return new Statement[] {
                new TagDatabaseStatement(tag)
        };
    }

    @Override
    public ChangeStatus checkStatus(ExecutionEnvironment env) {
        try {
            return new ChangeStatus().assertComplete(ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(env.getTargetDatabase()).tagExists(getTag()), "Database not tagged");
        } catch (DatabaseException e) {
            return new ChangeStatus().unknown(e);
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
