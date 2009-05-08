package liquibase.change;

import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.TagDatabaseStatement;

public class TagDatabaseChange extends AbstractChange{

    private String tag;

    public TagDatabaseChange() {
        super("tagDatabase", "Tag Database", ChangeMetaData.PRIORITY_DEFAULT);
    }

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

    protected Change[] createInverses() {
        return new Change[0];
    }
}
