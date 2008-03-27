package liquibase.change;

import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.TagDatabaseStatement;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.Set;

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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        return new SqlStatement[] {
                new TagDatabaseStatement(tag)
        };
    }

    public String getConfirmationMessage() {
        return "Tag '"+tag+"' applied to database";
    }

    public Element createNode(Document currentChangeLogDOM) {
        Element sqlElement = currentChangeLogDOM.createElement(getTagName());
        sqlElement.setAttribute("tag", getTag());
        return sqlElement;

    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }

    protected Change[] createInverses() {
        return new Change[0];
    }
}
