package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class SelectFromDatabaseChangeLogStatement extends AbstractSqlStatement {

    private SelectableColumn[] columnsToSelect;
    private WhereClause whereClause;
    private OrderByColumn[] orderByColumns;

    public SelectFromDatabaseChangeLogStatement(SelectableColumn... columnsToSelect) {
        this(null, columnsToSelect);
    }

    public SelectFromDatabaseChangeLogStatement(String... columnsToSelect) {
        this(null, columnsToSelect);
    }

    public SelectFromDatabaseChangeLogStatement(WhereClause whereClause, SelectableColumn... columnsToSelect) {
        this.columnsToSelect = columnsToSelect;
        this.whereClause = whereClause;
    }

    public SelectFromDatabaseChangeLogStatement(WhereClause whereClause, String... columnsToSelect) {
        this.columnsToSelect = new SelectableColumn[columnsToSelect.length];
        for (int i = 0; i < columnsToSelect.length; i ++) {
            this.columnsToSelect[i] = new SelectableColumn(columnsToSelect[i]);
        }
        this.whereClause = whereClause;
    }

    public SelectableColumn[] getColumnsToSelect() {
        return columnsToSelect;
    }

    public WhereClause getWhereClause() {
        return whereClause;
    }

    public OrderByColumn[] getOrderByColumns() {
        return orderByColumns;
    }

    public SelectFromDatabaseChangeLogStatement setOrderBy(OrderByColumn... columns) {
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

    public static class SelectableColumn {
        private String columnName;
        private String columnFunction;

        public SelectableColumn(final String columnName) {
            this.columnName = columnName;
        }

        public SelectableColumn(final String columnName, final String columnFunction) {
            this.columnName = columnName;
            this.columnFunction = columnFunction;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getColumnFunction() {
            return columnFunction;
        }
    }

    public static class OrderByColumn {
        private String columnName;
        private String orderByClause;

        public OrderByColumn(final String columnName, final String orderByClause) {
            this.columnName = columnName;
            this.orderByClause = orderByClause;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getOrderByClause() {
            return orderByClause;
        }
    }

}