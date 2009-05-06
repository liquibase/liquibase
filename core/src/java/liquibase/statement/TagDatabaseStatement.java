package liquibase.statement;

public class TagDatabaseStatement implements SqlStatement {

    private String tag;

    public TagDatabaseStatement(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
