package liquibase.statement.core;

import liquibase.database.structure.type.DataType;
import liquibase.statement.*;

import java.util.*;

public class CreateTableStatement extends AbstractSqlStatement {
    private String schemaName;
    private String tableName;
    private String tablespace;
    private List<String> columns = new ArrayList<String>();
    private Set<String> autoIncrementColumns = new HashSet<String>();
    private Map<String, DataType> columnTypes = new HashMap<String, DataType>();
    private Map<String, Object> defaultValues = new HashMap<String, Object>();

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

    public CreateTableStatement addPrimaryKeyColumn(String columnName, DataType columnType, Object defaultValue, String keyName, String tablespace, ColumnConstraint... constraints) {
//        String pkName = "PK_" + getTableName().toUpperCase();
////        if (pkName.length() > 18) {
////            pkName = pkName.substring(0, 17);
////        }
        PrimaryKeyConstraint pkConstraint = new PrimaryKeyConstraint(keyName);
        pkConstraint.addColumns(columnName);
	    pkConstraint.setTablespace(tablespace);

        List<ColumnConstraint> allConstraints = new ArrayList<ColumnConstraint>();
        allConstraints.addAll(Arrays.asList(constraints));
        allConstraints.add(new NotNullConstraint(columnName));
        allConstraints.add(pkConstraint);


        addColumn(columnName, columnType, defaultValue, allConstraints.toArray(new ColumnConstraint[allConstraints.size()]));

        return this;
    }

    public CreateTableStatement addColumn(String columnName, DataType columnType) {
        return addColumn(columnName, columnType, null, new ColumnConstraint[0]);
    }

    public CreateTableStatement addColumn(String columnName, DataType columnType, Object defaultValue) {
        if (defaultValue instanceof ColumnConstraint) {
            return addColumn(columnName,  columnType, null, (ColumnConstraint) defaultValue);
        }
        return addColumn(columnName, columnType, defaultValue, new ColumnConstraint[0]);
    }

    public CreateTableStatement addColumn(String columnName, DataType columnType, ColumnConstraint... constraints) {
        return addColumn(columnName, columnType, null, constraints);
    }

    public CreateTableStatement addColumn(String columnName, DataType columnType, Object defaultValue, ColumnConstraint... constraints) {
        this.getColumns().add(columnName);
        this.columnTypes.put(columnName, columnType);
        if (defaultValue != null) {
            defaultValues.put(columnName, defaultValue);
        }
        if (constraints != null) {
            for (ColumnConstraint constraint : constraints) {
                if (constraint == null) {
                    continue;
                }

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

    public Object getDefaultValue(String column) {
        return defaultValues.get(column);
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

    public Set<String> getAutoIncrementColumns() {
        return autoIncrementColumns;
    }

    public Map<String, DataType> getColumnTypes() {
        return columnTypes;
    }

    public Map<String, Object> getDefaultValues() {
        return defaultValues;
    }

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
}
