package liquibase.statement.core;

import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.*;

import java.util.*;

public class CreateTableStatement extends AbstractSqlStatement {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String tablespace;
    private String remarks;
    private List<String> columns = new ArrayList<String>();
    private Set<AutoIncrementConstraint> autoIncrementConstraints = new HashSet<AutoIncrementConstraint>();
    private Map<String, LiquibaseDataType> columnTypes = new HashMap<String, LiquibaseDataType>();
    private Map<String, Object> defaultValues = new HashMap<String, Object>();
    private Map<String, String> columnRemarks = new HashMap<String, String>();

    private PrimaryKeyConstraint primaryKeyConstraint;
    private Set<String> notNullColumns = new HashSet<String>();
    private Set<ForeignKeyConstraint> foreignKeyConstraints = new HashSet<ForeignKeyConstraint>();
    private Set<UniqueConstraint> uniqueConstraints = new HashSet<UniqueConstraint>();


    public CreateTableStatement(String catalogName, String schemaName, String tableName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public CreateTableStatement(String catalogName, String schemaName, String tableName,String remarks) {
        this(catalogName,schemaName,tableName);
        this.remarks = remarks;
    }

    public String getCatalogName() {
        return catalogName;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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

    public CreateTableStatement addPrimaryKeyColumn(String columnName, LiquibaseDataType columnType, Object defaultValue, String keyName, String tablespace, ColumnConstraint... constraints) {
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

    public CreateTableStatement addColumn(String columnName, LiquibaseDataType columnType) {
        return addColumn(columnName, columnType, null, new ColumnConstraint[0]);
    }

    public CreateTableStatement addColumn(String columnName, LiquibaseDataType columnType, Object defaultValue) {
        if (defaultValue instanceof ColumnConstraint) {
            return addColumn(columnName,  columnType, null, new ColumnConstraint[]{(ColumnConstraint) defaultValue});
        }
        return addColumn(columnName, columnType, defaultValue, new ColumnConstraint[0]);
    }

    public CreateTableStatement addColumn(String columnName, LiquibaseDataType columnType, ColumnConstraint[] constraints) {
        return addColumn(columnName, columnType, null, constraints);
    }

    public CreateTableStatement addColumn(String columnName, LiquibaseDataType columnType, Object defaultValue, ColumnConstraint[] constraints) {
        return addColumn(columnName,columnType,defaultValue,null,constraints);

    }
    public CreateTableStatement addColumn(String columnName, LiquibaseDataType columnType, Object defaultValue, String remarks, ColumnConstraint... constraints) {
        this.getColumns().add(columnName);
        this.columnTypes.put(columnName, columnType);
        if (defaultValue != null) {
            defaultValues.put(columnName, defaultValue);
        }
        if(remarks != null) {
            this.columnRemarks.put(columnName, remarks);
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
                    autoIncrementConstraints.add((AutoIncrementConstraint) constraint);
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

    public String getColumnRemarks(String column) {
        return columnRemarks.get(column);
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
        getAutoIncrementConstraints().add(autoIncrementConstraint);
        return this;
    }

    public Set<AutoIncrementConstraint> getAutoIncrementConstraints() {
        return autoIncrementConstraints;
    }

    public Map<String, LiquibaseDataType> getColumnTypes() {
        return columnTypes;
    }

    public Map<String, Object> getDefaultValues() {
        return defaultValues;
    }

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
}
