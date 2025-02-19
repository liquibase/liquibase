package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.statement.AbstractSqlStatement;
import lombok.Data;
import lombok.Getter;

public class SelectFromDatabaseChangeLogStatement extends AbstractSqlStatement {

    private ColumnConfig[] columnsToSelect;
    private WhereClause whereClause;
    private String[] orderByColumns;
    private Integer limit;

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

    public Integer getLimit() {
        return limit;
    }

    public SelectFromDatabaseChangeLogStatement setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public interface WhereClause {

        String generateSql(Database database);

    }

    public static class ByNotNullCheckSum implements WhereClause {

        @Override
        public String generateSql(Database database) {
            return String.format(" WHERE %s IS NOT NULL", database.escapeColumnName(null, null, null, "MD5SUM"));
        }
    }

    @Getter
    public static class ByTag implements WhereClause {

        private final String tagName;

        public ByTag(String tagName) {
            this.tagName = tagName;
        }

        @Override
        public String generateSql(Database database) {
            return String.format(" WHERE %s='%s'", database.escapeColumnName(null, null, null, "TAG"), getTagName());
        }
    }

    @Data
    public static class ByCheckSumNotNullAndNotLike implements WhereClause {
        private final int notLikeCheckSumVersion;

        @Override
        public String generateSql(Database database) {
            final String md5SUMColumnName = database.escapeColumnName(null, null, null, "MD5SUM");
            return String.format(" WHERE %s IS NOT NULL AND %s NOT LIKE '%d:%%'", md5SUMColumnName, md5SUMColumnName, notLikeCheckSumVersion);
        }
    }

    @Data
    public static class GroupByIdAuthorFilename implements WhereClause {
        @Override
        public String generateSql(Database database) {
            final String idColumnName = database.escapeColumnName(null, null, null, "ID");
            final String authorColumnName = database.escapeColumnName(null, null, null, "AUTHOR");
            final String fileNameColumnName = database.escapeColumnName(null, null, null, "FILENAME");
            final String exectype = database.escapeColumnName(null, null, null, "EXECTYPE");
            final String md5SUMColumnName = database.escapeColumnName(null, null, null, "MD5SUM");
            final String descriptionColumnName = database.escapeColumnName(null, null, null, "DESCRIPTION");
            final String commentsColumnName = database.escapeColumnName(null, null, null, "COMMENTS");
            final String tagColumnName = database.escapeColumnName(null, null, null, "TAG");
            final String contextsColumnName = database.escapeColumnName(null, null, null, "CONTEXTS");
            final String labelsColumnName = database.escapeColumnName(null, null, null, "LABELS");
            return String.format(" GROUP BY %s, %s, %s, %s, %s, %s, %s, %s, %s, %s", idColumnName, authorColumnName, fileNameColumnName, exectype,
                    md5SUMColumnName, descriptionColumnName, commentsColumnName, tagColumnName, contextsColumnName, labelsColumnName);
        }
    }
}
