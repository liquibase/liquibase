package liquibase.change;

import liquibase.database.Database;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.statement.SqlStatement;
import liquibase.statement.TagDatabaseStatement;
import liquibase.util.StringUtils;

public class TagDatabaseChange extends AbstractChange{

    private String tag;

    public TagDatabaseChange() {
        super("tagDatabase", "Tag Database");
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(tag) == null) {
            throw new InvalidChangeDefinitionException("tag is required", this);
        }

    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
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
