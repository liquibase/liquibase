package liquibase.database.sql;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.SybaseDatabase;
import liquibase.util.SqlUtil;
import liquibase.util.StringUtils;

import java.util.*;

public class CreateTableStatement implements SqlStatement {
    private String tableName;
    private String tablespace;
    private List<String> columns = new ArrayList<String>();
    private Set<String> autoIncrementColumns = new HashSet<String>();
    private Map<String, String> columnTypes = new HashMap<String, String>();
    private Map<String, String> defaultValues = new HashMap<String, String>();

    private PrimaryKeyConstraint primaryKeyConstraint;
    private Set<String> notNullColumns = new HashSet<String>();
    private Set<ForeignKeyConstraint> foreignKeyConstraints = new HashSet<ForeignKeyConstraint>();
    private Set<UniqueConstraint> uniqueConstraints = new HashSet<UniqueConstraint>();


    public CreateTableStatement(String tableName) {
        this.tableName = tableName;
    }


    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }


    public PrimaryKeyConstraint getPrimaryKeyConstraint() {
        return primaryKeyConstraint;
    }

    public Set<ForeignKeyConstraint> getForeignKeyConstraints() {
        return foreignKeyConstraints;
    }

    public Set<UniqueConstraint> getUniqueConstraints() {
        return uniqueConstraints;
    }


    public Set<String> getNotNullColumns() {
        return notNullColumns;
    }

    public CreateTableStatement addPrimaryKeyColumn(String columnName, String columnType, boolean autoIncrement) {
        String pkName = "PK_" + tableName.toUpperCase();
        if (pkName.length() > 18) {
            pkName = pkName.substring(0, 17);
        }
        PrimaryKeyConstraint pkConstraint = new PrimaryKeyConstraint(pkName);
        pkConstraint.addColumns(columnName);

        if (autoIncrement) {
            autoIncrementColumns.add(columnName);
        }

        addColumn(columnName, columnType, pkConstraint, new NotNullConstraint(columnName));

        return this;
    }

    public CreateTableStatement addColumn(String columnName, String columnType) {
        return addColumn(columnName, columnType, null, new ColumnConstraint[0]);
    }

    public CreateTableStatement addColumn(String columnName, String columnType, String defaultValue) {
        return addColumn(columnName, columnType, defaultValue, new ColumnConstraint[0]);
    }

    public CreateTableStatement addColumn(String columnName, String columnType, ColumnConstraint... constraints) {
        return addColumn(columnName, columnType, null, constraints);
    }

    public CreateTableStatement addColumn(String columnName, String columnType, String defaultValue, ColumnConstraint... constraints) {
        this.columns.add(columnName);
        this.columnTypes.put(columnName, columnType);
        if (defaultValue != null) {
            defaultValues.put(columnName, defaultValue);
        }
        if (constraints != null) {
            for (ColumnConstraint constraint : constraints) {
                if (constraint instanceof PrimaryKeyConstraint) {
                    if (this.primaryKeyConstraint == null) {
                        this.primaryKeyConstraint = (PrimaryKeyConstraint) constraint;
                    } else {
                        for (String column : ((PrimaryKeyConstraint) constraint).getColumns()) {
                            this.primaryKeyConstraint.addColumns(column);
                        }
                    }
                } else if (constraint instanceof NotNullConstraint) {
                    notNullColumns.add(columnName);
                } else if (constraint instanceof ForeignKeyConstraint) {
                    ((ForeignKeyConstraint) constraint).setColumn(columnName);
                    foreignKeyConstraints.add(((ForeignKeyConstraint) constraint));
                } else if (constraint instanceof UniqueConstraint) {
                    ((UniqueConstraint) constraint).addColumns(columnName);
                    uniqueConstraints.add(((UniqueConstraint) constraint));
                } else {
                    throw new RuntimeException("Unknown constraint type: " + constraint.getClass().getName());
                }
            }
        }

        return this;
    }


    public String getSqlStatement(Database database) {
//        StringBuffer fkConstraints = new StringBuffer();

        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE ").append(SqlUtil.escapeTableName(tableName, database)).append(" ");
        buffer.append("(");
        Iterator<String> columnIterator = columns.iterator();
        while (columnIterator.hasNext()) {
            String column = columnIterator.next();
            boolean isAutoIncrement = autoIncrementColumns.contains(column);

            buffer.append(column);
            buffer.append(" ").append(database.getColumnType(columnTypes.get(column), isAutoIncrement));

            if (getDefaultValue(column) != null) {
                buffer.append(" DEFAULT ").append(getDefaultValue(column));
            }

            if (isAutoIncrement) {
                buffer.append(" ").append(database.getAutoIncrementClause()).append(" ");
            }

            if (notNullColumns.contains(column)) {
                buffer.append(" NOT NULL");
            } else {
                if (database instanceof SybaseDatabase) {
                    buffer.append(" NULL");
                }
            }

            if (columnIterator.hasNext()) {
                buffer.append(", ");
            }
        }

        if (primaryKeyConstraint != null && primaryKeyConstraint.getColumns().size() > 0) {
            buffer.append(", CONSTRAINT ").append(StringUtils.trimToEmpty(primaryKeyConstraint.getConstraintName())).append(" PRIMARY KEY (");
            buffer.append(StringUtils.join(primaryKeyConstraint.getColumns(), ", "));
            buffer.append(")");
            buffer.append(",");
        }

        for (ForeignKeyConstraint fkConstraint : foreignKeyConstraints) {
            buffer.append(" CONSTRAINT ")
                    .append(fkConstraint.getForeignKeyName())
                    .append(" FOREIGN KEY (")
                    .append(fkConstraint.getColumn())
                    .append(") REFERENCES ")
                    .append(fkConstraint.getReferences());

            if (fkConstraint.isDeleteCascade()) {
                buffer.append(" ON DELETE CASCADE");
            }


            if (fkConstraint.isInitiallyDeferred()) {
                buffer.append(" INITIALLY DEFERRED");
            }
            if (fkConstraint.isDeferrable()) {
                buffer.append(" DEFERRABLE");
            }
            buffer.append(",");
        }

        for (UniqueConstraint uniqueConstraint : uniqueConstraints) {
            buffer.append(" CONSTRAINT ");
            buffer.append(uniqueConstraint.getConstraintName());
            buffer.append(" UNIQUE (");
            buffer.append(StringUtils.join(uniqueConstraint.getColumns(), ", "));
            buffer.append("),");
        }

//        if (constraints != null && constraints.getCheck() != null) {
//            buffer.append(constraints.getCheck()).append(" ");
//        }
//    }

        String sql = buffer.toString().replaceFirst(",\\s*$", "") + ")";

//        if (StringUtils.trimToNull(tablespace) != null && database.supportsTablespaces()) {
//            if (database instanceof MSSQLDatabase) {
//                buffer.append(" ON ").append(tablespace);
//            } else if (database instanceof DB2Database) {
//                buffer.append(" IN ").append(tablespace);
//            } else {
//                buffer.append(" TABLESPACE ").append(tablespace);
//            }
//        }

        if (tablespace != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase) {
                sql += " ON " + tablespace;
            } else if (database instanceof DB2Database) {
                sql += " IN " + tablespace;
            } else {
                sql += " TABLESPACE " + tablespace;
            }
        }
        return sql;
    }

    public String getDefaultValue(String column) {
        return defaultValues.get(column);
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public void addColumnConstraint(NotNullConstraint notNullConstraint) {
        notNullColumns.add(notNullConstraint.getColumnName());
    }

    public void addColumnConstraint(ForeignKeyConstraint fkConstraint) {
        foreignKeyConstraints.add(fkConstraint);
    }

    public void addColumnConstraint(UniqueConstraint uniqueConstraint) {
        uniqueConstraints.add(uniqueConstraint);
    }
}
