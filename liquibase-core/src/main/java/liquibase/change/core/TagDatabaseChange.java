package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.TagDatabaseStatement;

@ChangeClass(name="tagDatabase", description = "Tag Database", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class TagDatabaseChange extends AbstractChange {

    private String tag;

    @ChangeProperty(requiredForDatabase = "all")
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new TagDatabaseStatement(tag)
        };
    }

    public String getConfirmationMessage() {
        return "Tag '"+tag+"' applied to database";
    }

    @Override
    protected Change[] createInverses() {
        return new Change[0];
    }
}
