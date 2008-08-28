package liquibase.database.sql;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SybaseDatabase;
import liquibase.util.StringUtils;
import liquibase.log.LogFactory;

import java.util.*;
import java.util.logging.Level;

public class CreateTableStatement implements SqlStatement {
    private String schemaName;
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


    public CreateTableStatement(String schemaName, String tableName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getSchemaName() {
        return schemaName;
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

    public CreateTableStatement setTablespace(String tablespace) {
        this.tablespace = tablespace;
        return this;
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

    public CreateTableStatement addPrimaryKeyColumn(String columnName, String columnType, String keyName, ColumnConstraint... constraints) {
//        String pkName = "PK_" + getTableName().toUpperCase();
////        if (pkName.length() > 18) {
////            pkName = pkName.substring(0, 17);
////        }
        PrimaryKeyConstraint pkConstraint = new PrimaryKeyConstraint(keyName);
        pkConstraint.addColumns(columnName);

        List<ColumnConstraint> allConstraints = new ArrayList<ColumnConstraint>();
        allConstraints.addAll(Arrays.asList(constraints));
        allConstraints.add(new NotNullConstraint(columnName));
        allConstraints.add(pkConstraint);


        addColumn(columnName, columnType, allConstraints.toArray(new ColumnConstraint[allConstraints.size()]));

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

    public boolean supportsDatabase(Database database) {
        return true;
    }

    public CreateTableStatement addColumn(String columnName, String columnType, String defaultValue, ColumnConstraint... constraints) {
        this.getColumns().add(columnName);
        this.columnTypes.put(columnName, columnType);
        if (defaultValue != null) {
            defaultValues.put(columnName, defaultValue);
        }
        if (constraints != null) {
            for (ColumnConstraint constraint : constraints) {
                if (constraint instanceof PrimaryKeyConstraint) {
                    if (this.getPrimaryKeyConstraint() == null) {
                        this.primaryKeyConstraint = (PrimaryKeyConstraint) constraint;
                    } else {
                        for (String column : ((PrimaryKeyConstraint) constraint).getColumns()) {
                            this.getPrimaryKeyConstraint().addColumns(column);
                        }
                    }
                } else if (constraint instanceof NotNullConstraint) {
                    ((NotNullConstraint) constraint).setColumnName(columnName);
                    getNotNullColumns().add(columnName);
                } else if (constraint instanceof ForeignKeyConstraint) {
                    ((ForeignKeyConstraint) constraint).setColumn(columnName);
                    getForeignKeyConstraints().add(((ForeignKeyConstraint) constraint));
                } else if (constraint instanceof UniqueConstraint) {
                    ((UniqueConstraint) constraint).addColumns(columnName);
                    getUniqueConstraints().add(((UniqueConstraint) constraint));
                } else if (constraint instanceof AutoIncrementConstraint) {
                    autoIncrementColumns.add(columnName);
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
        buffer.append("CREATE TABLE ").append(database.escapeTableName(getSchemaName(), getTableName())).append(" ");
        buffer.append("(");
        Iterator<String> columnIterator = getColumns().iterator();
        while (columnIterator.hasNext()) {
            String column = columnIterator.next();
            boolean isAutoIncrement = autoIncrementColumns.contains(column);

            buffer.append(database.escapeColumnName(getSchemaName(), getTableName(), column));
            buffer.append(" ").append(database.getColumnType(columnTypes.get(column), isAutoIncrement));

            if ((database instanceof SQLiteDatabase) && 
					(getPrimaryKeyConstraint()!=null) &&
					(getPrimaryKeyConstraint().getColumns().size()==1) &&
					(getPrimaryKeyConstraint().getColumns().contains(column)) &&
					isAutoIncrement) {
            	String pkName = StringUtils.trimToNull(getPrimaryKeyConstraint().getConstraintName());
	            if (pkName == null) {
	                pkName = database.generatePrimaryKeyName(getTableName());
	            }
	            buffer.append(" CONSTRAINT ");
	            buffer.append(pkName);
				buffer.append(" PRIMARY KEY AUTOINCREMENT");
			}
            
            if (getDefaultValue(column) != null) {
                if (database instanceof MSSQLDatabase) {
                    buffer.append(" CONSTRAINT ").append(((MSSQLDatabase) database).generateDefaultConstraintName(tableName, column));
                }
                buffer.append(" DEFAULT ");
                buffer.append(getDefaultValue(column));
            }

            if (isAutoIncrement &&
					(database.getAutoIncrementClause()!=null) &&
					(!database.getAutoIncrementClause().equals(""))) {
                if (database.supportsAutoIncrement()) {
                    buffer.append(" ").append(database.getAutoIncrementClause()).append(" ");
                } else {
                    LogFactory.getLogger().log(Level.WARNING, database.getProductName()+" does not support autoincrement columns as request for "+(database.escapeTableName(getSchemaName(), getTableName())));
                }
            }

            if (getNotNullColumns().contains(column)) {
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

        buffer.append(",");
        
        if (!( (database instanceof SQLiteDatabase) && 
				(getPrimaryKeyConstraint()!=null) &&
				(getPrimaryKeyConstraint().getColumns().size()==1) &&
				autoIncrementColumns.contains(getPrimaryKeyConstraint().getColumns().get(0)) )) {
        	// ...skip this code block for sqlite if a single column primary key
        	// with an autoincrement constraint exists.
        	// This constraint is added after the column type.

	        if (getPrimaryKeyConstraint() != null && getPrimaryKeyConstraint().getColumns().size() > 0) {
	            String pkName = StringUtils.trimToNull(getPrimaryKeyConstraint().getConstraintName());
	            if (pkName == null) {
	                pkName = database.generatePrimaryKeyName(getTableName());
	            }
	            buffer.append(" CONSTRAINT ");
	            buffer.append(pkName);
	            buffer.append(" PRIMARY KEY (");
	            buffer.append(database.escapeColumnNameList(StringUtils.join(getPrimaryKeyConstraint().getColumns(), ", ")));
	            buffer.append(")");
	            buffer.append(",");
	        }
        }

        for (ForeignKeyConstraint fkConstraint : getForeignKeyConstraints()) {
            buffer.append(" CONSTRAINT ")
                    .append(fkConstraint.getForeignKeyName())
                    .append(" FOREIGN KEY (")
                    .append(database.escapeColumnName(getSchemaName(), getTableName(), fkConstraint.getColumn()))
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

        for (UniqueConstraint uniqueConstraint : getUniqueConstraints()) {
            if (uniqueConstraint.getConstraintName() != null) {
                buffer.append(" CONSTRAINT ");
                buffer.append(uniqueConstraint.getConstraintName());
            }
            buffer.append(" UNIQUE (");
            buffer.append(database.escapeColumnNameList(StringUtils.join(uniqueConstraint.getColumns(), ", ")));
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

        if (getTablespace() != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase) {
                sql += " ON " + getTablespace();
            } else if (database instanceof DB2Database) {
                sql += " IN " + getTablespace();
            } else {
                sql += " TABLESPACE " + getTablespace();
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

    public CreateTableStatement addColumnConstraint(NotNullConstraint notNullConstraint) {
        getNotNullColumns().add(notNullConstraint.getColumnName());
        return this;
    }

    public CreateTableStatement addColumnConstraint(ForeignKeyConstraint fkConstraint) {
        getForeignKeyConstraints().add(fkConstraint);
        return this;
    }

    public CreateTableStatement addColumnConstraint(UniqueConstraint uniqueConstraint) {
        getUniqueConstraints().add(uniqueConstraint);
        return this;
    }

    public CreateTableStatement addColumnConstraint(AutoIncrementConstraint autoIncrementConstraint) {
        autoIncrementColumns.add(autoIncrementConstraint.getColumnName());
        return this;
    }
}
