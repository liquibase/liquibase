package liquibase.statement;

public class SelectFromDatabaseChangeLogStatement implements SqlStatement {

    private String[] columnsToSelect;
    private WhereClause whereClause;

    public SelectFromDatabaseChangeLogStatement(String... columnsToSelect) {
        this(null, columnsToSelect);
    }

    public SelectFromDatabaseChangeLogStatement(WhereClause whereClause, String... columnsToSelect) {
        this.columnsToSelect = columnsToSelect;
        this.whereClause = whereClause;
    }

    public String[] getColumnsToSelect() {
        return columnsToSelect;
    }

    public WhereClause getWhereClause() {
        return whereClause;
    }

    public static interface WhereClause {

    }

    public static class ByTag implements WhereClause {

        private String tagName;

        public ByTag(String tagName) {
            this.tagName = tagName;
        }

        public String getTagName() {
            return tagName;
        }
    }

}