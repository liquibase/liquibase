/*
 * $Id: FullyQualifiedBeanNameGenerator.java 491374 2014-05-19 12:37:41Z daniel.warmuth $
 *
 * Copyright: MICROS Retail Deutschland GmbH
 *            Salzufer 8
 *            10587 Berlin
 *            Germany
 *
 * http://www.micros-retail.com
 *
 * All Rights Reserved!
 */
package liquibase.snapshot.jvm.metainformation;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

import com.sun.istack.internal.Nullable;
import java.util.List;

/**
 * @author <a href="mailto:marcel.scheeler@micros.com">marcel.scheeler</a>
 * @version $Revision: 491374 $ $Date: 2014-05-19 14:37:41 +0200 (Mo, 19 Mai 2014) $ $Author: daniel.warmuth $
 */
public class MSSQLDatabasRemarkProvider {

    /** Get remarks columns */
    @Nullable
    public String getRemark(Column column, Database database, Schema schema, Relation relation) throws DatabaseException {
        return getRemarkValue(database, getColumnRemarkQuery(column, database, schema, relation));
    }

    @Nullable
    public String getRemark(Table table, Database database, String schemaName) throws DatabaseException {
        String tableName = database.escapeStringForDatabase(database.escapeTableName(null, schemaName, table.getName()));
        return getRemarkValue(database, getTableRemarkQuery(database, tableName));
    }

    private String getColumnRemarkQuery(Column column, Database database, Schema schema, Relation relation) throws DatabaseException {
        String tableName = database.escapeStringForDatabase(database.escapeTableName(schema.getCatalogName(), schema.getName(), relation.getName()));
        String columnName = database.escapeStringForDatabase(column.getName());
        String sql;
        if (isMsql2006OrGreater(database)) {
            // SQL Server 2005 or later
            // https://technet.microsoft.com/en-us/library/ms177541.aspx
            sql =
                    "SELECT CAST([ep].[value] AS [nvarchar](MAX)) AS [REMARKS] " +
                            "FROM [sys].[extended_properties] AS [ep] " +
                            "WHERE [ep].[class] = 1 " +
                            "AND [ep].[major_id] = OBJECT_ID(N'" + tableName + "') " +
                            "AND [ep].[minor_id] = COLUMNPROPERTY([ep].[major_id], N'" + columnName + "', 'ColumnId') " +
                            "AND [ep].[name] = 'MS_Description'";
        } else {
            // SQL Server 2000
            // https://technet.microsoft.com/en-us/library/aa224810%28v=sql.80%29.aspx
            sql =
                    "SELECT CAST([p].[value] AS [nvarchar]) AS [REMARKS] " +
                            "FROM [dbo].[sysproperties] AS [p] " +
                            "WHERE [p].[id] = OBJECT_ID(N'" + tableName + "') " +
                            "AND [p].[smallid] = COLUMNPROPERTY([p].[id], N'" + columnName + "', 'ColumnId') " +
                            "AND [p].[type] = 4 " +
                            "AND [p].[name] = 'MS_Description'";
        }
        return sql;
    }

    private String getTableRemarkQuery(Database database, String tableName) throws DatabaseException {
        String sql;
        if (isMsql2006OrGreater(database)) {
            sql = "SELECT CAST(value as varchar(max)) as REMARKS\n" +
                    "FROM sys.extended_properties\n" +
                    "WHERE\n" +
                    " name='MS_Description' " +
                    " AND major_id = OBJECT_ID('" + tableName + "')\n" +
                    " AND\n" +
                    " minor_id = 0";
        } else {
            sql = "SELECT CAST(value as varchar) as REMARKS\n" +
                    "FROM dbo.sysproperties\n" +
                    "WHERE\n" +
                    " name='MS_Description' " +
                    " AND id = OBJECT_ID('" + tableName + "')\n" +
                    " AND\n" +
                    " smallid = 0";
        }
        return sql;
    }

    private String getRemarkValue(Database database, String sql) throws DatabaseException {
        List<String> values = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(sql), String.class);
        if (values != null && values.size() > 0) {
            return StringUtils.trimToNull(values.get(0));
        }
        return null;
    }

    private boolean isMsql2006OrGreater(Database database) throws DatabaseException {
        return database.getDatabaseMajorVersion() >= 9;
    }
}
