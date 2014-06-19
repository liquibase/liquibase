package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;

public class SelectFromDatabaseChangeLogStatement extends AbstractSqlStatement {

    private String[] columnsToSelect;
    private WhereClause whereClause;
    private String[] orderByColumns;

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

    public String[] getOrderByColumns() {
        return orderByColumns;
    }

    public SelectFromDatabaseChangeLogStatement setOrderBy(String... columns) {
        this.orderByColumns = columns;

        return this;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }

    public static interface WhereClause {

    }

    public static class ByNotNullCheckSum implements WhereClause {

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