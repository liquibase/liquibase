package liquibase.change.core;

import liquibase.Scope;
import liquibase.change.*;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.MarkChangeSetRanGenerator;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import lombok.Setter;

@DatabaseChange(name = "tagDatabase", description = "Applies a tag to the database to specify where to stop a rollback",
    priority = ChangeMetaData.PRIORITY_DEFAULT, since = "1.6")
@Setter
public class TagDatabaseChange extends AbstractChange {

    private String tag;

    @DatabaseChangeProperty(description = "Tag to apply", exampleValue = "version_1.3")
    public String getTag() {
        return tag;
    }

    /**
     * {@inheritDoc}
     * @see MarkChangeSetRanGenerator#generateSql(MarkChangeSetRanStatement, Database, SqlGeneratorChain)
     */
    @Override
    public SqlStatement[] generateStatements(Database database) {
        return SqlStatement.EMPTY_SQL_STATEMENT;
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(
                Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).tagExists(getTag()), "Database not tagged");
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
        return EMPTY_CHANGE;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
