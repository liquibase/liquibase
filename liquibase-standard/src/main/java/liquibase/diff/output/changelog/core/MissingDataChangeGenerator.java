package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;
import liquibase.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Data;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtil;

import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class MissingDataChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Data.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[]{
                Table.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[]{
                PrimaryKey.class, ForeignKey.class, Index.class
        };
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl outputControl, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Data data = (Data) missingObject;

            Table table = data.getTable();
            if (referenceDatabase.isLiquibaseObject(table)) {
                return null;
            }

            String excludeObjects = outputControl.getExcludeObjects();
            String includeObjects = outputControl.getIncludeObjects();
            List<String> columnNames = Collections.emptyList();
            JdbcConnection connection = (JdbcConnection) referenceDatabase.getConnection();
            String sql = "SELECT * FROM " + referenceDatabase.escapeTableName(table.getSchema().getCatalogName(), table.getSchema().getName(), table.getName());

            if (StringUtils.isNotEmpty(excludeObjects) || StringUtils.isNotEmpty(includeObjects)) {
                String queryToRetrieveTableMetaDataOnly = sql + " WHERE 1=0";
                stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(1);
                rs = stmt.executeQuery(queryToRetrieveTableMetaDataOnly);
                boolean isCaseSensitive = referenceDatabase.isCaseSensitive();
                // filter which column names to use for export below
                columnNames = identifyColumnNames(rs, excludeObjects, includeObjects, isCaseSensitive);

                // the filtering has
                //   a) excluded ALL columns
                //   b) included NO columns
                if (columnNames.isEmpty()) {
                    throw new CommandExecutionException(String.format("No columns matched with excludeObjects '%s' / includeObjects '%s'", excludeObjects, includeObjects));
                }

                String replacement = "\"" + String.join("\", \"", columnNames) + "\"";
                // replace "*" in original SELECT statement with list of filtered column names
                sql = sql.replace("*", replacement);
            }

            stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(1000);
            rs = stmt.executeQuery(sql);

            // identify all column names
            if (columnNames.isEmpty()) {
                columnNames = new ArrayList<>();
                for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                    columnNames.add(rs.getMetaData().getColumnName(i + 1));
                }
            }

            List<Change> changes = new ArrayList<>();

            while (rs.next()) {
                InsertDataChange change = new InsertDataChange();
                if (outputControl.getIncludeCatalog()) {
                    change.setCatalogName(table.getSchema().getCatalogName());
                }
                if (outputControl.getIncludeSchema()) {
                    change.setSchemaName(table.getSchema().getName());
                }
                change.setTableName(table.getName());

                // loop over all columns for this row
                for (int i = 0; i < columnNames.size(); i++) {
                    ColumnConfig column = new ColumnConfig();
                    column.setName(columnNames.get(i));
                    Object value = JdbcUtil.getResultSetValue(rs, i + 1);

                    if (value == null) {
                        if (outputControl.getPreserveNullValues()) {
                            column.setValue(null);
                        }
                    } else if (value instanceof Number) {
                        column.setValueNumeric((Number) value);
                    } else if (value instanceof Boolean) {
                        column.setValueBoolean((Boolean) value);
                    } else if (value instanceof Date) {
                        column.setValueDate((Date) value);
                    } else if (value instanceof byte[]) {
                        if (referenceDatabase instanceof InformixDatabase) {
                            column.setValue(new String((byte[]) value, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
                        }
                        column.setValueComputed(new DatabaseFunction("UNSUPPORTED FOR DIFF: BINARY DATA"));
                    } else { // fall back to simple string
                        column.setValue(value.toString());
                    }

                    change.addColumn(column);
                }

                // for each row, add a new change
                // (there will be one group per table)
                changes.add(change);
            }

            return changes.toArray(EMPTY_CHANGE);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) {
                }
            }
        }
    }

    private List<String> identifyColumnNames (final ResultSet rs, String excludeObjects, String includeObjects, final boolean isCaseSensitive) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        excludeObjects = extractColumnInfo(excludeObjects);
        includeObjects = extractColumnInfo(includeObjects);
        boolean withExcludeOnly = Objects.nonNull(excludeObjects);
        boolean withIncludeOnly = Objects.nonNull(includeObjects);
        Pattern pattern = null;

        if (withExcludeOnly) {
            pattern = Pattern.compile(excludeObjects, isCaseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        }

        if (withIncludeOnly) {
            pattern = Pattern.compile(includeObjects, isCaseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        }

        boolean noFiltering = null == pattern;

        for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
            String columnName = rs.getMetaData().getColumnName(i);

            // only collect columns which are either explicitly "included" or "not excluded"
            if (noFiltering ||
                (withIncludeOnly && pattern.matcher(columnName).matches()) ||
                (withExcludeOnly && !pattern.matcher(columnName).matches()))
            {
                columnNames.add(isCaseSensitive ? "\\\"" + columnName + "\\\"" : columnName);
            }
        }

        return columnNames;
    }

    private String extractColumnInfo (final String s) {
        if (null == s) {
            return null;
        }

        int commaPos = s.indexOf(',');
        int colPos = s.indexOf("column:") + 7;

        if (6 < colPos) {
            if (commaPos < colPos) {
                return s.substring(colPos).trim();
            }
            return s.substring(colPos, commaPos).trim();
        }

        if (s.contains("table:")) {
            return null;
        }

        return s.trim();
    }
}
