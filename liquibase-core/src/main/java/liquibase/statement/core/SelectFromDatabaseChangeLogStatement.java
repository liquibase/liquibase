package liquibase.statement.core;

import liquibase.AbstractExtensibleObject;
import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

public class SelectFromDatabaseChangeLogStatement extends AbstractStatement {

    public static final String COLUMNS_TO_SELECT = "columnsToSelect";
    public static final String ORDER_BY=  "orderBy";
    public static final String WHERE_CLAUSE = "whereClause";

    public SelectFromDatabaseChangeLogStatement(String... columnsToSelect) {
        this(null, columnsToSelect);
    }

    public SelectFromDatabaseChangeLogStatement(WhereClause whereClause, String... columnsToSelect) {
        setColumnsToSelect(columnsToSelect);
        setWhereClause(whereClause);
    }

    public String[] getColumnsToSelect() {
        return getAttribute(COLUMNS_TO_SELECT, String[].class);
    }

    public SelectFromDatabaseChangeLogStatement setColumnsToSelect(String... columnsToSelect) {
        if (columnsToSelect != null && columnsToSelect.length == 0) {
            columnsToSelect = null;
        }
        return (SelectFromDatabaseChangeLogStatement) setAttribute(COLUMNS_TO_SELECT, columnsToSelect);
    }

    public WhereClause getWhereClause() {
        return getAttribute(WHERE_CLAUSE, WhereClause.class);
    }

    public SelectFromDatabaseChangeLogStatement setWhereClause(WhereClause whereClause) {
        return (SelectFromDatabaseChangeLogStatement) setAttribute(WHERE_CLAUSE, whereClause);
    }

    public String[] getOrderBy() {
        return getAttribute(ORDER_BY, String[].class);
    }

    public SelectFromDatabaseChangeLogStatement setOrderBy(String... orderByColumns) {
        if (orderByColumns != null && orderByColumns.length == 0) {
            orderByColumns = null;
        }
        return (SelectFromDatabaseChangeLogStatement) setAttribute(ORDER_BY, orderByColumns);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }

    public static interface WhereClause {

    }

    public static class ByNotNullCheckSum extends AbstractExtensibleObject implements WhereClause {

    }

    public static class ByTag extends AbstractExtensibleObject implements WhereClause {

        public static final String TAG_NAME = "tagName";

        public ByTag(String tagName) {
            setTagName(tagName);
        }

        public String getTagName() {
            return getAttribute(TAG_NAME, String.class);
        }

        public ByTag setTagName(String tagName) {
            return (ByTag) setAttribute(TAG_NAME, tagName);
        }


    }

}