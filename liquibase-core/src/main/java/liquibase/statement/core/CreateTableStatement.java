package liquibase.statement.core;

import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.*;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.util.*;

/**
 * Creates a new table.
 * Many get methods return unmodifiable collections and rely on add/remove methods to protect the internal consistency of the table description.
 */
public class CreateTableStatement extends AbstractTableStatement {

    private static final String TABLESPACE = "tablespace";
    private static final String REMARKS = "remarks";
    private static final String COLUMN_NAMES = "columnNames";
    private static final String AUTO_INCREMENT_CONSTRAINTS = "autoIncrementConstraints";
    private static final String COLUMN_TYPES = "columnTypes";
    private static final String DEFAULT_VALUES = "defaultValues";
    private static final String COLUMN_REMARKS = "columnRemarks";

    private static final String CONSTRAINTS = "constraints";

    public CreateTableStatement() {
        init();
    }

    public CreateTableStatement(String catalogName, String schemaName, String tableName) {
        super(catalogName, schemaName, tableName);
        init();
    }

    protected void init() {
        setAttribute(COLUMN_NAMES, new ArrayList<String>());
        setAttribute(AUTO_INCREMENT_CONSTRAINTS, new HashSet<AutoIncrementConstraint>());
        setAttribute(COLUMN_TYPES, new HashMap<String, LiquibaseDataType>());
        setAttribute(DEFAULT_VALUES, new HashMap<String, Object>());
        setAttribute(COLUMN_REMARKS, new HashMap<String, String>());
        setAttribute(COLUMN_TYPES, new HashMap<String, LiquibaseDataType>());
        setAttribute(CONSTRAINTS, new HashSet<Constraint>());
    }

    /**
     * Returns unmodifiable list of columns. To add a column use {@link #addColumn(String, liquibase.datatype.LiquibaseDataType)} or a variation.
     * To remove a column use {@link #removeColumn(String)}
     */
    public List<String> getColumnNames() {
        return Collections.unmodifiableList(getAttribute(COLUMN_NAMES, List.class));
    }

    public String getTablespace() {
        return getAttribute(TABLESPACE, String.class);
    }

    public CreateTableStatement setTablespace(String tablespace) {
        return (CreateTableStatement) setAttribute(TABLESPACE, tablespace);
    }

    /**
     * Table-wide remarks. To get column remarks, use {@link #getColumnRemarks(String)}
     */
    public String getRemarks() {
        return getAttribute(REMARKS, String.class);
    }

    public CreateTableStatement setRemarks(String remarks) {
        return (CreateTableStatement) setAttribute(REMARKS, remarks);
    }

    /**
     * Removes modifiable set of constraints defined on this table.
     */
    public Set<? extends Constraint> getConstraints() {
        return getAttribute(CONSTRAINTS, Set.class);
    }

    /**
     * Returns an unmodifiable set of constraints of the given type. If none are defined, returns an empty collection.
     */
    public <T extends Constraint> Set<T> getConstraints(Class<T> type) {
        Set returnSet = new HashSet();
        for (Constraint constraint : getConstraints()) {
            if (type.isAssignableFrom(constraint.getClass())) {
                returnSet.add(constraint);
            }
        }
        return Collections.unmodifiableSet(returnSet);
    }


    /**
     * Adds a new constraint for the table.
     * No validation is done adding the constraint, so it is possible to add constraints before columns are added.
     * Any validation needs to be done by the {@link liquibase.statementlogic.StatementLogic} class.
     */
    public CreateTableStatement addConstraint(Constraint constraint) {
        getAttribute(CONSTRAINTS, Set.class).add(constraint);
        return this;
    }


    /**
     * Convenience method to return the defined primary key key.
     * If multiple primary key constraints are added, one is returned randomly.
     */
    public PrimaryKeyConstraint getPrimaryKeyConstraint() {
        Set<PrimaryKeyConstraint> constraints = getConstraints(PrimaryKeyConstraint.class);
        if (constraints.isEmpty()) {
            return null;
        }
        return constraints.iterator().next();
    }

    /**
     * Convenience method on {@link #getConstraints(Class)} to return the foreign keys. Returns unmodifiable collection.
     */
    public Set<ForeignKeyConstraint> getForeignKeyConstraints() {
        return getConstraints(ForeignKeyConstraint.class);
    }

    /**
     * Convenience method on {@link #getConstraints(Class)} to return the unique constraints. Returns unmodifiable collection.
     */
    public Set<UniqueConstraint> getUniqueConstraints() {
        return getConstraints(UniqueConstraint.class);
    }

    /**
     * Convenience method on {@link #getConstraints(Class)} to return the not null constraints. Returns unmodifiable collection.
     */
    public Set<NotNullConstraint> getNotNullConstraints() {
        return getConstraints(NotNullConstraint.class);
    }

    /**
     * Convenience method to return the not null constraint for a given column. Returns null if no constraint is defined.
     */
    public NotNullConstraint getNotNullConstraint(String columnName) {
        for (NotNullConstraint constraint : getNotNullConstraints()) {
            if (constraint.getColumnName().equals(columnName)) {
                return constraint;
            }
        }
        return null;
    }

    /**
     * Convenience method on {@link #getConstraints(Class)} to return the auto increment constraints. Returns unmodifiable collection.
     */
    public Set<AutoIncrementConstraint> getAutoIncrementConstraints() {
        return getAttribute(AUTO_INCREMENT_CONSTRAINTS, Set.class);
    }


