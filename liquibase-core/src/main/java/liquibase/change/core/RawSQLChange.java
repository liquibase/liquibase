package liquibase.change.core;

import liquibase.change.AbstractSQLChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

/**
 * Allows execution of arbitrary SQL.  This change can be used when existing changes are either don't exist,
 * are not flexible enough, or buggy. 
 */
@DatabaseChange(name="sql",
        description = "The 'sql' tag allows you to specify whatever sql you want. It is useful for complex changes " +
            "that aren't supported through Liquibase's automated refactoring tags and to work around bugs and " +
            "limitations " +
            "of Liquibase. The SQL contained in the sql tag can be multi-line.\n" +
        "\n" +
        "The createProcedure refactoring is the best way to create stored procedures.\n" +
        "\n" +
        "The 'sql' tag can also support multiline statements in the same file. Statements can either be split " +
         "using a ; at the end of the last line of the SQL or a go on its own on the line between the statements " +
          "can be used.Multiline SQL statements are also supported and only a ; or go statement will finish a " +
           "statement, a new line is not enough. Files containing a single statement do not need to use a ; or go.\n" +
        "\n" +
        "The sql change can also contain comments of either of the following formats:\n" +
        "\n" +
        "A multiline comment that starts with /* and ends with */.\n" +
        "A single line comment starting with <space>--<space> and finishing at the end of the line\n" +
        "Note: By default it will attempt to split statements on a ';' or 'go' at the end of lines. Because of " +
         "this, if you have a comment or some other non-statement ending ';' or 'go', don't have it at the end of a " +
          "line or you will get invalid SQL.",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class RawSQLChange extends AbstractSQLChange {

    private String comment;
    
    public RawSQLChange() {
    }

    public RawSQLChange(String sql) {
        setSql(sql);
    }


    @Override
    @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE, exampleValue = "insert into person (name) values ('Bob')", requiredForDatabase = "all")
    public String getSql() {
        return super.getSql();
    }

    @DatabaseChangeProperty(serializationType = SerializationType.NESTED_OBJECT, exampleValue = "What about Bob?")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getConfirmationMessage() {
        return "Custom SQL executed";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        String nestedSql = StringUtils.trimToNull(parsedNode.getValue(String.class));
        if (nestedSql != null) {
            setSql(nestedSql);
        }
    }
}
