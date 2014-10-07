package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;

public class SelectFromDatabaseChangeLogStatement extends AbstractSqlStatement {

    private ColumnConfig[] columnsToSelect;
    private WhereClause whereClause;
    private String[] orderByColumns;

    public SelectFromDatabaseChangeLogStatement(String... columnsToSelect) {
        if (columnsToSelect != null) {
            this.columnsToSelect = new ColumnConfig[columnsToSelect.length];
            for (int i = 0; i < columnsToSelect.length; i++) {
                this.columnsToSelect[i] = new ColumnConfig().setName(columnsToSelect[i]);
            }
        }
    }

    public SelectFromDatabaseChangeLogStatement(ColumnConfig... columnsToSelect) {
        this(null, columnsToSelect);
    }

    public SelectFromDatabaseChangeLogStatement(WhereClause whereClause, ColumnConfig... columnsToSelect) {
        this.columnsToSelect = columnsToSelect;
        this.whereClause = whereClause;
    }

    public ColumnConfig[] getColumnsToSelect() {
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