package liquibase.statement.core;

import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.*;

import java.util.*;

public class CreateTableStatement extends AbstractSqlStatement implements CompoundStatement {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String tablespace;
    private String remarks;
    private List<String> columns = new ArrayList<>();
    private Set<AutoIncrementConstraint> autoIncrementConstraints = new HashSet<>();
    private Map<String, LiquibaseDataType> columnTypes = new HashMap<>();
    private Map<String, Object> defaultValues = new HashMap<>();
    private Map<String, String> defaultValueConstraintNames = new HashMap<>();
    private Map<String, String> columnRemarks = new HashMap<>();

    private PrimaryKeyConstraint primaryKeyConstraint;
    private Set<ForeignKeyConstraint> foreignKeyConstraints = new HashSet<>();

    /* NOT NULL constraints in RDBMSs are curious beasts. In some RDBMS, they do not exist as constraints at all, i.e.
       they are merely a property of the column. In others, like Oracle DB, they can exist in both forms, and to be
       able to give the NN constraint a name in CREATE TABLE, we need to save both the NN property as well as the NN constraint. To make things even more complicated, you cannot just add a NN constraint after the list
       of columns, like you could do with UNIQUE, CHECK or FOREIGN KEY constraints. They must be defined
       in line with the column (this implies that a NN constraint can always affects exactly one column). */
    private HashMap<String, NotNullConstraint> notNullColumns = new HashMap<>();

    private Set<UniqueConstraint> uniqueConstraints = new HashSet<>();

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

    public Map<String, NotNullConstraint> getNotNullColumns() {
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

        List<ColumnConstraint> allConstraints = new ArrayList<>();
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
        return addColumn(columnName, columnType, null, defaultValue, remarks, constraints);
    }

    public CreateTableStatement addColumn(String columnName, LiquibaseDataType columnType, String defaultValueConstraintName, Object defaultValue, String remarks, ColumnConstraint... constraints) {
        this.getColumns().add(columnName);
        this.columnTypes.put(columnName, columnType);
        if (defaultValue != null) {
            defaultValues.put(columnName, defaultValue);
        }
        if (defaultValueConstraintName != null) {
            defaultValueConstraintNames.put(columnName, defaultValueConstraintName);
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
                    getNotNullColumns().put(columnName, (NotNullConstraint) constraint);
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

    public String getDefaultValueConstraintName(String column) {
        return defaultValueConstraintNames.get(column);
    }

    public String getColumnRemarks(String column) {
        return columnRemarks.get(column);
    }

    public CreateTableStatement addColumnConstraint(NotNullConstraint notNullConstraint) {
        getNotNullColumns().put(notNullConstraint.getColumnName(), notNullConstraint);
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

    public Map<String, String> getDefaultValueConstraintNames() {
        return defaultValueConstraintNames;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}
