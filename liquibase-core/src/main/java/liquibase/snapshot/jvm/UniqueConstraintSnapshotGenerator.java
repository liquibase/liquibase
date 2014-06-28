package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.*;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.sql.SQLException;
import java.util.*;

public class UniqueConstraintSnapshotGenerator extends JdbcSnapshotGenerator {

    public UniqueConstraintSnapshotGenerator() {
        super(UniqueConstraint.class, new Class[]{Table.class});
    }


    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SQLiteDatabase) {
            return PRIORITY_NONE;
        }
        return super.getPriority(objectType, database);
    }


    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        UniqueConstraint exampleConstraint = (UniqueConstraint) example;
        Table table = exampleConstraint.getTable();

        List<Map<String, ?>> metadata = listColumns(exampleConstraint, database);

        if (metadata.size() == 0) {
            return null;
        }
        UniqueConstraint constraint = new UniqueConstraint();
        constraint.setTable(table);
        constraint.setName(example.getName());
        for (Map<String, ?> col : metadata) {
            constraint.getColumns().add((String) col.get("COLUMN_NAME"));
        }

        return constraint;
    }


    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {

        if (!snapshot.getSnapshotControl().shouldInclude(UniqueConstraint.class)) {
            return;
        }

        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            Database database = snapshot.getDatabase();
            Schema schema;
            schema = table.getSchema();

            List<CachedRow> metadata = null;
            try {
                metadata = listConstraints(table, snapshot, schema);
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }


            Set<String> seenConstraints = new HashSet<String>();

            for (CachedRow constraint : metadata) {
                UniqueConstraint uq = new UniqueConstraint().setName(cleanNameFromDatabase((String) constraint.get("CONSTRAINT_NAME"), database)).setTable(table);
                if (seenConstraints.add(uq.getName())) {
                    table.getUniqueConstraints().add(uq);
                }
            }
        }
    }

    protected List<CachedRow> listConstraints(Table table, DatabaseSnapshot snapshot, Schema schema) throws DatabaseException, SQLException {
        return ((JdbcDatabaseSnapshot) snapshot).getMetaData().getUniqueConstraints(schema.getCatalogName(), schema.getName(), table.getName());
    }

    protected List<Map<String, ?>> listColumns(UniqueConstraint example, Database database) throws DatabaseException {
        Table table = example.getTable();
        Schema schema = table.getSchema();
        String name = example.getName();

        String sql = null;
        if (database instanceof MySQLDatabase || database instanceof HsqlDatabase) {
            sql = "select const.CONSTRAINT_NAME, COLUMN_NAME " +
                    "from "+database.getSystemSchema()+".table_constraints const " +
                    "join "+database.getSystemSchema()+".key_column_usage col " +
                    "on const.constraint_schema=col.constraint_schema " +
                    "and const.table_name=col.table_name " +
                    "and const.constraint_name=col.constraint_name " +
                    "where const.constraint_schema='" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' " +
                    "and const.table_name='" + database.correctObjectName(example.getTable().getName(), Table.class) + "' " +
                    "and const.constraint_name='" + database.correctObjectName(name, UniqueConstraint.class) + "'" +
                    "order by ordinal_position";
        } else if (database instanceof PostgresDatabase) {
                sql = "select const.CONSTRAINT_NAME, COLUMN_NAME " +
                        "from "+database.getSystemSchema()+".table_constraints const " +
                        "join "+database.getSystemSchema()+".key_column_usage col " +
                        "on const.constraint_schema=col.constraint_schema " +
                        "and const.table_name=col.table_name " +
                        "and const.constraint_name=col.constraint_name " +
                        "where const.constraint_catalog='" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' " +
                        "and const.constraint_schema='" + database.correctObjectName(schema.getSchema().getName(), Schema.class) + "' " +
                        "and const.table_name='" + database.correctObjectName(example.getTable().getName(), Table.class) + "' " +
                        "and const.constraint_name='" + database.correctObjectName(name, UniqueConstraint.class) + "'" +
                        "order by ordinal_position";
        } else if (database instanceof MSSQLDatabase) {
            sql = "select TC.CONSTRAINT_NAME as CONSTRAINT_NAME, CC.COLUMN_NAME as COLUMN_NAME from INFORMATION_SCHEMA.TABLE_CONSTRAINTS TC " +
                    "inner join INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE CC on TC.CONSTRAINT_NAME = CC.CONSTRAINT_NAME " +
                    "where TC.CONSTRAINT_SCHEMA='" + database.correctObjectName(schema.getName(), Schema.class) + "' " +
                    "and TC.TABLE_NAME='" + database.correctObjectName(example.getTable().getName(), Table.class) + "' " +
                    "and TC.CONSTRAINT_NAME='" + database.correctObjectName(name, UniqueConstraint.class) + "'" +
                    "order by TC.CONSTRAINT_NAME";
        } else if (database instanceof OracleDatabase) {
            sql = "select ucc.column_name from user_cons_columns ucc where ucc.constraint_name='"+database.correctObjectName(name, UniqueConstraint.class)+"' order by ucc.position";
        } else if (database instanceof DB2Database) {
            sql = "select k.colname as column_name from syscat.keycoluse k, syscat.tabconst t " +
                    "where k.constname = t.constname " +
                    "and t.type='U' " +
                    "and k.constname='"+database.correctObjectName(name, UniqueConstraint.class)+"' "+
                    "order by colseq";
        } else if (database instanceof DerbyDatabase) {
            sql = "SELECT cg.descriptor as descriptor, t.tablename " +
                    "FROM sys.sysconglomerates cg "+
                    "JOIN sys.syskeys k ON cg.conglomerateid = k.conglomerateid "+
                    "JOIN sys.sysconstraints c ON c.constraintid = k.constraintid " +
                    "JOIN sys.systables t ON c.tableid = t.tableid "+
                    "WHERE c.constraintname='"+database.correctObjectName(name, UniqueConstraint.class)+"'";
            List<Map<String, ?>> rows = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(sql));

            List<Map<String, ?>> returnList = new ArrayList<Map<String, ?>>();
            if (rows.size() == 0) {
                return returnList;
            } else if (rows.size() > 1) {
                throw new UnexpectedLiquibaseException("Got multiple rows back querying unique constraints");
            } else {
                Map rowData = rows.get(0);
                String descriptor = rowData.get("DESCRIPTOR").toString();
                descriptor = descriptor.replaceFirst(".*\\(","").replaceFirst("\\).*","");
                for (String columnNumber : StringUtils.splitAndTrim(descriptor, ",")) {
                    String columnName = (String) ExecutorService.getInstance().getExecutor(database).queryForObject(new RawSqlStatement(
                            "select c.columnname from sys.syscolumns c " +
                                    "join sys.systables t on t.tableid=c.referenceid " +
                                    "where t.tablename='"+rowData.get("TABLENAME")+"' and c.columnnumber=" + columnNumber), String.class);

                    Map<String, String> row = new HashMap<String, String>();
                    row.put("COLUMN_NAME", columnName);
                    returnList.add(row);
                }
                return returnList;
            }



        } else if (database instanceof FirebirdDatabase) {
            sql = "SELECT RDB$INDEX_SEGMENTS.RDB$FIELD_NAME AS column_name " +
                    "FROM RDB$INDEX_SEGMENTS " +
                    "LEFT JOIN RDB$INDICES ON RDB$INDICES.RDB$INDEX_NAME = RDB$INDEX_SEGMENTS.RDB$INDEX_NAME " +
                    "WHERE UPPER(RDB$INDICES.RDB$INDEX_NAME)='"+database.correctObjectName(name, UniqueConstraint.class)+"' " +
                    "ORDER BY RDB$INDEX_SEGMENTS.RDB$FIELD_POSITION";
        } else {
            String catalogName = database.correctObjectName(schema.getCatalogName(), Catalog.class);
            String schemaName = database.correctObjectName(schema.getName(), Schema.class);
            String constraintName = database.correctObjectName(name, UniqueConstraint.class);
            String tableName = database.correctObjectName(table.getName(), Table.class);
            sql = "select CONSTRAINT_NAME, COLUMN_LIST as COLUMN_NAME " +
                    "from "+database.getSystemSchema()+".constraints " +
                    "where constraint_type='UNIQUE' ";
            if (catalogName != null) {
                sql += "and constraint_catalog='" + catalogName + "' ";
            }
            if (schemaName != null) {
                sql += "and constraint_schema='" + schemaName + "' ";
            }
            if (tableName != null) {
                sql += "and table_name='" + tableName + "' ";
            }
            if (constraintName != null) {
                sql += "and constraint_name='" + constraintName + "'";
            }
        }
        return ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(sql));
    }
}
