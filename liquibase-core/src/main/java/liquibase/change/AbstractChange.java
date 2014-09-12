package liquibase.change;

import java.lang.reflect.Type;
import java.util.*;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.DatabaseObject;
import liquibase.exception.*;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Standard superclass to simplify {@link Change } implementations. You can implement Change directly, this class is purely for convenience but recommended.
 * <p></p>
 * By default, this base class relies on annotations such as {@link DatabaseChange} and {@link DatabaseChangeProperty}
 * and delegating logic to the {@link liquibase.sqlgenerator.SqlGenerator} objects created to do the actual change work.
 * Place the @DatabaseChangeProperty annotations on the read "get" methods to control property metadata.
 */
public abstract class AbstractChange implements Change {

    private ResourceAccessor resourceAccessor;

    private ChangeSet changeSet;

    public AbstractChange() {
    }

    /**
     * Default implementation is a no-op
     */
    @Override
    public void finishInitialization() throws SetupException {

    }

    /**
     * Generate the ChangeMetaData for this class. Default implementation reads from the @{@link DatabaseChange } annotation
     * and calls out to {@link #createChangeParameterMetadata(String)} for each property.
     *
     * @throws UnexpectedLiquibaseException if no @DatabaseChange annotation on this Change class
     */
    @Override
    public ChangeMetaData createChangeMetaData() {
        try {
            DatabaseChange databaseChange = this.getClass().getAnnotation(DatabaseChange.class);

            if (databaseChange == null) {
                throw new UnexpectedLiquibaseException("No @DatabaseChange annotation for " + getClass().getName());
            }

            Set<ChangeParameterMetaData> params = new HashSet<ChangeParameterMetaData>();
            for (PropertyDescriptor property : Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors()) {
                if (isInvalidProperty(property)) {
                    continue;
                }
                Method readMethod = property.getReadMethod();
                Method writeMethod = property.getWriteMethod();
                if (readMethod == null) {
                    try {
                        readMethod = this.getClass().getMethod("is" + StringUtils.upperCaseFirst(property.getName()));
                    } catch (Exception ignore) {
                        //it was worth a try
                    }
                }
                if (readMethod != null && writeMethod != null) {
                    DatabaseChangeProperty annotation = readMethod.getAnnotation(DatabaseChangeProperty.class);
                    if (annotation == null || annotation.isChangeProperty()) {
                        params.add(createChangeParameterMetadata(property.getDisplayName()));
                    }
                }

            }

            Map<String, String> notes = new HashMap<String, String>();
            for (DatabaseChangeNote note : databaseChange.databaseNotes()) {
                notes.put(note.database(), note.notes());
            }

            return new ChangeMetaData(databaseChange.name(), databaseChange.description(), databaseChange.priority(), databaseChange.appliesTo(), notes, params);
        } catch (Throwable e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    protected boolean isInvalidProperty(PropertyDescriptor property) {
        return property.getDisplayName().equals("metaClass");
    }

    /**
     * Called by {@link #createChangeMetaData()} to create metadata for a given parameter. It finds the method that corresponds to the parameter
     * and calls the corresponding create*MetaData methods such as {@link #createRequiredDatabasesMetaData(String, DatabaseChangeProperty)} to determine the
     * correct values for the ChangeParameterMetaData fields.
     *
     * @throws UnexpectedLiquibaseException if the passed parameter does not exist
     */
    protected ChangeParameterMetaData createChangeParameterMetadata(String parameterName) {

        try {
            String displayName = parameterName.replaceAll("([A-Z])", " $1");
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);

            PropertyDescriptor property = null;
            for (PropertyDescriptor prop : Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors()) {
                if (prop.getDisplayName().equals(parameterName)) {
                    property = prop;
                    break;
                }
            }
            if (property == null) {
                throw new UnexpectedLiquibaseException("Could not find property " + parameterName);
            }

            Method readMethod = property.getReadMethod();
            if (readMethod == null) {
                readMethod = getClass().getMethod("is" + StringUtils.upperCaseFirst(property.getName()));
            }
            Type type = readMethod.getGenericReturnType();

            DatabaseChangeProperty changePropertyAnnotation = readMethod.getAnnotation(DatabaseChangeProperty.class);

            String mustEqualExisting = createMustEqualExistingMetaData(parameterName, changePropertyAnnotation);
            String description = createDescriptionMetaData(parameterName, changePropertyAnnotation);
            Map<String, Object> examples = createExampleValueMetaData(parameterName, changePropertyAnnotation);
            String since = createSinceMetaData(parameterName, changePropertyAnnotation);
            SerializationType serializationType = createSerializationTypeMetaData(parameterName, changePropertyAnnotation);
            String[] requiredForDatabase = createRequiredDatabasesMetaData(parameterName, changePropertyAnnotation);
            String[] supportsDatabase = createSupportedDatabasesMetaData(parameterName, changePropertyAnnotation);


            return new ChangeParameterMetaData(this, parameterName, displayName, description, examples, since, type, requiredForDatabase, supportsDatabase, mustEqualExisting, serializationType);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Create the {@link ChangeParameterMetaData} "since" value. Uses the value on the DatabaseChangeProperty annotation or returns null as a default.
     */
    @SuppressWarnings("UnusedParameters")
    protected String createSinceMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return null;
        }
        return StringUtils.trimToNull(changePropertyAnnotation.since());
    }

    /**
     * Create the {@link ChangeParameterMetaData} "description" value. Uses the value on the DatabaseChangeProperty annotation or returns null as a default.
     */
    @SuppressWarnings("UnusedParameters")
    protected String createDescriptionMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return null;
        }
        return StringUtils.trimToNull(changePropertyAnnotation.description());
    }

    /**
     * Create the {@link ChangeParameterMetaData} "serializationType" value. Uses the value on the DatabaseChangeProperty annotation or returns {@link SerializationType}.NAMED_FIELD as a default.
     */
    @SuppressWarnings("UnusedParameters")
    protected SerializationType createSerializationTypeMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return SerializationType.NAMED_FIELD;
        }
        return changePropertyAnnotation.serializationType();
    }

    /**
     * Create the {@link ChangeParameterMetaData} "mustEqual" value. Uses the value on the DatabaseChangeProperty annotation or returns null as a default.
     */
    @SuppressWarnings("UnusedParameters")
    protected String createMustEqualExistingMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return null;
        }

        return changePropertyAnnotation.mustEqualExisting();
    }

    /**
     * Create the {@link ChangeParameterMetaData} "example" value. Uses the value on the DatabaseChangeProperty annotation or returns null as a default.
     * Returns map with key=database short name, value=example. Use short-name "all" as the fallback.
     */
    @SuppressWarnings("UnusedParameters")
    protected Map<String, Object> createExampleValueMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return null;
        }

        Map<String, Object> examples = new HashMap<String, Object>();
        examples.put("all", StringUtils.trimToNull(changePropertyAnnotation.exampleValue()));

        return examples;
    }

    /**
     * Create the {@link ChangeParameterMetaData} "requiredDatabases" value.
     * Uses the value on the DatabaseChangeProperty annotation or returns an array containing the string "COMPUTE" as a default.
     * "COMPUTE" will cause ChangeParameterMetaData to attempt to determine the required databases based on the generated Statements
     */
    @SuppressWarnings("UnusedParameters")
    protected String[] createRequiredDatabasesMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return new String[]{ChangeParameterMetaData.COMPUTE};
        } else {
            return changePropertyAnnotation.requiredForDatabase();
        }
    }

    /**
     * Create the {@link ChangeParameterMetaData} "supportedDatabase" value.
     * Uses the value on the DatabaseChangeProperty annotation or returns an array containing the string "COMPUTE" as a default.
     * "COMPUTE" will cause ChangeParameterMetaData to attempt to determine the required databases based on the generated Statements
     */
    @SuppressWarnings("UnusedParameters")
    protected String[] createSupportedDatabasesMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return new String[]{ChangeParameterMetaData.COMPUTE};
        } else {
            return changePropertyAnnotation.supportsDatabase();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DatabaseChangeProperty(isChangeProperty = false)
    public ChangeSet getChangeSet() {
        return changeSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChangeSet(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#generateStatementsIsVolatile(Database) } method on the {@link SqlStatement} objects returned by {@link #generateStatements }.
     * If zero or null SqlStatements are returned by generateStatements then this method returns false.
     */
    @Override
    public boolean generateStatementsVolatile(Database database) {
        SqlStatement[] statements = generateStatements(database);
        if (statements == null) {
            return false;
        }
        for (SqlStatement statement : statements) {
            if (SqlGeneratorFactory.getInstance().generateStatementsVolatile(statement, database)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#generateRollbackStatementsIsVolatile(Database) } method on the {@link SqlStatement} objects returned by {@link #generateStatements }
     * If no or null SqlStatements are returned by generateRollbackStatements then this method returns false.
     */
    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        if (generateStatementsVolatile(database)) {
            return true;
        }
        SqlStatement[] statements = generateStatements(database);
        if (statements == null) {
            return false;
        }
        for (SqlStatement statement : statements) {
            if (SqlGeneratorFactory.getInstance().generateRollbackStatementsVolatile(statement, database)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#supports(liquibase.statement.SqlStatement, liquibase.database.Database)} method on the {@link SqlStatement} objects returned by {@link #generateStatements }.
     * If no or null SqlStatements are returned by generateStatements then this method returns true.
     * If {@link #generateStatementsVolatile(liquibase.database.Database)} returns true, we cannot call generateStatements and so assume true.
     */
    @Override
    public boolean supports(Database database) {
        if (generateStatementsVolatile(database)) {
            return true;
        }
        SqlStatement[] statements = generateStatements(database);
        if (statements == null) {
            return true;
        }
        for (SqlStatement statement : statements) {
            if (!SqlGeneratorFactory.getInstance().supports(statement, database)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#warn(liquibase.statement.SqlStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)} method on the {@link SqlStatement} objects returned by {@link #generateStatements }.
     * If a generated statement is not supported for the given database, no warning will be added since that is a validation error.
     * If no or null SqlStatements are returned by generateStatements then this method returns no warnings.
     */
    @Override
    public Warnings warn(Database database) {
        Warnings warnings = new Warnings();
        if (generateStatementsVolatile(database)) {
            return warnings;
        }

        SqlStatement[] statements = generateStatements(database);
        if (statements == null) {
            return warnings;
        }
        for (SqlStatement statement : statements) {
            if (SqlGeneratorFactory.getInstance().supports(statement, database)) {
                warnings.addAll(SqlGeneratorFactory.getInstance().warn(statement, database));
            } else if (statement.skipOnUnsupported()) {
                warnings.addWarning(statement.getClass().getName() + " is not supported on " + database.getShortName() + ", but " + ChangeFactory.getInstance().getChangeMetaData(this).getName() + " will still execute");
            }
        }

        return warnings;
    }

    /**
     * Implementation checks the ChangeParameterMetaData for declared required fields
     * and also delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#validate(liquibase.statement.SqlStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)}  method on the {@link SqlStatement} objects returned by {@link #generateStatements }.
     * If no or null SqlStatements are returned by generateStatements then this method returns no errors.
     * If there are no parameters than this method returns no errors
     */
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors changeValidationErrors = new ValidationErrors();

        for (ChangeParameterMetaData param : ChangeFactory.getInstance().getChangeMetaData(this).getParameters().values()) {
            if (param.isRequiredFor(database) && param.getCurrentValue(this) == null) {
                changeValidationErrors.addError(param.getParameterName() + " is required for " + ChangeFactory.getInstance().getChangeMetaData(this).getName() + " on " + database.getShortName());
            }
        }
        if (changeValidationErrors.hasErrors()) {
            return changeValidationErrors;
        }

        String unsupportedWarning = ChangeFactory.getInstance().getChangeMetaData(this).getName() + " is not supported on " + database.getShortName();
        if (!this.supports(database)) {
            changeValidationErrors.addError(unsupportedWarning);
        } else if (!generateStatementsVolatile(database)) {
            boolean sawUnsupportedError = false;
            SqlStatement[] statements;
            statements = generateStatements(database);
            if (statements != null) {
                for (SqlStatement statement : statements) {
                    boolean supported = SqlGeneratorFactory.getInstance().supports(statement, database);
                    if (!supported && !sawUnsupportedError) {
                        if (!statement.skipOnUnsupported()) {
                            changeValidationErrors.addError(unsupportedWarning);
                            sawUnsupportedError = true;
                        }
                    } else {
                        changeValidationErrors.addAll(SqlGeneratorFactory.getInstance().validate(statement, database));
                    }
                }
            }
        }

        return changeValidationErrors;
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        return new ChangeStatus().unknown("Not implemented");
    }

    /**
     * Implementation relies on value returned from {@link #createInverses()}.
     */
    @Override
    public SqlStatement[] generateRollbackStatements(Database database) throws RollbackImpossibleException {
        return generateRollbackStatementsFromInverse(database);
    }

    /**
     * Implementation returns true if {@link #createInverses()} returns a non-null value.
     */
    @Override
    public boolean supportsRollback(Database database) {
        return createInverses() != null;
    }

    /**
     * Implementation generates checksum by serializing the change with {@link StringChangeLogSerializer}
     */
    @Override
    public CheckSum generateCheckSum() {
        return CheckSum.compute(new StringChangeLogSerializer().serialize(this, false));
    }

    /*
     * Generates rollback statements from the inverse changes returned by createInverses().
     * Throws RollbackImpossibleException if the changes created by createInverses() is not supported for the passed database.
     *
     */
    private SqlStatement[] generateRollbackStatementsFromInverse(Database database) throws RollbackImpossibleException {
        Change[] inverses = createInverses();
        if (inverses == null) {
            throw new RollbackImpossibleException("No inverse to " + getClass().getName() + " created");
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        try {
            for (Change inverse : inverses) {
                if (!inverse.supports(database)) {
                    throw new RollbackImpossibleException(ChangeFactory.getInstance().getChangeMetaData(inverse).getName() + " is not supported on " + database.getShortName());
                }
                statements.addAll(Arrays.asList(inverse.generateStatements(database)));
            }
        } catch (LiquibaseException e) {
            throw new RollbackImpossibleException(e);
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    /**
     * Create inverse changes that can roll back this change. This method is intended
     * to be overriden by Change implementations that have a logical inverse operation. Default implementation returns null.
     * <p/>
     * If {@link #generateRollbackStatements(liquibase.database.Database)} is overridden, this method may not be called.
     *
     * @return Return null if there is no corresponding inverse and therefore automatic rollback is not possible. Return an empty array to have a no-op rollback.
     * @also #generateRollbackStatements #supportsRollback
     */
    protected Change[] createInverses() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    /**
     * @{inheritDoc}
     */
    @DatabaseChangeProperty(isChangeProperty = false)
    public ResourceAccessor getResourceAccessor() {
        return resourceAccessor;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGeneratorFactory#getAffectedDatabaseObjects(liquibase.statement.SqlStatement, liquibase.database.Database)}  method on the {@link SqlStatement} objects returned by {@link #generateStatements }
     * Returns empty set if change is not supported for the passed database
     */
    @Override
    public Set<DatabaseObject> getAffectedDatabaseObjects(Database database) {
        if (this.generateStatementsVolatile(database)) {
            return new HashSet<DatabaseObject>();
        }
        Set<DatabaseObject> affectedObjects = new HashSet<DatabaseObject>();
        SqlStatement[] statements = generateStatements(database);

        if (statements != null) {
            for (SqlStatement statement : statements) {
                affectedObjects.addAll(SqlGeneratorFactory.getInstance().getAffectedDatabaseObjects(statement, database));
            }
        }

        return affectedObjects;
    }

    /**
     * Returns the fields on this change that are serializable.
     */
    @Override
    public Set<String> getSerializableFields() {
        return ChangeFactory.getInstance().getChangeMetaData(this).getParameters().keySet();
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        return ChangeFactory.getInstance().getChangeMetaData(this).getParameters().get(field).getCurrentValue(this);
    }

    @Override
    public String getSerializedObjectName() {
        return ChangeFactory.getInstance().getChangeMetaData(this).getName();
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        return ChangeFactory.getInstance().getChangeMetaData(this).getParameters().get(field).getSerializationType();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
    }

    @Override
    public String getSerializableFieldNamespace(String field) {
        return getSerializedObjectNamespace();
    }

    @Override
    public String toString() {
        return ChangeFactory.getInstance().getChangeMetaData(this).getName();
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        ChangeMetaData metaData = ChangeFactory.getInstance().getChangeMetaData(this);
        this.setResourceAccessor(resourceAccessor);
        try {
            for (ChangeParameterMetaData param : metaData.getParameters().values()) {
                if (Collection.class.isAssignableFrom(param.getDataTypeClass())) {
                    Class collectionType = (Class) param.getDataTypeClassParameters()[0];
                    if (param.getDataTypeClassParameters().length == 1) {
                        if (ColumnConfig.class.isAssignableFrom(collectionType)) {
                            List<ParsedNode> columnNodes = new ArrayList<ParsedNode>(parsedNode.getChildren(null, param.getParameterName()));
                            columnNodes.addAll(parsedNode.getChildren(null, "column"));

                            Object nodeValue = parsedNode.getValue();
                            if (nodeValue instanceof ParsedNode) {
                                columnNodes.add((ParsedNode) nodeValue);
                            } else if (nodeValue instanceof Collection) {
                                for (Object nodeValueChild : ((Collection) nodeValue)) {
                                    if (nodeValueChild instanceof ParsedNode) {
                                        columnNodes.add((ParsedNode) nodeValueChild);
                                    }
                                }
                            }


                            for (ParsedNode child : columnNodes) {
                                if (child.getName().equals("column") || child.getName().equals("columns")) {
                                    List<ParsedNode> columnChildren = child.getChildren(null, "column");
                                    if (columnChildren != null && columnChildren.size() > 0) {
                                        for (ParsedNode columnChild : columnChildren) {
                                            ColumnConfig columnConfig = (ColumnConfig) collectionType.newInstance();
                                            columnConfig.load(columnChild, resourceAccessor);
                                            ((ChangeWithColumns) this).addColumn(columnConfig);
                                        }
                                    } else {
                                        ColumnConfig columnConfig = (ColumnConfig) collectionType.newInstance();
                                        columnConfig.load(child, resourceAccessor);
                                        ((ChangeWithColumns) this).addColumn(columnConfig);
                                    }
                                }
                            }
                        } else if (LiquibaseSerializable.class.isAssignableFrom(collectionType)) {
                            List<ParsedNode> childNodes = new ArrayList<ParsedNode>(parsedNode.getChildren(null, param.getParameterName()));
                            for (ParsedNode childNode : childNodes) {
                                LiquibaseSerializable childObject = (LiquibaseSerializable) collectionType.newInstance();
                                childObject.load(childNode, resourceAccessor);

                                ((Collection) param.getCurrentValue(this)).add(childObject);

                            }
                        }
                    }
                } else if (LiquibaseSerializable.class.isAssignableFrom(param.getDataTypeClass())) {
                    try {
                        ParsedNode child = parsedNode.getChild(null, param.getParameterName());
                        if (child != null) {
                            LiquibaseSerializable serializableChild = (LiquibaseSerializable) param.getDataTypeClass().newInstance();
                            serializableChild.load(child, resourceAccessor);
                            param.setValue(this, serializableChild);
                        }
                    } catch (InstantiationException e) {
                        throw new UnexpectedLiquibaseException(e);
                    } catch (IllegalAccessException e) {
                        throw new UnexpectedLiquibaseException(e);
                    }
                } else {
                    Object childValue = parsedNode.getChildValue(null, param.getParameterName(), param.getDataTypeClass());
                    if (childValue == null && param.getSerializationType() == SerializationType.DIRECT_VALUE) {
                        childValue = parsedNode.getValue();
                    }
                    param.setValue(this, childValue);
                }
            }
        } catch (InstantiationException e) {
            throw new UnexpectedLiquibaseException(e);
        } catch (IllegalAccessException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        customLoadLogic(parsedNode, resourceAccessor);
        try {
            this.finishInitialization();
        } catch (SetupException e) {
            throw new ParsedNodeException(e);
        }
    }

    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {

    }

    @Override
    public ParsedNode serialize() throws ParsedNodeException {
        ParsedNode node = new ParsedNode(null, getSerializedObjectName());
        ChangeMetaData metaData = ChangeFactory.getInstance().getChangeMetaData(this);
        for (ChangeParameterMetaData param : metaData.getSetParameters(this).values()) {
            Object currentValue = param.getCurrentValue(this);
            currentValue = serializeValue(currentValue);
            if (currentValue != null) {
                node.addChild(null, param.getParameterName(), currentValue);
            }
        }

        return node;
    }

    protected Object serializeValue(Object value) throws ParsedNodeException {
        if (value instanceof Collection) {
            List returnList = new ArrayList();
            for (Object obj : (Collection) value) {
                Object objValue = serializeValue(obj);
                if (objValue != null) {
                    returnList.add(objValue);
                }
            }
            if (((Collection) value).size() == 0) {
                return null;
            } else {
                return returnList;
            }
        } else if (value instanceof LiquibaseSerializable) {
            return ((LiquibaseSerializable) value).serialize();
        } else {
            return value;
        }
    }
}