    /**
     * Adds a new column with the given information, but also adds the column to the table's primary key constraint and creates a not null constraint for the column.
     * If no primary key constraint is yet defined, it creates it.
     */
    public CreateTableStatement addPrimaryKeyColumn(String columnName, LiquibaseDataType columnType, Object defaultValue, String keyName, String tablespace, Constraint... constraints) {
        PrimaryKeyConstraint pkConstraint = new PrimaryKeyConstraint(keyName);
        pkConstraint.addColumns(columnName);
	    pkConstraint.setTablespace(tablespace);

        List<Constraint> allConstraints = new ArrayList<Constraint>();
        allConstraints.addAll(Arrays.asList(constraints));
        allConstraints.add(new NotNullConstraint(columnName));
        allConstraints.add(pkConstraint);


        addColumn(columnName, columnType, defaultValue, allConstraints.toArray(new Constraint[allConstraints.size()]));

        return this;
    }

    public CreateTableStatement addColumn(String columnName, LiquibaseDataType columnType) {
        return addColumn(columnName, columnType, null, new Constraint[0]);
    }

    public CreateTableStatement addColumn(String columnName, LiquibaseDataType columnType, Object defaultValue) {
        if (defaultValue instanceof Constraint) {
            return addColumn(columnName,  columnType, null, new Constraint[]{(Constraint) defaultValue});
        }
        return addColumn(columnName, columnType, defaultValue, new Constraint[0]);
    }

    public CreateTableStatement addColumn(String columnName, LiquibaseDataType columnType, Constraint[] constraints) {
        return addColumn(columnName, columnType, null, constraints);
    }

    public CreateTableStatement addColumn(String columnName, LiquibaseDataType columnType, Object defaultValue, Constraint[] constraints) {
        return addColumn(columnName,columnType,defaultValue,null,constraints);

    }

    /**
     * Adds a column to the table definition.
     * Will automatically call {@link liquibase.statement.ColumnConstraint#setColumnName(String)} for any ColumnConstraints with a null getColumnName().
     * Any non-ColumnConstraints are not modified before being stored.
     * If a primary key constraint is included in this call and a primary key already exists, this column will be merged with the existing constraint.
     */
    public CreateTableStatement addColumn(String columnName, LiquibaseDataType columnType, Object defaultValue, String remarks, Constraint... constraints) {
        getAttribute(COLUMN_NAMES, List.class).add(columnName);
        getAttribute(COLUMN_TYPES, Map.class).put(columnName, columnType);
        if (defaultValue != null) {
            getAttribute(DEFAULT_VALUES, Map.class).put(columnName, defaultValue);
        }
        if(remarks != null) {
            getAttribute(COLUMN_REMARKS, Map.class).put(columnName, remarks);
        }
        if (constraints != null) {
            for (Constraint constraint : constraints) {
                if (constraint == null) {
                    continue;
                }

                if (constraint instanceof PrimaryKeyConstraint) {
                    PrimaryKeyConstraint existingPK = this.getPrimaryKeyConstraint();
                    if (existingPK != null) {
                        existingPK.addColumns(columnName);
                    } else {
                        if (!((PrimaryKeyConstraint) constraint).getColumns().contains(columnName)) {
                            ((PrimaryKeyConstraint) constraint).addColumns(columnName);
                        }
                        addConstraint(constraint);
                    }
                } else {
                    if (constraint instanceof ColumnConstraint && ((ColumnConstraint) constraint).getColumnName() == null) {
                        ((ColumnConstraint) constraint).setColumnName(columnName);
                    }
                    addConstraint(constraint);
                }
            }
        }

        return this;
    }

    public CreateTableStatement removeColumn (String columnName) {
        getAttribute(COLUMN_NAMES, List.class).remove(columnName);
        getAttribute(COLUMN_TYPES, Map.class).remove(columnName);
        getAttribute(DEFAULT_VALUES, Map.class).remove(columnName);

        getAttribute(COLUMN_REMARKS, Map.class).remove(columnName);

        Iterator<? extends Constraint> constraints = getConstraints().iterator();
        while (constraints.hasNext()) {
            Constraint constraint = constraints.next();
            if (constraint instanceof ColumnConstraint && ((ColumnConstraint) constraint).getColumnName().equals(columnName)) {
                constraints.remove();
            }
        }

        return this;
    }

    /**
     * Return the default value for the given column. Null if not default value is specified
     */
    public Object getDefaultValue(String column) {
        return getAttribute(DEFAULT_VALUES, Map.class).get(column);
    }

    public CreateTableStatement setDefaultValue(String column, Object value) {
        getAttribute(DEFAULT_VALUES, Map.class).put(column, value);
        return this;
    }

    public String getColumnRemarks(String column) {
        return (String) getAttribute(COLUMN_REMARKS, Map.class).get(column);
    }

    public CreateTableStatement setColumnRemarks(String column, String remarks) {
        getAttribute(COLUMN_REMARKS, Map.class).put(column, remarks);

        return this;
    }

    public LiquibaseDataType getColumnType(String columnName) {
        return (LiquibaseDataType) getAttribute(COLUMN_TYPES, Map.class).get(columnName);
    }

    public CreateTableStatement setColumnType(String column, LiquibaseDataType dataType) {
        getAttribute(COLUMN_TYPES, Map.class).put(column, dataType);

        return this;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Table().setName(getTableName()).setSchema(new Schema(getCatalogName(), getSchemaName()))
        };
    }
}
